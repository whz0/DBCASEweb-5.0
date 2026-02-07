<script setup lang="ts">
  import {ref} from 'vue';
  import { useTheme } from '@/composables/useTheme';
  import Dropdown from 'primevue/dropdown';

  const visible = ref(false);
  const { theme, setTheme } = useTheme();

  const themeOptions = [
    { label: 'Light', value: 'light', icon: 'bi bi-sun' },
    { label: 'Dark', value: 'dark', icon: 'bi bi-moon' },
    { label: 'System', value: 'system', icon: 'bi bi-circle-half' }
  ];
</script>

<template>
  <Button severity="secondary" class="bi bi-universal-access-circle" @click="visible = true" text />

  <Dialog v-model:visible="visible" modal :dismissable-mask="true" :draggable="false" header="Settings" :style="{ width: '30rem' }">
    <div class="flex flex-col gap-4">
      <div class="flex items-center justify-between">
        <label>Theme</label>
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
