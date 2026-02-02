<script setup>

import PanelLogicalScheme from "@/components/PanelLogicalScheme.vue";
import PanelDBScheme from "@/components/PanelDBScheme.vue";
import PanelERScheme from "@/components/PanelERScheme.vue";
</script>

<template>
  <div class="row">
    <PanelERScheme/>
    <div class="col-md-6">
      <div class="container">
        <PanelLogicalScheme/>
        <PanelDBScheme/>
      </div>
    </div>
  </div>
</template>

<style scoped>

</style>