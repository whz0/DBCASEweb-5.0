<script setup lang="ts">
import { jsPDF } from 'jspdf'
import { computed, ref } from 'vue'
import { useI18n } from 'vue-i18n'

import { DialogId, useDialogStore } from '@/stores/dialogStore'
import { useErSchemaStore } from '@/stores/erSchemaStore'

const { t } = useI18n()
const dialogStore = useDialogStore()
const erSchemaStore = useErSchemaStore()
const filename = ref('')
const selectedFormat = ref<'dbw' | 'pdf'>('dbw')

const visible = computed(() => dialogStore.isOpen(DialogId.SaveSchema))
const closeModal = () => {
  dialogStore.close(DialogId.SaveSchema)
  filename.value = ''
  selectedFormat.value = 'dbw'
}

const saveAsDBW = () => {
  const schemaData = {
    version: '1.0',
    state: erSchemaStore.getCurrentSnapshot(),
  }

  const json = JSON.stringify(schemaData, null, 2)
  const blob = new Blob([json], { type: 'application/json' })
  const url = URL.createObjectURL(blob)

  const link = document.createElement('a')
  link.href = url
  link.download = `${filename.value.trim()}.dbw`
  link.click()

  URL.revokeObjectURL(url)
}

const saveAsPDF = () => {
  erSchemaStore.exportToPDF(filename.value.trim())
}

const confirm = () => {
  if (filename.value.trim()) {
    if (selectedFormat.value === 'dbw') {
      saveAsDBW()
    } else {
      saveAsPDF()
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
])

const confirmLabel = computed(() => {
  const base = t('common.confirm')
  return selectedFormat.value === 'dbw' ? `${base} (.dbw)` : `${base} (PDF)`
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
        :disabled="!filename.trim()"
        @click="confirm"
        :model="saveOptions"
      />
    </template>
  </Dialog>
</template>
