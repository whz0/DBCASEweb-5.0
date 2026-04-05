<script setup lang="ts">
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'

import ContentUnavailableView from '@/components/ContentUnavailableView.vue'
import PanelDBScheme from '@/components/panels/PanelDBScheme.vue'
import PanelERScheme from '@/components/panels/PanelERScheme.vue'
import PanelLogicalScheme from '@/components/panels/PanelLogicalScheme.vue'
import ToolBar from '@/components/ToolBar.vue'
import { useLayout } from '@/composables/useLayout'
import { DialogId, useDialogStore } from '@/stores/dialogStore'
import { useErSchemaStore } from '@/stores/erSchemaStore'
import { PanelId, useGeneratePanelStore } from '@/stores/generatePanelStore'

const panelStore = useGeneratePanelStore()
const dialogStore = useDialogStore()
const { t } = useI18n()

const isAnyPanelOpen = computed(() => {
  return (
    panelStore.isOpen(PanelId.ERScheme) ||
    panelStore.isOpen(PanelId.LogicalScheme) ||
    panelStore.isOpen(PanelId.BDScheme)
  )
})

const noSchemaActions = computed(() => [
  {
    label: t('schema.generate'),
    icon: 'bi bi-gear',
    onClick: () => dialogStore.open(DialogId.GenerateScheme),
  },
])

const { layout } = useLayout()

const store = useErSchemaStore()

if (store.entities.length === 0 && store.relationships.length === 0) {
  store.addEntity({
    id: '1',
    name: 'Person',
    position: { x: 50, y: 100 },
    attributes: ['a1'],
    primaryKeys: ['a1'],
  })
  store.addEntity({
    id: '2',
    name: 'Car',
    position: { x: 300, y: 100 },
    attributes: [],
    primaryKeys: [],
  })
  store.addRelationship({
    id: '3',
    name: 'Owns',
    position: { x: 175, y: 100 },
    type: 'Normal',
    participants: [
      { entityId: '1', cardinalityMin: '1', cardinalityMax: 'N' },
      { entityId: '2', cardinalityMin: '0', cardinalityMax: 'N' },
    ],
    attributes: [],
  })
}
</script>

<template>
  <div class="h-screen flex flex-col overflow-hidden">
    <header>
      <ToolBar />
    </header>
    <Splitter v-if="isAnyPanelOpen" class="flex-1 min-h-0" :layout="layout">
      <SplitterPanel v-show="panelStore.isOpen(PanelId.ERScheme)" :minSize="25">
        <PanelERScheme />
      </SplitterPanel>
      <SplitterPanel v-show="panelStore.isOpen(PanelId.LogicalScheme)" :minSize="25">
        <PanelLogicalScheme />
      </SplitterPanel>
      <SplitterPanel v-show="panelStore.isOpen(PanelId.BDScheme)" :minSize="25">
        <PanelDBScheme />
      </SplitterPanel>
    </Splitter>
    <div v-else class="flex-1">
      <ContentUnavailableView
        icon="bi bi-window-dash"
        :title="t('schema.noSchemasTitle')"
        :message="t('schema.noSchemasMessage')"
        :actions="noSchemaActions"
      />
    </div>
  </div>
</template>

<style scoped></style>
