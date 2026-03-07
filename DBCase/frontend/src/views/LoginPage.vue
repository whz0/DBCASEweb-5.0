<script setup lang="ts">
import { ref } from "vue";
import { useI18n } from "vue-i18n";
import { useToast } from "primevue";
import { useAuthStore } from "@/stores/authStore.ts";

const { t } = useI18n();
const toast = useToast();

const { login, oauth2Login } = useAuthStore()

interface User {
  username: string,
  password: string,
  chart: string
}

const formData = ref<User>({
  username: '',
  password: '',
  chart: ''
})
</script>

<template>
  <div class="flex items-center justify-center min-h-screen p-4">
    <Card class="w-full max-w-sm border-2 border-gray-200 shadow-xl">
      <template #title>
        <div class="flex flex-col items-center gap-2 pt-4">
          <img src="@/assets/logo.png" alt="Logo" class="w-16" />
          <div class="text-2xl font-bold text-primary">{{ t('login.title') }}</div>
          <div class="text-sm font-normal text-muted-color">{{ t('login.subtitle') }}</div>
        </div>
      </template>
      <template #content>
        <form @submit.prevent="login(formData, (message, severity) =>
        toast.add({ severity: severity, detail: message, life: 3000 }))" class="flex flex-col gap-4">
          <div class="flex flex-col gap-1">
            <label for="username" class="text-sm font-semibold">{{ t('login.username') }}</label>
            <InputGroup>
              <InputGroupAddon>
                <i class="bi bi-person" />
              </InputGroupAddon>
              <InputText id="username" v-model="formData.username" :placeholder="t('login.username')" fluid />
            </InputGroup>
          </div>

          <div class="flex flex-col gap-1">
            <label for="password" class="text-sm font-semibold">{{ t('login.password') }}</label>
            <InputGroup>
              <InputGroupAddon>
                <i class="bi bi-lock" />
              </InputGroupAddon>
              <Password id="password" v-model="formData.password" :feedback="false" toggleMask :placeholder="t('login.password')" fluid />
            </InputGroup>
          </div>

          <Button type="submit" :label="t('login.login')" size="large" fluid />

          <Divider align="center">
            <span class="text-xs text-muted-color">{{ t('login.or') }}</span>
          </Divider>

          <Button
            :label="t('login.google')"
            icon="bi bi-google"
            severity="danger"
            outlined fluid />
          <Button
            as="a"
            @click="oauth2Login"
            :label="t('login.github')"
            icon="bi bi-github"
            severity="contrast"
            fluid />
        </form>
      </template>
      <template #footer>
        <div class="text-center text-sm text-muted-color pb-2">
          {{ t('login.noAccount') }}
          <a href="#" class="text-primary hover:underline font-semibold">{{ t('login.createAccount') }}</a>
        </div>
      </template>
    </Card>
  </div>
</template>

<style lang="ts" scoped>

</style>
