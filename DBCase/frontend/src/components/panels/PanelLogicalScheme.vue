<script setup lang="ts">
import { useToast } from 'primevue'
import { ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'

import TransformDiagramDialog from '@/components/dialogs/TransformDiagramDialog.vue'
import ScrollablePanel from '@/components/panels/ScrollablePanel.vue'
import { DiagramType, useDiagramStore } from '@/stores/diagramStore'
import { useErSchemaStore } from '@/stores/erSchemaStore'
import { PanelId, useGeneratePanelStore } from '@/stores/generatePanelStore'

const { t } = useI18n()
const toast = useToast()

const panelStore = useGeneratePanelStore()
const diagramStore = useDiagramStore()
const erSchemaStore = useErSchemaStore()
const { transform } = useDiagramStore()

const relationship = ref('')
const restriction = ref('')
const lostRestriction = ref('')
const toastMessage = (message: string, severity: 'error' | 'warn' | 'info' | 'success') =>
  toast.add({ severity, detail: message, life: 3000 })

watch(
  () => diagramStore.logicalResult,
  (value) => {
    if (!value) return
    const newRel = fromApiFormat(value.relationship ?? '')
    const newRes = fromApiFormat(value.restriction ?? '')
    const newLost = fromApiFormat(value.lossRestriction ?? '')
    if (relationship.value !== newRel) relationship.value = newRel
    if (restriction.value !== newRes) restriction.value = newRes
    if (lostRestriction.value !== newLost) lostRestriction.value = newLost
  },
)

watch([relationship, restriction, lostRestriction], ([rel, res, lost]) => {
  const next = {
    relationship: toApiFormat(rel),
    restriction: toApiFormat(res),
    lossRestriction: toApiFormat(lost),
  }
  const cur = diagramStore.logicalResult
  if (
    cur?.relationship === next.relationship &&
    cur?.restriction === next.restriction &&
    cur?.lossRestriction === next.lossRestriction
  )
    return
  diagramStore.logicalResult = next
})

const showTransform = ref(false)

const decodeHtmlEntities = (str: string) => {
  const txt = document.createElement('textarea')
  txt.innerHTML = str
  return txt.value
}

const toApiFormat = (html: string) => {
  return (
    html
      .replace(/<u>(.*?)<\/u>/gi, '__$1__')
      .replace(/<div>/gi, '\n')
      .replace(/<\/div>/gi, '')
      .replace(/<br\s*\/?>/gi, '\n')
      .replace(/<[^>]+>/g, '')
      .replace(/\n+$/, '')
      .split('\n')
      .map(decodeHtmlEntities)
      .join('\n') + '\n'
  )
}

const fromApiFormat = (text: string) => {
  return text
    .replace(/\n$/, '')
    .replace(/__([^_]*)__/g, '<u>$1</u>')
    .replace(/\n/g, '<br>')
}

const handleTransform = async (value: DiagramType) => {
  showTransform.value = false

  const diagram = {
    relationship: toApiFormat(relationship.value),
    restriction: toApiFormat(restriction.value),
    lossRestriction: toApiFormat(lostRestriction.value),
  }

  const data = await transform(diagram, DiagramType.logical, value, toastMessage)

  if (value === DiagramType.er) {
    if (data) erSchemaStore.loadSnapshot(data)
    if (!panelStore.isOpen(PanelId.ERScheme)) panelStore.open(PanelId.ERScheme)
  } else if (value === DiagramType.db) {
    if (!panelStore.isOpen(PanelId.BDScheme)) panelStore.open(PanelId.BDScheme)
  }
}
</script>

<template>
  <div class="grid grid-cols-3 my-4">
    <div class="text-3xl col-start-2">
      <h1>{{ t('panels.logical') }}</h1>
    </div>
    <div>
      <Button
        severity="secondary"
        class="bi bi-arrow-left-right"
        @click="showTransform = true"
        v-tooltip.bottom="t('schema.transform')"
        text
      />
      <Button
        severity="secondary"
        class="bi bi-x-lg"
        @click="panelStore.close(PanelId.LogicalScheme)"
        v-tooltip.bottom="t('panels.close')"
        text
      />
    </div>
  </div>
  <TransformDiagramDialog
    v-model:visible="showTransform"
    source-type="logical"
    @transform="handleTransform"
  />
  <div class="bg-danger-500 p-6 w-1em h-full">
    <ScrollablePanel :title="t('schema.relationship')" heigh="h-4/12" v-model="relationship" />
    <ScrollablePanel :title="t('schema.restriction')" heigh="h-3/12" v-model="restriction" />
    <ScrollablePanel
      :title="t('schema.lostRestriction')"
      heigh="h-3/12"
      v-model="lostRestriction"
    />
  </div>
</template>

<style scoped></style>
