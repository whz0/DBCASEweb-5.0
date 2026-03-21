<script setup lang="ts">
import { useI18n } from 'vue-i18n'

import { PanelId, useGeneratePanelStore } from '@/stores/generatePanelStore.ts'

const { t } = useI18n()
const panelStore = useGeneratePanelStore()
</script>

<template>
  <div class="h-full grid grid-cols-3">
    <div class="text-3xl col-start-2">
      <h1>{{ t('panels.physical') }}</h1>
    </div>
    <div>
      <Button
        severity="secondary"
        class="bi bi-download"
        v-tooltip.bottom="t('panels.download')"
        text
      />
      <Button
        severity="secondary"
        class="bi bi-x-lg"
        @click="panelStore.close(PanelId.BDScheme)"
        v-tooltip.bottom="t('panels.close')"
        text
      />
    </div>
  </div>
</template>

<style scoped></style>
