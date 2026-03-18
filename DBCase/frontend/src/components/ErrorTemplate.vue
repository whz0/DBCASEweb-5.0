<script setup lang="ts">
import { useI18n } from 'vue-i18n'
import { useRouter } from 'vue-router'

const { t } = useI18n()

defineProps<{
  code: string
  title: string
  message: string
  icon: string
  iconColor: string
  severity: 'warn' | 'error'
}>()

const router = useRouter()
</script>

<template>
  <div class="flex flex-col items-center justify-center h-full gap-6 p-8 animate-fade-in">
    <i :class="[icon, 'text-9xl', iconColor]"></i>
    <h1 class="text-6xl font-bold">{{ code }}</h1>
    <Message :severity="severity" :closable="false">
      <span class="text-xl">{{ title }}</span>
    </Message>
    <p class="text-lg">{{ message }}</p>
    <div class="flex gap-3">
      <Button :label="t('common.goHome')" icon="bi bi-house" @click="router.push('/')" />
      <Button
        :label="t('common.goBack')"
        icon="bi bi-arrow-left"
        severity="secondary"
        @click="router.back()"
      />
    </div>
  </div>
</template>

<style scoped></style>
