import {defineStore} from "pinia";
import {ref} from 'vue';

export const enum panelId {
  ERScheme,
  LogicalScheme,
  BDScheme
}

export const useGeneratePanelStore = defineStore('panel', () => {
  const visible = ref<Set<panelId>>(new Set())

  const open = (id: panelId) => {
    visible.value.add(id)
  }

  const close = (id: panelId)=> {
    visible.value.delete(id)
  }

  const isOpen = (id: panelId) => {
    return visible.value.has(id)
  }

  return {
    visible,
    open,
    close,
    isOpen,
  }
})
