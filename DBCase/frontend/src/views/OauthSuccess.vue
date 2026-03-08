<script setup lang="ts">
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/authStore'

const router = useRouter()
const auth = useAuthStore()

const handleContinue = async () => {
  await auth.validateToken()
  if (!auth.user) await router.push('/login')
  else await router.push('/')
}

</script>

<template>
  <div class="min-h-screen flex items-center justify-center bg-gray-50">
    <div class="text-center px-6">

      <div class="w-16 h-16 rounded-full bg-green-100 flex items-center justify-center mx-auto mb-6">
        <i class="bi bi-check-lg text-green-500 text-2xl"></i>
      </div>

      <h1 class="text-2xl font-semibold text-gray-800 mb-2">¡Todo listo!</h1>
      <p class="text-gray-400 mb-8">Cuenta de github vinculado</p>

      <button @click="handleContinue" class="btn">
        <i class="bi bi-arrow-right me-2"></i>
        Ir al panel
      </button>

    </div>
  </div>
</template>

<script lang="ts">

</script>

<style scoped>
.btn {
  background: #111827;
  color: white;
  padding: 12px 32px;
  border-radius: 8px;
  border: none;
  cursor: pointer;
  font-size: 0.95rem;
  transition: background 0.2s, opacity 0.2s;
}
.btn:hover { background: #374151; }
.btn:disabled { opacity: 0.6; cursor: not-allowed; }
</style>
