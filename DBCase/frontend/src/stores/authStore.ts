import { defineStore } from 'pinia'
import { ref } from 'vue'

import { i18n } from '@/i18n'
import http from '@/plugins/axios.ts'

interface User {
  username: string
  chart: string
}

export const useAuthStore = defineStore('auth', () => {
  const user = ref<User>({
    username: '',
    chart: '',
  })

  function init() {
    if (sessionStorage.getItem('username') != null) {
      user.value.username = sessionStorage.getItem('username') ?? ''
      user.value.chart = sessionStorage.getItem('chart') ?? ''
    }
  }

  async function login(
    credential: object,
    toast: (message: string, severity: 'error' | 'warn' | 'info' | 'success') => void,
  ) {
    return http
      .post('/user/login', credential)
      .then(async () => {
        const { data } = await http.get('/user/me')
        assignUser(data)
        toast(i18n.global.t('login.success'), 'success')
      })
      .catch((error) => {
        let message
        let severity: 'error' | 'warn' | 'info' = 'error'

        if (error.response) {
          message = error.response.data?.message || 'Error de autenticación'
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
    return http
      .get('/user/me')
      .then(({ data }) => {
        assignUser(data)
      })
      .catch(() => {
        assignUser()
      })
  }

  async function oauth2Login(provider: string) {
    window.location.href = `http://localhost:8080/oauth2/authorization/${provider}`
  }

  async function register(
    credential: object,
    toast: (message: string, severity: 'error' | 'warn' | 'info' | 'success') => void,
  ) {
    return http
      .post('/user/register', credential)
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
    return http.post('/user/logout').then(() => {
      assignUser()
    })
  }

  function assignUser(u: User | null = null) {
    if (u != null) {
      const username: keyof typeof u = 'username'
      const chart: keyof typeof u = 'chart'
      user.value.username = u[username]
      user.value.chart = u[chart]
    } else {
      user.value.username = ''
      user.value.chart = ''
    }
    sessionStorage.setItem('username', user.value.username)
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
  }
})
