<script setup lang="ts">
import { useToast } from 'primevue'
import { ref, type UnwrapRef } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRouter } from 'vue-router'

import { useAuthStore } from '@/stores/authStore'

const router = useRouter()
const { t } = useI18n()
const toast = useToast()
const { register } = useAuthStore()

interface Credential {
  username: string
  password: string
}

const formData = ref<Credential>({
  username: '',
  password: '',
})
const confirmPassword = ref('')

const handleRegister = async (formData: UnwrapRef<Credential>) => {
  if (formData.password !== confirmPassword.value) {
    toast.add({ severity: 'error', detail: t('register.passwordMismatch'), life: 3000 })
    return
  }

  await register(formData, (message, severity) =>
    toast.add({ severity, detail: message, life: 3000 }),
  )
  await router.replace('/')
}
</script>

<template>
  <div class="flex items-center justify-center min-h-screen p-4">
    <Card class="w-full max-w-sm border-2 border-gray-200 shadow-xl">
      <template #title>
        <div class="flex flex-col items-center gap-2 pt-4">
          <img src="@/assets/logo.png" alt="Logo" class="w-16" />
          <div class="text-2xl font-bold text-primary">{{ t('register.title') }}</div>
          <div class="text-sm font-normal text-muted-color">{{ t('register.subtitle') }}</div>
        </div>
      </template>
      <template #content>
        <form @submit.prevent="handleRegister(formData)" class="flex flex-col gap-4">
          <div class="flex flex-col gap-1">
            <label for="username" class="text-sm font-semibold">{{ t('register.username') }}</label>
            <InputGroup>
              <InputGroupAddon>
                <i class="bi bi-person" />
              </InputGroupAddon>
              <InputText
                id="username"
                v-model="formData.username"
                :placeholder="t('register.username')"
                required
                fluid
              />
            </InputGroup>
          </div>
          <div class="flex flex-col gap-1">
            <label for="password" class="text-sm font-semibold">{{ t('register.password') }}</label>
            <InputGroup>
              <InputGroupAddon>
                <i class="bi bi-lock" />
              </InputGroupAddon>
              <Password
                id="password"
                v-model="formData.password"
                :placeholder="t('register.password')"
                required
                fluid
                :feedback="false"
                toggleMask
              />
            </InputGroup>
          </div>
          <div class="flex flex-col gap-1">
            <label for="confirmPassword" class="text-sm font-semibold">{{
              t('register.confirmPassword')
            }}</label>
            <InputGroup>
              <InputGroupAddon>
                <i class="bi bi-lock-fill" />
              </InputGroupAddon>
              <Password
                id="confirmPassword"
                v-model="confirmPassword"
                :placeholder="t('register.confirmPassword')"
                required
                fluid
                :feedback="false"
                toggleMask
              />
            </InputGroup>
          </div>
          <Button type="submit" :label="t('register.register')" severity="primary" fluid />
        </form>
      </template>
      <template #footer>
        <div class="text-center text-sm text-muted-color pb-2">
          {{ t('register.hasAccount') }}
          <a
            @click="$router.push('/login')"
            class="text-primary hover:underline font-semibold cursor-pointer"
          >
            {{ t('register.signIn') }}
          </a>
        </div>
      </template>
    </Card>
  </div>
</template>
