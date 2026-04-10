<script setup lang="ts">
import { useToast } from 'primevue'
import { ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'

import TransformDiagramDialog from '@/components/dialogs/TransformDiagramDialog.vue'
import { DiagramType, useDiagramStore } from '@/stores/diagramStore'
import { useErSchemaStore } from '@/stores/erSchemaStore'
import { PanelId, useGeneratePanelStore } from '@/stores/generatePanelStore'

const { t } = useI18n()
const toast = useToast()

const panelStore = useGeneratePanelStore()
const diagramStore = useDiagramStore()
const erSchemaStore = useErSchemaStore()
const { save, transform } = useDiagramStore()

const relationship = ref('')
const restriction = ref('')
const lossRestriction = ref('')
const toastMessage = (message: string, severity: 'error' | 'warn' | 'info' | 'success') =>
  toast.add({ severity, detail: message, life: 3000 })

watch(
  () => diagramStore.logicalResult,
  (value) => {
    if (!value) return

    const map = new Map(Object.entries(value))

    relationship.value = map.get('relationship') ?? ''
    restriction.value = map.get('restriction') ?? ''
    lossRestriction.value = map.get('lossRestriction') ?? ''
  },
)

const handleSave = () => {
  save(toastMessage)
}

const showTransform = ref(false)

const handleTransform = async (value: DiagramType) => {
  showTransform.value = false

  const diagram = {
    relationship: relationship.value,
    restriction: restriction.value,
    lossRestriction: lossRestriction.value,
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
        class="bi bi-save"
        @click="handleSave"
        v-tooltip.bottom="t('common.save')"
        text
      />
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
    <FloatLabel variant="on" class="my-3">
      <Textarea id="relationship" v-model="relationship" rows="8" style="resize: none" fluid />
      <label for="relationship" class="text-xl!">{{ t('schema.relationship') }}</label>
    </FloatLabel>
    <FloatLabel variant="on" class="my-3">
      <Textarea id="restriction" v-model="restriction" rows="4" style="resize: none" fluid />
      <label for="restriction" class="text-xl!">{{ t('schema.restriction') }}</label>
    </FloatLabel>
    <FloatLabel variant="on" class="my-3">
      <Textarea
        id="lossRestriction"
        v-model="lossRestriction"
        rows="4"
        style="resize: none"
        fluid
      />
      <label for="lossRestriction" class="text-xl!">{{ t('schema.lostRestriction') }}</label>
    </FloatLabel>
  </div>
</template>

<style scoped></style>
