<script setup lang="ts">
import {computed, ref} from 'vue'
import {useI18n} from 'vue-i18n'
import {DialogId, useDialogStore} from '@/stores/dialogStore'
import {panelId, useGeneratePanelStore} from "@/stores/generatePanelStore.ts";

const { t } = useI18n()
const dialogStore = useDialogStore()
const panelStore = useGeneratePanelStore()
const menu = ref()
const items = computed(() => [
  {
    label: t('entity.addEntity'),
    icon: 'bi bi-square',
    command: () => dialogStore.open(DialogId.AddEntity),
  },
  {
    label: t('panels.insertRelationship'),
    icon: 'bi bi-diagram-3',
  },
  {
    label: t('panels.insertIsARelationship'),
    icon: 'bi bi-diagram-2',
  },
  {
    label: t('panels.createDomain'),
    icon: 'bi bi-collection',
  },
])

const onRightClick = (e: Event) => {
  menu.value.show(e)
}
</script>

<template>
  <div @contextmenu="onRightClick" aria-haspopup="true" class="h-full grid grid-cols-3">
    <div>
      <Button severity="secondary" class="bi bi-arrow-90deg-left" text />
      <Button severity="secondary" class="bi bi-arrow-90deg-right" text />
    </div>
    <div class="text-3xl">
      <h1>{{ t('panels.conceptual') }}</h1>
    </div>
    <div>
      <Button severity="secondary" class="bi bi-download" text />
      <Button
        severity="secondary"
        class="bi bi-x-lg"
        @click="panelStore.close(panelId.ERScheme)"
        v-tooltip.bottom="t('panels.close')"
        text
      ></Button>
    </div>
    <ContextMenu ref="menu" :model="items" />
  </div>
</template>

<script lang="ts"></script>

<style scoped></style>
