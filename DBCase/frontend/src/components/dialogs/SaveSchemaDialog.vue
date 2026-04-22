<script setup lang="ts">
import { computed, ref } from 'vue'
import { useI18n } from 'vue-i18n'

import { useDiagramStore } from '@/stores/diagramStore'
import { DialogId, useDialogStore } from '@/stores/dialogStore'
import { useErSchemaStore } from '@/stores/erSchemaStore'

const { t } = useI18n()
const dialogStore = useDialogStore()
const erSchemaStore = useErSchemaStore()
const diagramStore = useDiagramStore()
const filename = ref('')
const selectedFormat = ref<'dbw' | 'pdf' | 'sql' | 'txt'>('dbw')

const visible = computed(() => dialogStore.isOpen(DialogId.SaveSchema))
const closeModal = () => {
  dialogStore.close(DialogId.SaveSchema)
  filename.value = ''
  selectedFormat.value = 'dbw'
}

const downloadFile = (content: string, extension: string) => {
  const blob = new Blob([content], { type: 'text/plain' })
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = `${filename.value.trim()}.${extension}`
  link.click()
  URL.revokeObjectURL(url)
}

const saveAsDBW = () => {
  const schemaData = {
    version: '1.0',
    state: erSchemaStore.getCurrentSnapshot(),
  }

  const json = JSON.stringify(schemaData, null, 2)
  downloadFile(json, 'dbw')
}

const saveAsPDF = () => {
  erSchemaStore.exportToPDF(filename.value.trim())
}

const saveAsSQL = () => {
  const sql = diagramStore.dbResult || ''
  downloadFile(sql, 'sql')
}

const saveAsTXT = () => {
  const logical = diagramStore.logicalResult
  if (!logical) {
    downloadFile('', 'txt')
    return
  }

  const map = new Map(Object.entries(logical))
  const relationship = map.get('relationship') ?? ''
  const restriction = map.get('restriction') ?? ''
  const lossRestriction = map.get('lossRestriction') ?? ''

  const content = `${t('schema.relationship')}:\n${relationship}\n\n${t('schema.restriction')}:\n${restriction}\n\n${t('schema.lostRestriction')}:\n${lossRestriction}`
  downloadFile(content, 'txt')
}

const canConfirm = computed(() => {
  if (!filename.value.trim()) return false

  if (selectedFormat.value === 'sql') {
    return !!diagramStore.dbResult && diagramStore.dbResult.trim() !== ''
  }

  if (selectedFormat.value === 'txt') {
    if (!diagramStore.logicalResult) return false
    return Object.values(diagramStore.logicalResult).some((v) => v && v.trim() !== '')
  }

  return true
})

const confirm = () => {
  if (canConfirm.value) {
    switch (selectedFormat.value) {
      case 'dbw':
        saveAsDBW()
        break
      case 'pdf':
        saveAsPDF()
        break
      case 'sql':
        saveAsSQL()
        break
      case 'txt':
        saveAsTXT()
        break
    }
    closeModal()
  }
}

const saveOptions = computed(() => [
  {
    label: 'DBCase Schema (.dbw)',
    icon: 'bi bi-file-earmark-code',
    command: () => {
      selectedFormat.value = 'dbw'
    },
  },
  {
    label: t('schema.pdf'),
    icon: 'bi bi-file-pdf',
    command: () => {
      selectedFormat.value = 'pdf'
    },
  },
  {
    label: t('schema.sql'),
    icon: 'bi bi-filetype-sql',
    disabled: !diagramStore.dbResult,
    command: () => {
      selectedFormat.value = 'sql'
    },
  },
  {
    label: t('schema.text'),
    icon: 'bi bi-file-earmark-text',
    disabled: !diagramStore.logicalResult,
    command: () => {
      selectedFormat.value = 'txt'
    },
  },
])

const confirmLabel = computed(() => {
  const base = t('common.confirm')
  const formats = {
    dbw: '(.dbw)',
    pdf: '(PDF)',
    sql: '(.sql)',
    txt: '(.txt)',
  }
  return `${base} ${formats[selectedFormat.value]}`
})
</script>

<template>
  <Dialog
    :header="t('toolbar.saveSchema')"
    :visible="visible"
    @update:visible="closeModal"
    :dismissable-mask="true"
    :draggable="false"
    modal
    :style="{ width: '30rem' }"
  >
    <form @submit.prevent="confirm">
      <div class="flex flex-col gap-4 mt-2">
        <div class="flex flex-col gap-2">
          <label for="filename" class="font-semibold">{{ t('schema.schemaName') }}</label>
          <InputText
            id="filename"
            v-model="filename"
            :placeholder="t('schema.enterSchemaName')"
            autofocus
            fluid
          />
        </div>
      </div>
    </form>
    <template #footer>
      <Button
        :label="t('common.cancel')"
        icon="bi bi-x-lg"
        severity="secondary"
        text
        @click="closeModal"
      />
      <SplitButton
        :label="confirmLabel"
        icon="bi bi-check-lg"
        :disabled="!canConfirm"
        @click="confirm"
        :model="saveOptions"
      />
    </template>
  </Dialog>
</template>
