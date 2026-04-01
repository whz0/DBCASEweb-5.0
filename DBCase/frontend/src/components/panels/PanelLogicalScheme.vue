<script setup lang="ts">
import {ref} from 'vue'
import {useI18n} from 'vue-i18n'
import {useToast} from "primevue";

import {PanelId, useGeneratePanelStore} from '@/stores/generatePanelStore.ts'
import {DiagramType, useDiagramStore} from "@/stores/diagramStore.ts"

const { t } = useI18n()
const toast = useToast()

const panelStore = useGeneratePanelStore()
const {save, generate} = useDiagramStore()

const relationship = ref('')
const restriction = ref('')
const lossRestriction = ref('')

const handleSave = () => {
  save({
    'relationship': relationship.value,
    'restriction': restriction.value,
    'lossRestriction': lossRestriction.value,
  },
    DiagramType.logical,
    (message, severity) =>
      toast.add({ severity: severity, detail: message, life: 3000 })
  )
}

const handleGenerate = () => {
  const diagram = generate((message, severity) =>
    toast.add({ severity: severity, detail: message, life: 3000 }))
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
        class="bi bi-diagram-3"
        @click="handleGenerate"
        v-tooltip.bottom="t('schema.generateLogical')"
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
  <div class="bg-danger-500 p-6 w-1em h-full">
    <FloatLabel variant="on" class="my-3">
      <Textarea id="relationship" v-model="relationship" rows="8" style="resize: none" fluid />
      <label for="relationship" class="text-xl!">Relaciones</label>
    </FloatLabel>
    <FloatLabel variant="on" class="my-3">
      <Textarea id="restriction" v-model="restriction" rows="4" style="resize: none" fluid />
      <label for="restriction" class="text-xl!">Restricciones de integridad referencial</label>
    </FloatLabel>
    <FloatLabel variant="on" class="my-3">
      <Textarea id="lossRestriction" v-model="lossRestriction" rows="4" style="resize: none" fluid />
      <label for="lossRestriction" class="text-xl!">Restricciones perdidas</label>
    </FloatLabel>
  </div>
</template>

<style scoped></style>
