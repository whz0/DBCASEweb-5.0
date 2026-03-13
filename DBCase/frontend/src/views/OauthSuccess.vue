<script setup lang="ts">
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/authStore'
import { computed } from 'vue'
import { useToast } from 'primevue'

const router = useRouter()
const route = useRoute()
const auth = useAuthStore()
const toast = useToast()
const provider = computed(() => {
  const p = route.query.provider as string
  if (!p) return ''
  return p.charAt(0).toUpperCase() + p.slice(1).toLowerCase()
})

const handleContinue = async () => {
  await auth.validateToken()
  if (!auth.user) await router.push('/login')
  else {
    toast.add({
      severity: 'success',
      detail: `Login con éxito con ${provider.value}`,
      life: 3000
    })
    await router.push('/')
  }
}
</script>

<template>
  <div class="min-h-screen flex items-center justify-center">
    <div class="text-center px-6">
      <div
        class="w-16 h-16 rounded-full bg-green-200 flex items-center justify-center mx-auto mb-6"
      >
        <i class="bi bi-check-lg text-green-500 text-2xl"></i>
      </div>

      <h1 class="text-2xl font-semibold mb-2">¡Todo listo!</h1>
      <p class="mb-8">Cuenta de {{ provider || 'OAuth2' }} vinculada</p>

      <Button @click="handleContinue" class="btn">
        <i class="bi bi-arrow-right me-2"></i>
        Ir al panel
      </Button>
    </div>
  </div>
</template>

<script lang="ts"></script>

<style scoped></style>
