<script setup lang="ts">
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'

import { useCardinalityMode } from '@/composables/useCardinalityMode'
import { useTheme } from '@/composables/useTheme'
import { api } from '@/plugins/axios'
import { useAuthStore } from '@/stores/authStore'
import { DialogId, useDialogStore } from '@/stores/dialogStore'

const dialogStore = useDialogStore()
const authStore = useAuthStore()
const { theme, setTheme } = useTheme()
const { locale, t } = useI18n()
const { showArrow, showNumber, showMinMax, setArrow, setNumber, setMinMax } = useCardinalityMode()

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

const closeModal = () => {
  if (authStore.user.username) {
    api.user.saveSettings({ language: locale.value, theme: theme.value })
  }
  dialogStore.close(DialogId.Accessibility)
}
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
      <div class="flex flex-col gap-2">
        <label>{{ t('settings.cardinalityNotation') }}</label>
        <div class="flex flex-col gap-2 pl-1">
          <div class="flex items-center gap-2">
            <Checkbox
              :modelValue="showArrow"
              :binary="true"
              inputId="chkArrow"
              :disabled="showArrow && !showNumber && !showMinMax"
              @update:modelValue="setArrow"
            />
            <label for="chkArrow">{{ t('settings.cardinalityArrow') }}</label>
          </div>
          <div class="flex items-center gap-2">
            <Checkbox
              :modelValue="showNumber"
              :binary="true"
              inputId="chkNumber"
              :disabled="showNumber && !showArrow && !showMinMax"
              @update:modelValue="setNumber"
            />
            <label for="chkNumber">{{ t('settings.cardinalityNumber') }}</label>
          </div>
          <div class="flex items-center gap-2">
            <Checkbox
              :modelValue="showMinMax"
              :binary="true"
              inputId="chkMinMax"
              :disabled="showMinMax && !showArrow && !showNumber"
              @update:modelValue="setMinMax"
            />
            <label for="chkMinMax">{{ t('settings.cardinalityMinMax') }}</label>
          </div>
        </div>
      </div>
    </div>
  </Dialog>
</template>
