<script setup lang="ts">
import ErrorTemplate from '@/components/ErrorTemplate.vue';
</script>

<template>
  <ErrorTemplate 
    code="500"
    title="Internal Server Error"
    message="Something went wrong on our end. Please try again later."
    icon="bi bi-x-octagon"
    iconColor="text-red-500"
    severity="error"
  />
</template>
