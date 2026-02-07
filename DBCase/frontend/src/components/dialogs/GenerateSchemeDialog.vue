<script setup lang="ts">
import {ref} from 'vue';

const visible = ref(false);

defineEmits(['generateER', 'generateLogical', 'generateDB'])

</script>

<template>
  <Button severity="secondary" class="bi bi-magic" @click="visible = true" text />

  <Dialog v-model:visible="visible"  modal header="Generar esquema" :style="{ width: '50rem' }" :breakpoints="{ '1199px': '75vw', '575px': '90vw' }">
    <div class="grid gap-2 grid-rows-3">
      <Button @click="$emit('generateER', true)">Esquema ER</Button>
      <Button @click="$emit('generateLogical', true)">Esquema lógico</Button>
      <Button @click="$emit('generateDB', true)">Esquema físico</Button>
    </div>
  </Dialog>
</template>

<script lang="ts">

</script>

<style scoped>

</style>
