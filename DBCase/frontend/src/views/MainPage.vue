<script setup lang="ts">

import PanelLogicalScheme from "@/components/PanelLogicalScheme.vue";
import PanelDBScheme from "@/components/PanelDBScheme.vue";
import PanelERScheme from "@/components/PanelERScheme.vue";
</script>

<template>
  <div class="row vh-100">
    <PanelERScheme/>
    <div class="col-md-6">
      <div class="container-fluid h-100">
        <PanelLogicalScheme/>
        <PanelDBScheme/>
      </div>
    </div>
  </div>
</template>

<script lang="ts">
export default {
  name: 'MainPage'
}
</script>

<style scoped>

</style>
