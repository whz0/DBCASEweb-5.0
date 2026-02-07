<script setup lang="ts">

import PanelLogicalScheme from "@/components/Panels/PanelLogicalScheme.vue";
import PanelDBScheme from "@/components/Panels/PanelDBScheme.vue";
import PanelERScheme from "@/components/Panels/PanelERScheme.vue";
import GenerateSchemeDialog from "@/components/Dialog/GenerateSchemeDialog.vue";

import {ref} from "vue";

const erVisible = ref(true);
const lVisible = ref(false);
const pVisible = ref(false);
</script>

<template>
  <GenerateSchemeDialog @generateER="(value: boolean) =>  erVisible = value"
                        @generateLogical="(value: boolean) => lVisible = value"
                        @generateDB="(value: boolean) => pVisible = value" />
  <Splitter class="h-full">
    <SplitterPanel v-show='erVisible' >
      <PanelERScheme @close="erVisible = false"/>
    </SplitterPanel>
    <SplitterPanel v-show="lVisible">
      <PanelLogicalScheme @close="lVisible = false"/>
    </SplitterPanel>
    <SplitterPanel v-show="pVisible">
      <PanelDBScheme @close="pVisible = false"/>
    </SplitterPanel>
  </Splitter>
</template>

<style scoped>

</style>
