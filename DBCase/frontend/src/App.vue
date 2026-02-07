<script setup lang="ts">
  import ToolBar from '@/components/ToolBar.vue'
</script>

<template>
  <header>

  </header>
  <ToolBar />
  <main>

    <RouterView />
  </main>
</template>

<script lang="ts">
export default {
  name: 'App'
}
</script>

<style>
#app {
  font-family: Avenir, Helvetica, Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  text-align: center;
}
</style>
