<script setup>
  import {ref} from 'vue'

  const help = ref('NO')
</script>

<template>
  <button @click="help = 'SI' ? help === 'NO' : 'SI'"> {{help}} lo necesito</button>
</template>

<style scoped>

</style>