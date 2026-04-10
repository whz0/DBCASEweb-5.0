<script setup lang="ts">
defineProps({
  label: String,
  height: String,
  items: Array,
})
</script>

<template>
  <ScrollPanel :class="['w-full', `${height}`, 'border', 'border-solid', 'rounded-md', 'm-2']">
    <div class="stiky top-0.5 flex">
      <h2 class="m-1 text-left text-2xl">
        {{ label }}
      </h2>
    </div>
    <p class="text-left m-1">
      {{ items }}
    </p>
  </ScrollPanel>
</template>

<style scoped></style>
