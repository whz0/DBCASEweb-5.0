<script setup lang="ts">
import {ref} from 'vue'

import { useI18n } from 'vue-i18n';
import GenerateSchemeDialog from "@/components/dialogs/GenerateSchemeDialog.vue";

const { t } = useI18n();

const emits = defineEmits(['generatePanel', 'close'])
</script>

<template>
  <div class="h-full grid grid-cols-3">
    <div class="text-3xl col-start-2">
      <h1>{{ t('panels.physical') }}</h1>
    </div>
    <div>
      <GenerateSchemeDialog :i-am="'db'" @generate="(value: string) => $emit('generatePanel', value)"/>
      <Button severity="secondary" class="bi bi-download" text />
      <Button severity="secondary" class="bi bi-x-lg" @click="$emit('close')" v-tooltip.bottom="t('panels.close')" text/>
    </div>
  </div>
</template>

<style scoped>

</style>
