<script setup lang="ts">
import { ref } from 'vue'
import { useI18n } from 'vue-i18n'

const { t } = useI18n()
const help = ref('NO')
</script>

<template>
  <button @click="help + '---'">{{ help }} {{ t('common.needHelp') }}</button>
</template>

<style scoped></style>
