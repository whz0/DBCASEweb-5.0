<script setup lang="ts">
import {ref} from 'vue';
import { useI18n } from 'vue-i18n';

const visible = ref(false);
const { t } = useI18n();

const emit = defineEmits(['generate-er', 'generate-logical', 'generate-db'])

</script>

<template>
  <Button severity="secondary" class="bi bi-magic" @click="visible = true" text v-tooltip.bottom="t('schema.generate')" />

  <Dialog v-model:visible="visible"  modal :header="t('schema.generate')" :style="{ width: '50rem' }" :breakpoints="{ '1199px': '75vw', '575px': '90vw' }">
    <div class="grid gap-2 grid-rows-3">
      <Button @click="emit('generate-er', true)">{{ t('schema.generateER') }}</Button>
      <Button @click="emit('generate-logical', true)">{{ t('schema.generateLogical') }}</Button>
      <Button @click="emit('generate-db', true)">{{ t('schema.generatePhysical') }}</Button>
    </div>
  </Dialog>
</template>

<script lang="ts">

</script>

<style scoped>

</style>
