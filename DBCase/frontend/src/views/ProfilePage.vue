<script setup lang="ts">
import {useAuthStore} from "@/stores/authStore.ts";

const {validateToken} = useAuthStore()

const handlerequest = () => {
  console.log('Hola')
  console.log(validateToken())
}
</script>

<template>
  <h1>Hola</h1>
  <Button @click="handlerequest" />
</template>

<style scoped>

</style>
