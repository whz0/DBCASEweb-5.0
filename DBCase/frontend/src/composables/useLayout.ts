import { ref } from 'vue';

const layout = ref<'horizontal' | 'vertical'>('horizontal');

export function useLayout() {
  const setLayout = (newLayout: 'horizontal' | 'vertical') => {
    layout.value = newLayout;
  };

  return {
    layout,
    setLayout
  };
}
