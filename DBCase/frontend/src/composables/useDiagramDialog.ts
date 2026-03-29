import { computed } from 'vue'

import { DialogId, useDialogStore } from '@/stores/dialogStore'
import { useErSchemaStore } from '@/stores/erSchemaStore'

export function useDiagramDialog(addId: DialogId, editId: DialogId) {
  const dialogStore = useDialogStore()
  const erSchemaStore = useErSchemaStore()

  const isEditMode = computed(() => dialogStore.isOpen(editId))
  const visible = computed(() => dialogStore.isOpen(addId) || isEditMode.value)

  const closeModal = () => {
    dialogStore.close(addId)
    dialogStore.close(editId)
  }

  return {
    erSchemaStore,
    dialogStore,
    isEditMode,
    visible,
    closeModal,
  }
}
