import { ref, type Ref } from 'vue';

interface Dialog {
  visible: Ref<boolean>;
  open: () => void;
  close: () => void;
}

class DialogMap extends Map<string, Dialog> {
  computeIfAbsent(key: string, compute: () => Dialog): Dialog {
    if (!this.has(key)) {
      this.set(key, compute());
    }
    return this.get(key)!;
  }
}

const dialogs = new DialogMap();

export function useDialog(name: string): Dialog {
  return dialogs.computeIfAbsent(name, () => {
    const visible = ref(false);

    return {
      visible,
      open: () => { visible.value = true; },
      close: () => { visible.value = false; }
    };
  });
}
