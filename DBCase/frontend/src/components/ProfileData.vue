<script setup lang="ts">
import { ref } from 'vue'
import { useI18n } from 'vue-i18n'

const count = ref(0)
const { t } = useI18n()
</script>

<template>
  <button @click="count++">{{ t('profile.clickMe', { count: count }) }}</button>
</template>

<style scoped></style>
