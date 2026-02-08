<script setup lang="ts">
import { useI18n } from 'vue-i18n';

const { t } = useI18n();
</script>

<template>
  <div class="h-full grid grid-cols-3">
    <div>
      <Button severity="secondary" class="bi bi-arrow-90deg-left" text />
      <Button severity="secondary" class="bi bi-arrow-90deg-right" text />
    </div>
    <div class="text-3xl">
      <h1>{{ t('panels.physical') }}</h1>
    </div>
    <div>
      <Button severity="secondary" class="bi bi-download" text />
      <Button severity="secondary" class="bi bi-x-lg" @click="$emit('close')" v-tooltip.bottom="t('panels.close')" />
    </div>
  </div>
</template>

<style scoped>

</style>
