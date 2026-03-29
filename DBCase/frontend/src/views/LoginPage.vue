<script setup lang="ts">
import { useToast } from 'primevue'
import { ref, type UnwrapRef } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRouter } from 'vue-router'

import { useAuthStore } from '@/stores/authStore.ts'

const router = useRouter()
const { t } = useI18n()
const toast = useToast()

const { login, oauth2Login } = useAuthStore()

interface Credential {
  username: string
  password: string
}

const formData = ref<Credential>({
  username: '',
  password: '',
})

const loading = ref(false)

const handleLogin = async (data: UnwrapRef<Credential>) => {
  loading.value = true
  try {
    await login(data, (message, severity) =>
      toast.add({ severity: severity, detail: message, life: 3000 }),
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
      <!-- Decorative Background Pattern -->
      <div class="absolute inset-0 opacity-10 pointer-events-none">
        <svg class="h-full w-full" viewBox="0 0 100 100" preserveAspectRatio="none">
          <defs>
            <pattern id="grid" width="10" height="10" patternUnits="userSpaceOnUse">
              <path d="M 10 0 L 0 0 0 10" fill="none" stroke="currentColor" stroke-width="0.5" />
            </pattern>
          </defs>
          <rect width="100" height="100" fill="url(#grid)" />
        </svg>
      </div>

      <div class="relative z-10 flex flex-col items-center text-center gap-8 max-w-md">
        <div class="p-6 bg-white/10 rounded-3xl backdrop-blur-md border border-white/20 shadow-2xl">
          <img src="@/assets/logo.png" alt="Logo" class="w-32" />
        </div>
        <div>
          <h1 class="text-5xl font-extrabold mb-4 tracking-tight">DBCase 5.0</h1>
          <p class="text-xl opacity-90 font-light leading-relaxed">
            {{ t('login.subtitle') }}
          </p>
        </div>
      </div>
    </div>

    <!-- Right Side: Login Form -->
    <div class="w-full lg:w-1/2 flex items-center justify-center p-6 sm:p-12">
      <div class="w-full max-w-md">
        <!-- Mobile Logo -->
        <div class="flex flex-col items-center mb-8 lg:hidden">
          <img src="@/assets/logo.png" alt="Logo" class="w-16 mb-4" />
          <h1 class="text-3xl font-bold">DBCase 5.0</h1>
        </div>

        <div class="mb-8 text-center lg:text-left">
          <h2 class="text-3xl font-bold mb-2">{{ t('login.title') }}</h2>
          <p class="text-muted-color">{{ t('login.subtitle') }}</p>
        </div>

        <Card
          class="rounded-3xl! border! border-(--p-button-outlined-secondary-border-color) shadow-none! overflow-hidden"
        >
          <template #content>
            <form @submit.prevent="handleLogin(formData)">
              <div class="mb-6 text-center">
                <label for="username" class="block text-sm font-semibold mb-2">
                  {{ t('login.username') }}
                </label>
                <InputText
                  id="username"
                  v-model="formData.username"
                  :placeholder="t('login.username')"
                  fluid
                  class="!rounded-xl !py-3 !px-4"
                  required
                />
              </div>

              <div class="mb-6 text-center">
                <label for="password" class="block text-sm font-semibold mb-2">
                  {{ t('login.password') }}
                </label>
                <Password
                  id="password"
                  v-model="formData.password"
                  :feedback="false"
                  toggleMask
                  :placeholder="t('login.password')"
                  fluid
                  input-class="!rounded-xl !py-3"
                  required
                />
              </div>

              <Button
                type="submit"
                :label="t('login.login')"
                :loading="loading"
                icon="bi bi-box-arrow-in-right"
                fluid
                class="rounded-xl! py-3.5! font-bold!"
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
                {{ t('login.noAccount') }}
                <router-link to="/register" class="font-bold text-primary hover:underline ml-1">
                  {{ t('login.createAccount') }}
                </router-link>
              </p>
            </div>
          </template>
        </Card>
      </div>
    </div>
  </div>
</template>

<style lang="ts" scoped></style>
