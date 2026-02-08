<script setup lang="ts">
  import {ref} from 'vue';
  import { useI18n } from 'vue-i18n';

  const visible = ref(false);
  const { t } = useI18n();
</script>

<template>
  <Button severity="secondary" class="bi bi-info-circle" @click="visible = true" text v-tooltip.bottom="t('aboutUs.title')"/>

  <Dialog v-model:visible="visible"  modal :header="t('aboutUs.header')" :style="{ width: '50rem' }" :breakpoints="{ '1199px': '75vw', '575px': '90vw' }">
    <p class="m-0">
      {{ t('aboutUs.content') }}
    </p>
  </Dialog>
</template>

<style scoped>

</style>
