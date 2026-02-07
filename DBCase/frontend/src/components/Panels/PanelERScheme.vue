<script setup lang="ts">
import {ref} from 'vue';
import { useDialogs } from '@/composables/useDialogs.ts';
import GenerateSchemeDialog from "@/components/Dialog/GenerateSchemeDialog.vue";

const menu = ref();
const { openAddEntityDialog } = useDialogs();
const items = ref([
  {
    label: 'Insertar una nueva entidad',
    icon: 'bi bi-square',
    command: openAddEntityDialog
  },
  {label: 'Insertar una nueva relación', icon: 'bi bi-diagram-3'},
  {label: 'Insertar una nueva relación IsA', icon: 'bi bi-diagram-2'},
  {label: 'Crear dominio', icon: 'bi bi-collection'},
]);

const onRightClick = (e: Event) => {
  menu.value.show(e);
}
</script>

<template>
  <div @contextmenu="onRightClick" aria-haspopup="true" class="h-full grid grid-cols-3">
    <div>
      <Button severity="secondary" class="bi bi-arrow-90deg-left" text />
      <Button severity="secondary" class="bi bi-arrow-90deg-right" text />
    </div>
    <div class="text-3xl">
      <h1>Esquema Conceptual</h1>
    </div>
    <div>
      <Button severity="secondary" class="bi bi-download" text />
      <Button severity="secondary" class="bi bi-x-lg" @click="$emit('close')"></Button>
    </div>
    <ContextMenu ref="menu" :model="items" />
  </div>
</template>

<script lang="ts">

</script>

<style scoped>

</style>
