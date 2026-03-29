<script setup lang="ts">
import PanelDBScheme from '@/components/panels/PanelDBScheme.vue'
import PanelERScheme from '@/components/panels/PanelERScheme.vue'
import PanelLogicalScheme from '@/components/panels/PanelLogicalScheme.vue'
import ToolBar from '@/components/ToolBar.vue'
import { useLayout } from '@/composables/useLayout'
import { useErSchemaStore } from '@/stores/erSchemaStore'
import { PanelId, useGeneratePanelStore } from '@/stores/generatePanelStore'

const panelStore = useGeneratePanelStore()

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
    participants: [],
    attributes: [],
  })
}
</script>

<template>
  <header>
    <ToolBar />
  </header>
  <Splitter class="h-full" :layout="layout">
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
</template>

<style scoped></style>
