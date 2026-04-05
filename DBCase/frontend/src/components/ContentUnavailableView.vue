<script setup lang="ts">
interface Action {
  label: string
  icon?: string
  severity?: 'primary' | 'secondary' | 'success' | 'info' | 'warn' | 'help' | 'danger' | 'contrast'
  onClick: () => void
}

defineProps<{
  icon?: string
  title: string
  message: string
  actions?: Action[]
}>()
</script>

<template>
  <div class="flex flex-col items-center justify-center p-12 text-center h-full animate-fade-in">
    <div class="bg-surface-100 dark:bg-surface-800 p-6 rounded-full mb-6">
      <i :class="[icon || 'bi bi-inbox', 'text-5xl text-surface-400 dark:text-surface-500']"></i>
    </div>
    <h3 class="text-2xl font-bold text-surface-900 dark:text-surface-0 mb-2">
      {{ title }}
    </h3>
    <p class="text-surface-600 dark:text-surface-400 max-w-sm mb-8">
      {{ message }}
    </p>
    <div v-if="actions && actions.length" class="flex flex-wrap gap-3 justify-center">
      <Button
        v-for="(action, index) in actions"
        :key="index"
        :label="action.label"
        :icon="action.icon"
        :severity="action.severity || 'primary'"
        @click="action.onClick"
      />
    </div>
  </div>
</template>

<style scoped>
.animate-fade-in {
  animation: fadeIn 0.5s ease-out;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>
