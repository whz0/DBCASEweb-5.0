<script setup lang="ts">
import { ref } from 'vue'
import { useI18n } from 'vue-i18n'

import DiagramCanvas from '@/components/canvas/DiagramCanvas.vue'
import TransformDiagramDialog from '@/components/dialogs/TransformDiagramDialog.vue'
import { useErSchemaStore } from '@/stores/erSchemaStore'
import { PanelId, useGeneratePanelStore } from '@/stores/generatePanelStore.ts'

const { t } = useI18n()
const panelStore = useGeneratePanelStore()
const erSchemaStore = useErSchemaStore()

const showTransform = ref(false)

const handleTransform = (target: 'er' | 'logical' | 'physical') => {
  showTransform.value = false
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
