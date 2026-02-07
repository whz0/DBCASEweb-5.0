<script setup lang="ts">

</script>

<template>
  <div class="h-full grid grid-cols-3">
    <div>
      <Button severity="secondary" class="bi bi-arrow-90deg-left" text />
      <Button severity="secondary" class="bi bi-arrow-90deg-right" text />
    </div>
    <div class="text-3xl">
      <h1>Esquema Físico</h1>
    </div>
    <div>
      <Button severity="secondary" class="bi bi-download" text />
      <Button severity="secondary" class="bi bi-x-lg" @click="$emit('close')"></Button>
    </div>
  </div>
</template>

<style scoped>

</style>
