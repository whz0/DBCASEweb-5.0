<script setup lang="ts">
  import {ref} from 'vue'

  const help = ref('NO')
</script>

<template>
  <button @click="help + '---'"> {{help}} lo necesito</button>
</template>

<style scoped>

</style>
