<script setup lang="ts">
import ErrorTemplate from '@/components/ErrorTemplate.vue';
</script>

<template>
  <ErrorTemplate 
    code="404"
    title="Page Not Found"
    message="The page you're looking for doesn't exist or has been moved."
    icon="bi bi-exclamation-triangle"
    iconColor="text-orange-500"
    severity="warn"
  />
</template>
