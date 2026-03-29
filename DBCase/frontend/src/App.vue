<script setup lang="ts">
import { onMounted, onUnmounted } from 'vue'

import { useErSchemaStore } from '@/stores/erSchemaStore'

const erSchemaStore = useErSchemaStore()

const handleKeyDown = (event: KeyboardEvent) => {
  if (['INPUT', 'TEXTAREA'].includes((event.target as HTMLElement).tagName)) {
    return
  }

  if (event.ctrlKey || event.metaKey) {
    if (event.key.toLowerCase() === 'z') {
      event.preventDefault()
      if (event.shiftKey) {
        erSchemaStore.redo()
      } else {
        erSchemaStore.undo()
      }
    }
  }
}

onMounted(() => {
  window.addEventListener('keydown', handleKeyDown)
})

onUnmounted(() => {
  window.removeEventListener('keydown', handleKeyDown)
})
</script>

<template>
  <Toast />
  <main class="flex-1 min-h-0">
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
