<script setup lang="ts">

import PanelLogicalScheme from "@/components/panels/PanelLogicalScheme.vue";
import PanelDBScheme from "@/components/panels/PanelDBScheme.vue";
import PanelERScheme from "@/components/panels/PanelERScheme.vue";

import {ref, toRef} from "vue";
import { useLayout } from '@/composables/useLayout';

const erVisible = ref(true);
const lVisible = ref(false);
const dbVisible = ref(false);

function generate(data: string) {
  switch(data) {
    case 'er': erVisible.value = true;
    break;
    case 'logical': lVisible.value = true;
    break;
    case 'db': dbVisible.value = true;
    break;
  }
}

const { layout } = useLayout();
</script>

<template>
  <Splitter class="h-full" :layout="layout">
    <SplitterPanel v-show='erVisible' :minSize="25">
      <PanelERScheme @close="erVisible = false" @generate-panel="generate"/>
    </SplitterPanel>
    <SplitterPanel v-show="lVisible" :minSize="25">
      <PanelLogicalScheme @close="lVisible = false" @generate-panel="generate"/>
    </SplitterPanel>
    <SplitterPanel v-show="dbVisible" :minSize="25">
      <PanelDBScheme @close="dbVisible = false" @generate-panel="generate"/>
    </SplitterPanel>
  </Splitter>
</template>

<style scoped>

</style>
