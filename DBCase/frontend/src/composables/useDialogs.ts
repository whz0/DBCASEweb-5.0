import { ref } from 'vue';

const addEntityDialogVisible = ref(false);

export function useDialogs() {
  return {
    addEntityDialogVisible,
    openAddEntityDialog: () => {
      addEntityDialogVisible.value = true;
    }
  };
}
