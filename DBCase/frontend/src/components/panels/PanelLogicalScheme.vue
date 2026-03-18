<script setup lang="ts">
import { ref } from 'vue'
import { useI18n } from 'vue-i18n'

import { PanelId, useGeneratePanelStore } from '@/stores/generatePanelStore.ts'

const { t } = useI18n()
const panelStore = useGeneratePanelStore()

const value = ref('')
</script>

<template>
  <div class="grid grid-cols-3">
    <div class="text-3xl col-start-2">
      <h1>{{ t('panels.logical') }}</h1>
    </div>
    <div>
      <Button severity="secondary" class="bi bi-download" text />
      <Button
        severity="secondary"
        class="bi bi-x-lg"
        @click="panelStore.close(PanelId.LogicalScheme)"
        text
      />
    </div>
  </div>
  <div class="bg-danger-500 p-6 w-1em h-full">
    <Textarea
      v-model="value"
      variant="filled"
      rows="3"
      fluid
      :invalid="!value"
      style="resize: none"
      class="h-9/12"
    />
  </div>
</template>

<style scoped></style>
