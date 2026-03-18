<script setup lang="ts"></script>

<template>
  <!-- CI Test Comment -->
  <Toast />
  <main class="flex-1 justify-center overflow-auto">
    <RouterView />
  </main>
</template>

<script lang="ts">
export default {
  name: 'App',
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
