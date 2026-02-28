<script setup lang="ts">
import {panelId, useGeneratePanelStore} from "@/stores/generatePanelStore.ts";
import PanelLogicalScheme from "@/components/panels/PanelLogicalScheme.vue";
import PanelDBScheme from "@/components/panels/PanelDBScheme.vue";
import PanelERScheme from "@/components/panels/PanelERScheme.vue";
import {useLayout} from '@/composables/useLayout';
import ToolBar from "@/components/ToolBar.vue";

const panelStore = useGeneratePanelStore()

const { layout } = useLayout();
</script>

<template>
  <header>
    <ToolBar />
  </header>
  <Splitter class="h-full" :layout="layout">
    <SplitterPanel v-show="panelStore.isOpen(panelId.ERScheme)" :minSize="25">
      <PanelERScheme />
    </SplitterPanel>
    <SplitterPanel v-show="panelStore.isOpen(panelId.LogicalScheme)" :minSize="25">
      <PanelLogicalScheme />
    </SplitterPanel>
    <SplitterPanel v-show="panelStore.isOpen(panelId.BDScheme)" :minSize="25">
      <PanelDBScheme />
    </SplitterPanel>
  </Splitter>
</template>

<script lang="ts">

</script>

<style scoped>

</style>
