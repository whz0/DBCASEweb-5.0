<script setup lang="ts">

import PanelLogicalScheme from "@/components/panels/PanelLogicalScheme.vue";
import PanelDBScheme from "@/components/panels/PanelDBScheme.vue";
import PanelERScheme from "@/components/panels/PanelERScheme.vue";
import GenerateSchemeDialog from "@/components/dialogs/GenerateSchemeDialog.vue";

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
