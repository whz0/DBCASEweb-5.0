<script setup lang="ts">
import { computed } from 'vue'
import { useTheme } from '@/composables/useTheme'
import { useI18n } from 'vue-i18n'
import { DialogId, useDialogStore } from '@/stores/dialogStore'
import Select from 'primevue/select'
import Dialog from 'primevue/dialog'

const dialogStore = useDialogStore()
const { theme, setTheme } = useTheme()
const { locale, t } = useI18n()

const themeOptions = computed(() => [
  { label: t('settings.light'), value: 'light', icon: 'bi bi-sun' },
  { label: t('settings.dark'), value: 'dark', icon: 'bi bi-moon' },
  { label: t('settings.system'), value: 'system', icon: 'bi bi-circle-half' },
])

const languageOptions = computed(() => [
  { label: t('settings.english'), value: 'en' },
  { label: t('settings.spanish'), value: 'es' },
])

const changeLanguage = (lang: string) => {
  locale.value = lang
  localStorage.setItem('locale', lang)
}

const visible = computed(() => dialogStore.isOpen(DialogId.Accessibility))
const closeModal = () => dialogStore.close(DialogId.Accessibility)
</script>

<template>
  <Dialog
    :visible="visible"
    @update:visible="closeModal"
    modal
    :dismissable-mask="true"
    :draggable="false"
    :header="t('settings.title')"
    :style="{ width: '30rem' }"
  >
    <div class="flex flex-col gap-4">
      <div class="flex items-center justify-between">
        <label>{{ t('settings.language') }}</label>
        <Select
          :modelValue="locale"
          @update:modelValue="changeLanguage"
          :options="languageOptions"
          optionLabel="label"
          optionValue="value"
        />
      </div>
      <div class="flex items-center justify-between">
        <label>{{ t('settings.theme') }}</label>
        <Select
          :modelValue="theme"
          @update:modelValue="setTheme"
          :options="themeOptions"
          optionLabel="label"
          optionValue="value"
        >
          <template #value="{ value }">
            <i :class="themeOptions.find((o) => o.value === value)?.icon"></i>
            <span class="ml-2">{{ themeOptions.find((o) => o.value === value)?.label }}</span>
          </template>
          <template #option="{ option }">
            <i :class="option.icon"></i>
            <span class="ml-2">{{ option.label }}</span>
          </template>
        </Select>
      </div>
    </div>
  </Dialog>
</template>
