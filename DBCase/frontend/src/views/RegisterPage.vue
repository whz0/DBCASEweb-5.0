<script setup lang="ts">
import { useToast } from 'primevue'
import { ref, type UnwrapRef } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRouter } from 'vue-router'

import { useAuthStore } from '@/stores/authStore'

const router = useRouter()
const { t } = useI18n()
const toast = useToast()
const { register, oauth2Login } = useAuthStore()

interface Credential {
  username: string
  password: string
}

const formData = ref<Credential>({
  username: '',
  password: '',
})
const confirmPassword = ref('')
const loading = ref(false)

const handleRegister = async (data: UnwrapRef<Credential>) => {
  if (data.password !== confirmPassword.value) {
    toast.add({ severity: 'error', detail: t('register.passwordMismatch'), life: 3000 })
    return
  }

  loading.value = true
  try {
    await register(data, (message, severity) =>
      toast.add({ severity, detail: message, life: 3000 }),
    )
    await router.replace('/')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="flex min-h-screen">
    <!-- Left Side: Visual/Branding (Hidden on mobile) -->
    <div
      class="hidden lg:flex lg:w-1/2 items-center justify-center p-12 bg-primary text-primary-contrast relative overflow-hidden"
    >
      <!-- Background -->
      <div class="absolute inset-0 opacity-10 pointer-events-none">
        <svg class="h-full w-full" viewBox="0 0 100 100" preserveAspectRatio="none">
          <defs>
            <pattern id="grid-register" width="10" height="10" patternUnits="userSpaceOnUse">
              <path d="M 10 0 L 0 0 0 10" fill="none" stroke="currentColor" stroke-width="0.5" />
            </pattern>
          </defs>
          <rect width="100" height="100" fill="url(#grid-register)" />
        </svg>
      </div>

      <div class="relative z-10 flex flex-col items-center text-center gap-8 max-w-md">
        <div class="p-6 bg-white/10 rounded-3xl backdrop-blur-md border border-white/20 shadow-2xl">
          <img src="@/assets/logo.png" alt="Logo" class="w-32" />
        </div>
        <div>
          <h1 class="text-5xl font-extrabold mb-4 tracking-tight">DBCASEweb 5.0</h1>
          <p class="text-xl opacity-90 font-light leading-relaxed">
            {{ t('register.subtitle') }}
          </p>
        </div>
      </div>
    </div>

    <!-- Right Side: Register Form -->
    <div class="w-full lg:w-1/2 flex items-center justify-center p-6 sm:p-12">
      <div class="w-full max-w-md">
        <!-- Mobile Logo -->
        <div class="flex flex-col items-center mb-8 lg:hidden">
          <img src="@/assets/logo.png" alt="Logo" class="w-16 mb-4" />
          <h1 class="text-3xl font-bold">DBCASEweb 5.0</h1>
        </div>

        <div class="mb-8 text-center lg:text-left">
          <h2 class="text-3xl font-bold mb-2">{{ t('register.title') }}</h2>
          <p class="text-muted-color">{{ t('register.subtitle') }}</p>
        </div>

        <Card
          class="rounded-3xl! border! border-(--p-button-outlined-secondary-border-color) shadow-none! overflow-hidden"
        >
          <template #content>
            <form @submit.prevent="handleRegister(formData)">
              <div class="mb-5 text-center">
                <label for="username" class="block text-sm font-semibold mb-2">
                  {{ t('register.username') }}
                </label>
                <InputText
                  id="username"
                  v-model="formData.username"
                  :placeholder="t('register.username')"
                  fluid
                  class="!rounded-xl !py-3 !px-4"
                  required
                />
              </div>

              <div class="mb-5 text-center">
                <label for="password" class="block text-sm font-semibold mb-2">
                  {{ t('register.password') }}
                </label>
                <Password
                  id="password"
                  v-model="formData.password"
                  :feedback="true"
                  :promptLabel="t('register.promptLabel')"
                  :weakLabel="t('register.weakLabel')"
                  :mediumLabel="t('register.mediumLabel')"
                  :strongLabel="t('register.strongLabel')"
                  toggleMask
                  :placeholder="t('register.password')"
                  fluid
                  input-class="!rounded-xl !py-3"
                  required
                />
              </div>

              <div class="mb-5 text-center">
                <label for="confirmPassword" class="block text-sm font-semibold mb-2">
                  {{ t('register.confirmPassword') }}
                </label>
                <Password
                  id="confirmPassword"
                  v-model="confirmPassword"
                  :feedback="false"
                  toggleMask
                  :placeholder="t('register.confirmPassword')"
                  fluid
                  input-class="!rounded-xl !py-3"
                  required
                />
              </div>

              <Button
                type="submit"
                :label="t('register.register')"
                :loading="loading"
                icon="bi bi-person-plus"
                fluid
                class="rounded-xl! py-3.5! font-bold! mt-2"
              />

              <Divider align="center" class="my-4!">
                <span class="text-xs uppercase font-bold tracking-widest text-muted-color px-3">
                  {{ t('login.or') }}
                </span>
              </Divider>

              <div class="grid grid-cols-2 gap-4">
                <Button
                  type="button"
                  @click="oauth2Login('google')"
                  label="Google"
                  icon="bi bi-google"
                  severity="secondary"
                  outlined
                  fluid
                  class="rounded-xl! py-3.5! text-sm!"
                />
                <Button
                  type="button"
                  @click="oauth2Login('github')"
                  label="GitHub"
                  icon="bi bi-github"
                  severity="secondary"
                  outlined
                  fluid
                  class="rounded-xl! py-3.5! text-sm!"
                />
              </div>
            </form>
          </template>
          <template #footer>
            <div class="pt-2 text-center">
              <p class="text-sm text-muted-color">
                {{ t('register.hasAccount') }}
                <router-link to="/login" class="font-bold text-primary hover:underline ml-1">
                  {{ t('register.signIn') }}
                </router-link>
              </p>
            </div>
          </template>
        </Card>
      </div>
    </div>
  </div>
</template>
