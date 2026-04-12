<script setup lang="ts">
import { useI18n } from 'vue-i18n'

const { t } = useI18n()

defineProps<{
  title: string
  authors: string[]
}>()
</script>

<template>
  <h3 class="text-xl font-medium">{{ title }}</h3>
  <h3 class="text-lg font-normal">{{ t('aboutUs.author') }}</h3>
  <p class="font-light" v-for="(author, index) in authors" :key="index">{{ author }}</p>
  <Divider />
</template>

<style scoped></style>
