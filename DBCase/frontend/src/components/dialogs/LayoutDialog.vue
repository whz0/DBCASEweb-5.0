<script setup lang="ts">
import { ref } from 'vue';
import Dialog from 'primevue/dialog';
import Button from 'primevue/button';
import { useLayout } from '@/composables/useLayout';

const visible = ref(false);
const { setLayout } = useLayout();

const layouts = [
  { label: 'Horizontal', value: 'horizontal', icon: 'bi bi-layout-split' },
  { label: 'Vertical', value: 'vertical', icon: 'bi bi-layout-three-columns' }
];

const selectLayout = (layout: 'horizontal' | 'vertical') => {
  setLayout(layout);
  visible.value = false;
};

defineExpose({
  visible
});
</script>

<template>
  <Button severity="secondary" class="bi bi-grid-1x2" @click="visible = true" text v-tooltip.bottom="'Layout'" />

  <Dialog v-model:visible="visible" modal header="Choose Layout" :style="{ width: '30rem' }">
    <div class="flex flex-col gap-4">
      <div class="flex gap-4 justify-center">
        <Button 
          v-for="layout in layouts" 
          :key="layout.value"
          :icon="layout.icon"
          :label="layout.label"
          @click="selectLayout(layout.value as any)"
          severity="secondary"
          outlined
          class="flex-1"
        />
      </div>
    </div>
  </Dialog>
</template>
