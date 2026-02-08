<script setup lang="ts">

import PanelLogicalScheme from "@/components/panels/PanelLogicalScheme.vue";
import PanelDBScheme from "@/components/panels/PanelDBScheme.vue";
import PanelERScheme from "@/components/panels/PanelERScheme.vue";
import GenerateSchemeDialog from "@/components/dialogs/GenerateSchemeDialog.vue";

import {ref} from "vue";
import { useLayout } from '@/composables/useLayout';

const erVisible = ref(true);
const lVisible = ref(false);
const pVisible = ref(false);
const { layout } = useLayout();
</script>

<template>
  <GenerateSchemeDialog @generate-er="(value: boolean) =>  erVisible = value"
                        @generate-logical="(value: boolean) => lVisible = value"
                        @generate-db="(value: boolean) => pVisible = value" />
  <Splitter class="h-full" :layout="layout">
    <SplitterPanel v-show='erVisible' :minSize="25">
      <PanelERScheme @close="erVisible = false"/>
    </SplitterPanel>
    <SplitterPanel v-show="lVisible" :minSize="25">
      <PanelLogicalScheme @close="lVisible = false"/>
    </SplitterPanel>
    <SplitterPanel v-show="pVisible" :minSize="25">
      <PanelDBScheme @close="pVisible = false"/>
    </SplitterPanel>
  </Splitter>
</template>

<style scoped>

</style>
