<script setup lang="ts">
import {ref} from 'vue';

const menu = ref();
const items = ref([
  {label: 'Insertar una nueva entidad', icon: 'bi bi-square'},
  {label: 'Insertar una nueva relación', icon: 'bi bi-diagram-3'},
  {label: 'Insertar una nueva relación IsA', icon: 'bi bi-diagram-2'},
  {label: 'Crear dominio', icon: 'bi bi-collection'},
]);

const onRightClick = (e: Event) => {
  menu.value.show(e);
}
</script>

<template>
  <aside class="col-md-6 h-100" @contextmenu="onRightClick" aria-haspopup="true">
    <h2>Esquema Conceptual</h2>
    <button>Atrás</button>
    <button>Adelante</button>
    <ContextMenu ref="menu" :model="items" />
  </aside>
</template>

<script lang="ts">

</script>

<!-- Add "scoped" attribute to limit CSS to this component only -->
<style scoped>
  aside {
    border-style: solid;
    border-width: 2px;
    border-color: hsl(128, 50%, 50%);
  }

  ContextMenu {
    background-color: white;
  }
</style>
