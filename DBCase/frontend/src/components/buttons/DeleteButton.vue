<script setup lang="ts">
defineProps({
  label: String,
})

const emits = defineEmits(['delete'])
</script>

<template>
  <Button
    severity="secondary"
    class="bi bi-trash"
    @click="emits('delete')"
    v-tooltip.bottom="label"
    text
  />
</template>

<style scoped></style>
