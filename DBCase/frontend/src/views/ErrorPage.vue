<script setup lang="ts">
import { useI18n } from 'vue-i18n'

import ErrorTemplate from '@/components/ErrorTemplate.vue'

const { t } = useI18n()
</script>

<template>
  <ErrorTemplate
    :code="t('errors.500')"
    :title="t('errors.internalServerError')"
    :message="t('errors.internalServerErrorMessage')"
    icon="bi bi-x-octagon"
    iconColor="text-red-500"
    severity="error"
  />
</template>
