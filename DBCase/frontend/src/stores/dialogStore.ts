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
  AddIsARelationship,
  AddDomain,
  EditEntity,
  EditRelationship,
  EditIsARelationship,
  EditAttribute,
  EditDomain,
  EditCardinality,
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

  function reset() {
    openDialogs.value.clear()
  }

  return {
    openDialogs,
    open,
    close,
    isOpen,
    reset,
  }
})
