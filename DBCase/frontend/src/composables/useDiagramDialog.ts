import { computed } from 'vue'

import { useDiagramStore } from '@/stores/erSchemaStore.ts'
import { DialogId, useDialogStore } from '@/stores/dialogStore'

export function useDiagramDialog(addId: DialogId, editId: DialogId) {
  const dialogStore = useDialogStore()
  const diagramStore = useDiagramStore()

  const isEditMode = computed(() => dialogStore.isOpen(editId))
  const visible = computed(() => dialogStore.isOpen(addId) || isEditMode.value)

  const closeModal = () => {
    dialogStore.close(addId)
    dialogStore.close(editId)
  }

  return {
    diagramStore,
    dialogStore,
    isEditMode,
    visible,
    closeModal,
  }
}
