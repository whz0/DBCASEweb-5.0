import { defineStore } from 'pinia'
import { ref } from 'vue'

import { useTheme } from '@/composables/useTheme'
import { i18n } from '@/i18n'
import { api } from '@/plugins/axios.ts'
import { useDiagramStore } from '@/stores/diagramStore'
import { useDialogStore } from '@/stores/dialogStore'
import { useErSchemaStore } from '@/stores/erSchemaStore'
import { useGeneratePanelStore } from '@/stores/generatePanelStore'
import type { UserSettings } from '@/types/api'

const theme = useTheme()

interface User {
  username: string
  chart: string
  pictureUrl: string
  settings?: UserSettings
}

export const useAuthStore = defineStore('auth', () => {
  const user = ref<User>({
    username: '',
    chart: '',
    pictureUrl: '',
  })

  function init() {
    if (sessionStorage.getItem('username') != null) {
      user.value.username = sessionStorage.getItem('username') ?? ''
      user.value.chart = sessionStorage.getItem('chart') ?? ''
      user.value.pictureUrl = sessionStorage.getItem('pictureUrl') ?? ''
    }
  }

  async function login(
    credential: object,
    toast: (message: string, severity: 'error' | 'warn' | 'info' | 'success') => void,
  ) {
    return api.user
      .login(credential)
      .then(async () => {
        const { data } = await api.user.getMe()
        assignUser(data)
        toast(i18n.global.t('login.success'), 'success')
      })
      .catch((error) => {
        let message
        let severity: 'error' | 'warn' | 'info' = 'error'

        if (error.response) {
          message = error.response.data?.message || error.response.data || 'Error de autenticación'
          severity = error.response.status >= 500 ? 'error' : 'warn'
        } else if (error.request) {
          message = 'No se pudo contactar con el servidor'
          severity = 'error'
        } else {
          message = error.message
        }
        toast(message, severity)
      })
  }

  async function validateToken() {
    return api.user
      .getMe()
      .then(({ data }) => {
        assignUser(data)
      })
      .catch(() => {
        assignUser()
      })
  }

  async function oauth2Login(provider: string) {
    window.location.href = `${import.meta.env.VITE_BACKEND_URL}/oauth2/authorization/${provider}`
  }

  async function register(
    credential: object,
    toast: (message: string, severity: 'error' | 'warn' | 'info' | 'success') => void,
  ) {
    return api.user
      .register(credential)
      .then(({ data }) => {
        assignUser(data)
        toast(i18n.global.t('register.success'), 'success')
      })
      .catch((error) => {
        let message = i18n.global.t('register.error')
        if (
          error.response?.data?.message?.includes('ya existe') ||
          error.response?.data?.message?.includes('already exists')
        ) {
          message = i18n.global.t('register.userExists')
        }
        toast(message, 'error')
        throw error
      })
  }

  async function logout() {
    try {
      await api.user.logout()
    } catch (error) {
      console.error('Logout API failed:', error)
    } finally {
      assignUser()
      useDiagramStore().reset()
      useErSchemaStore().reset()
      useDialogStore().reset()
      useGeneratePanelStore().reset()
    }
  }

  function assignUser(u: User | null = null) {
    if (u != null) {
      user.value.username = u.username
      user.value.chart = u.chart
      user.value.pictureUrl = u.pictureUrl
      user.value.settings = u.settings

      if (u.settings?.language) {
        i18n.global.locale = u.settings.language as 'en' | 'es'
        localStorage.setItem('locale', u.settings.language)
      }
      if (u.settings?.theme) {
        theme.setTheme(u.settings.theme as 'light' | 'dark' | 'system')
      }
    } else {
      user.value.username = ''
      user.value.chart = ''
      user.value.pictureUrl = ''
      user.value.settings = undefined
      i18n.global.locale = 'en'
      localStorage.setItem('locale', 'en')
      theme.setTheme('system')
    }
    sessionStorage.setItem('username', user.value.username)
    sessionStorage.setItem('chart', user.value.chart)
    sessionStorage.setItem('pictureUrl', user.value.pictureUrl)
  }

  function setChart(chart: string) {
    user.value.chart = chart
    sessionStorage.setItem('chart', user.value.chart)
  }

  return {
    user,
    init,
    login,
    logout,
    validateToken,
    oauth2Login,
    register,
    setChart,
  }
})
