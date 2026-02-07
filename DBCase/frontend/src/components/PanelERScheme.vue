<script setup lang="ts">
import {ref} from 'vue';
import { useDialogs } from '@/composables/useDialogs';

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
  <aside @contextmenu="onRightClick" aria-haspopup="true" class="h-full">
    <h2>Esquema Conceptual</h2>
    <Button severity="secondary" class="bi bi-arrow-90deg-left" text />
    <Button severity="secondary" class="bi bi-arrow-90deg-right" text />
    <ContextMenu ref="menu" :model="items" />
  </aside>
</template>

<script lang="ts">

</script>

<style scoped>

</style>
