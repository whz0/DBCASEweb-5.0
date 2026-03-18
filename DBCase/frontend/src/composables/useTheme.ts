import { computed, ref, watch } from 'vue'

const getSystemTheme = () =>
  window.matchMedia('(prefers-color-scheme: dark)').matches ? 'dark' : 'light'
const savedTheme = localStorage.getItem('theme') as 'light' | 'dark' | 'system' | null
const theme = ref<'light' | 'dark' | 'system'>(savedTheme || 'system')
const systemTheme = ref(getSystemTheme())

// Listen for system theme changes
window.matchMedia('(prefers-color-scheme: dark)').addEventListener('change', (e) => {
  systemTheme.value = e.matches ? 'dark' : 'light'
})

const actualTheme = computed(() => {
  return theme.value === 'system' ? systemTheme.value : theme.value
})

watch(
  actualTheme,
  (newTheme) => {
    if (newTheme === 'dark') {
      document.documentElement.classList.add('dark')
    } else {
      document.documentElement.classList.remove('dark')
    }
  },
  { immediate: true },
)

watch(theme, (newTheme) => {
  localStorage.setItem('theme', newTheme)
})

export function useTheme() {
  const setTheme = (newTheme: 'light' | 'dark' | 'system') => {
    theme.value = newTheme
  }

  return {
    theme,
    actualTheme,
    setTheme,
  }
}
