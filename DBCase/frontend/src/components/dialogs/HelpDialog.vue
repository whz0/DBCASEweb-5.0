<script setup lang="ts">
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { DialogId, useDialogStore } from '@/stores/dialogStore'
import Dialog from 'primevue/dialog'

const { t } = useI18n()
const dialogStore = useDialogStore()

const visible = computed(() => dialogStore.isOpen(DialogId.Help))
const closeModal = () => dialogStore.close(DialogId.Help)
</script>

<template>
  <Dialog
    :visible="visible"
    @update:visible="closeModal"
    maximizable
    modal
    :dismissable-mask="true"
    :draggable="false"
    :header="t('help.header')"
    :style="{ width: '50rem' }"
    :breakpoints="{ '1199px': '75vw', '575px': '90vw' }"
  >
    <p class="m-0">
      {{ t('help.content') }}
    </p>
  </Dialog>
</template>

<style scoped></style>
