import { defineStore } from 'pinia'
import { ref } from 'vue'

export const enum PanelId {
  ERScheme,
  LogicalScheme,
  BDScheme,
}

export const useGeneratePanelStore = defineStore('panel', () => {
  const visible = ref<Set<PanelId>>(new Set())

  const open = (id: PanelId) => {
    visible.value.add(id)
  }

  const close = (id: PanelId) => {
    visible.value.delete(id)
  }

  const isOpen = (id: PanelId) => {
    return visible.value.has(id)
  }

  return {
    visible,
    open,
    close,
    isOpen,
  }
})
