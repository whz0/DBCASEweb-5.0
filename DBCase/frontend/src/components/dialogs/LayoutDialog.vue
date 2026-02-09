<script setup lang="ts">
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { useLayout } from '@/composables/useLayout'
import { DialogId, useDialogStore } from '@/stores/dialogStore'
import Dialog from 'primevue/dialog'
import Button from 'primevue/button'

const { t } = useI18n()
const dialogStore = useDialogStore()
const { setLayout } = useLayout()

const layouts = [
  { label: t('layout.horizontal'), value: 'horizontal', icon: 'bi bi-layout-split' },
  { label: t('layout.vertical'), value: 'vertical', icon: 'bi bi-layout-three-columns' },
]

const visible = computed(() => dialogStore.isOpen(DialogId.Layout))
const closeModal = () => dialogStore.close(DialogId.Layout)

const selectLayout = (layout: 'horizontal' | 'vertical') => {
  setLayout(layout)
  closeModal()
}
</script>

<template>
  <Dialog
    :visible="visible"
    @update:visible="closeModal"
    modal
    :header="t('layout.chooseLayout')"
    :style="{ width: '30rem' }"
  >
    <div class="flex flex-col gap-4">
      <div class="flex gap-4 justify-center">
        <Button
          v-for="layout in layouts"
          :key="layout.value"
          :icon="layout.icon"
          :label="layout.label"
          @click="selectLayout(layout.value as any)"
          severity="secondary"
          outlined
          class="flex-1"
        />
      </div>
    </div>
  </Dialog>
</template>
