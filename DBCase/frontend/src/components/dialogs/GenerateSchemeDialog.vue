<script setup lang="ts">
import {computed} from 'vue'
import {useI18n} from 'vue-i18n';
import {DialogId, useDialogStore} from '@/stores/dialogStore'
import {panelId, useGeneratePanelStore} from "@/stores/generatePanelStore.ts";

const { t } = useI18n();

const dialogStore = useDialogStore()
const panelStore = useGeneratePanelStore()

const visible = computed(() => dialogStore.isOpen(DialogId.Help))
const closeModal = () => dialogStore.close(DialogId.Help)

</script>

<template>
  <Dialog
    v-model:visible="visible"
    @update:visible="closeModal"
    modal
    :dismissable-mask="true"
    :draggable="false"
    :header="t('schema.generate')"
    :style="{ width: '50rem' }"
    :breakpoints="{ '1199px': '75vw', '575px': '90vw' }"
  >
    <div class="grid gap-2 grid-rows-2" >
      <Button
        @click="panelStore.open((panelId.ERScheme))"
      >{{ t('schema.generateER') }}</Button>
      <Button
        @click="panelStore.open((panelId.LogicalScheme))"
      >{{ t('schema.generateLogical') }}</Button>
      <Button
        @click="panelStore.open((panelId.BDScheme))"
      >{{ t('schema.generatePhysical') }}</Button>
    </div>
  </Dialog>
</template>

<script lang="ts">

</script>

<style scoped>

</style>
