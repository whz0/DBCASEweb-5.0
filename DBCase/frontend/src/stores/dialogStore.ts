import { defineStore } from 'pinia'
import { ref } from 'vue'

export const enum DialogId {
  AddEntity,
  AddRelationship,
  AddAttribute,
  SaveSchema,
  OpenSchema,
  GenerateScheme,
  Help,
  About,
  Accessibility,
  Layout,
  AddEntityToRelationship,
  Rename,
  AddIsARelationship,
  AddDomain
}

export const useDialogStore = defineStore('dialogs', () => {
  const openDialogs = ref<Set<DialogId>>(new Set())

  function open(dialogId: DialogId) {
    openDialogs.value.add(dialogId)
  }

  function close(dialogId: DialogId) {
    openDialogs.value.delete(dialogId)
  }

  function isOpen(dialogId: DialogId): boolean {
    return openDialogs.value.has(dialogId)
  }

  return {
    openDialogs,
    open,
    close,
    isOpen
  }
})
