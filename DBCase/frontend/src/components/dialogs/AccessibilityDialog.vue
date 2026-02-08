<script setup lang="ts">
  import {ref, computed} from 'vue';
  import { useTheme } from '@/composables/useTheme';
  import { useI18n } from 'vue-i18n';
  import Dropdown from 'primevue/dropdown';

  const visible = ref(false);
  const { theme, setTheme } = useTheme();
  const { locale, t } = useI18n();

  const themeOptions = computed(() => [
    { label: t('settings.light'), value: 'light', icon: 'bi bi-sun' },
    { label: t('settings.dark'), value: 'dark', icon: 'bi bi-moon' },
    { label: t('settings.system'), value: 'system', icon: 'bi bi-circle-half' }
  ]);

  const languageOptions = computed(() => [
    { label: t('settings.english'), value: 'en' },
    { label: t('settings.spanish'), value: 'es' }
  ]);

  const changeLanguage = (lang: string) => {
    locale.value = lang;
    localStorage.setItem('locale', lang);
  };
</script>

<template>
  <Button severity="secondary" class="bi bi-universal-access-circle" @click="visible = true" text v-tooltip.bottom="t('toolbar.settings')" />

  <Dialog v-model:visible="visible" modal :dismissable-mask="true" :draggable="false" :header="t('settings.title')" :style="{ width: '30rem' }">
    <div class="flex flex-col gap-4">
      <div class="flex items-center justify-between">
        <label>{{ t('settings.language') }}</label>
        <Dropdown
          :modelValue="locale"
          @update:modelValue="changeLanguage"
          :options="languageOptions"
          optionLabel="label"
          optionValue="value"
        />
      </div>
      <div class="flex items-center justify-between">
        <label>{{ t('settings.theme') }}</label>
        <Dropdown
          :modelValue="theme"
          @update:modelValue="setTheme"
          :options="themeOptions"
          optionLabel="label"
          optionValue="value"
        >
          <template #value="{ value }">
            <i :class="themeOptions.find(o => o.value === value)?.icon"></i>
            <span class="ml-2">{{ themeOptions.find(o => o.value === value)?.label }}</span>
          </template>
          <template #option="{ option }">
            <i :class="option.icon"></i>
            <span class="ml-2">{{ option.label }}</span>
          </template>
        </Dropdown>
      </div>
    </div>
  </Dialog>
</template>

<style scoped>

</style>
