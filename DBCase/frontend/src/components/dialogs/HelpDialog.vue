<script setup lang="ts">
  import {ref} from 'vue';
  import { useI18n } from 'vue-i18n';

  const visible = ref(false);
  const { t } = useI18n();
</script>

<template>
  <Button severity="secondary" class="bi bi-question-circle" @click="visible = true" text v-tooltip.bottom="t('help.title')" />

  <Dialog v-model:visible="visible" maximizable modal :header="t('help.header')" :style="{ width: '50rem' }" :breakpoints="{ '1199px': '75vw', '575px': '90vw' }">
    <p class="m-0">
      {{ t('help.content') }}
    </p>
  </Dialog>
</template>

<style scoped>

</style>
