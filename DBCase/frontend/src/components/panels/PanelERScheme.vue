<script setup lang="ts">
import { useToast } from 'primevue'
import { ref } from 'vue'
import { useI18n } from 'vue-i18n'

import DiagramCanvas from '@/components/canvas/DiagramCanvas.vue'
import TransformDiagramDialog from '@/components/dialogs/TransformDiagramDialog.vue'
import { DiagramType, useDiagramStore } from '@/stores/diagramStore.ts'
import { useErSchemaStore } from '@/stores/erSchemaStore'
import { PanelId, useGeneratePanelStore } from '@/stores/generatePanelStore.ts'

const { t } = useI18n()
const toast = useToast()
const panelStore = useGeneratePanelStore()
const erSchemaStore = useErSchemaStore()
const diagramStore = useDiagramStore()

const showTransform = ref(false)

const handleTransform = async (value: DiagramType) => {
  showTransform.value = false
  const toastFn = (message: string, severity: 'error' | 'warn' | 'info' | 'success') =>
    toast.add({ severity, detail: message, life: 3000 })

  await diagramStore.transform(erSchemaStore.getCurrentSnapshot(), DiagramType.er, value, toastFn)

  if (value === DiagramType.logical) {
    if (!panelStore.isOpen(PanelId.LogicalScheme)) panelStore.open(PanelId.LogicalScheme)
  } else if (value === DiagramType.db) {
    if (!panelStore.isOpen(PanelId.BDScheme)) panelStore.open(PanelId.BDScheme)
  }
}
</script>

<template>
  <div class="h-full flex flex-col">
    <div class="flex justify-between items-center p-2">
      <div>
        <Button
          severity="secondary"
          class="bi bi-arrow-90deg-left"
          @click="erSchemaStore.undo()"
          :disabled="!erSchemaStore.canUndo"
          v-tooltip.bottom="t('toolbar.undo')"
          text
        />
        <Button
          severity="secondary"
          class="bi bi-arrow-90deg-right"
          @click="erSchemaStore.redo()"
          :disabled="!erSchemaStore.canRedo"
          v-tooltip.bottom="t('toolbar.redo')"
          text
        />
        <Button
          severity="secondary"
          class="bi bi-file-earmark"
          @click="erSchemaStore.newDiagram()"
          v-tooltip.bottom="t('toolbar.new')"
          text
        />
      </div>
      <div class="text-3xl">
        <h1>{{ t('panels.conceptual') }}</h1>
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
          @click="panelStore.close(PanelId.ERScheme)"
          v-tooltip.bottom="t('panels.close')"
          text
        ></Button>
      </div>
    </div>
    <div class="flex-1">
      <DiagramCanvas />
    </div>
  </div>
  <TransformDiagramDialog
    v-model:visible="showTransform"
    source-type="er"
    @transform="handleTransform"
  />
</template>

<style scoped></style>
