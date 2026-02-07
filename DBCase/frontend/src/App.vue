<script setup lang="ts">
  import ToolBar from '@/components/ToolBar.vue'
</script>

<template>
  <header>

  </header>
  <ToolBar />
  <main class="flex-1 overflow-auto">
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
  @apply flex flex-col h-screen;
}
</style>
