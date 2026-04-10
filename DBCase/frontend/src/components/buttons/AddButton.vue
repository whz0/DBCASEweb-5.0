<script setup lang="ts">
defineProps({
  label: String,
})

const emits = defineEmits(['add'])
</script>

<template>
  <Button
    severity="secondary"
    class="bi bi-plus-lg"
    @click="emits('add')"
    v-tooltip.bottom="label"
    text
  />
</template>

<style scoped></style>
