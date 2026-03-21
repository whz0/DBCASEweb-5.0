<script setup lang="ts">
import { onMounted, onUnmounted } from 'vue'

import { useDiagramStore } from '@/stores/diagramStore'

const diagramStore = useDiagramStore()

const handleKeyDown = (event: KeyboardEvent) => {
  if (['INPUT', 'TEXTAREA'].includes((event.target as HTMLElement).tagName)) {
    return
  }

  if (event.ctrlKey || event.metaKey) {
    if (event.key.toLowerCase() === 'z') {
      event.preventDefault()
      if (event.shiftKey) {
        diagramStore.redo()
      } else {
        diagramStore.undo()
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
