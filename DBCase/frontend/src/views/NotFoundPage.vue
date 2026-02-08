<script setup lang="ts">
import { useI18n } from 'vue-i18n';
import ErrorTemplate from '@/components/ErrorTemplate.vue';

const { t } = useI18n();
</script>

<template>
  <ErrorTemplate 
    :code="t('errors.404')"
    :title="t('errors.pageNotFound')"
    :message="t('errors.pageNotFoundMessage')"
    icon="bi bi-exclamation-triangle"
    iconColor="text-orange-500"
    severity="warn"
  />
</template>
