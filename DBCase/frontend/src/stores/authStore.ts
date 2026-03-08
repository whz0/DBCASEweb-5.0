import { defineStore } from 'pinia'
import {ref} from "vue";
import http from "@/plugins/axios.ts"

interface User {
  username: string
  chart: string
}

export const useAuthStore = defineStore('auth', ()=> {

  const user = ref<User>({
    username: '',
    chart: '',
  })

  function init() {
    if(sessionStorage.getItem('username') != null) {
      user.value.username = sessionStorage.getItem('username') ?? ''
      user.value.chart = sessionStorage.getItem('chart') ?? ''
    }
  }

  async function login(
    credential: Object,
    toast: (message: string, severity: 'error' | 'warn' | 'info' | 'success') => void
  ) {
    return http
      .post('/user/login', credential)
      .then(() => {
        return http.get('/user/me')
          .then(({data}) => {
            assignUser(data)
            toast('Login exitoso', 'success')
          })
      })
      .catch((error) => {
        let message;
        let severity: 'error' | 'warn' | 'info' = 'error';

        if (error.response) {
          message = error.response.data?.message || 'Error de autenticación';
          severity = error.response.status >= 500 ? 'error' : 'warn';
        } else if (error.request) {
          message = 'No se pudo contactar con el servidor';
          severity = 'error';
        } else {
          message = error.message;
        }
        toast(message, severity);
      })
  }

  async function validateToken() {
    return http.get('/user/me')
      .then(({data}) => {
        assignUser(data)
      })
      .catch(() => {
        assignUser()
      })
  }

  async function oauth2Login() {
    window.location.href = 'http://localhost:8080/oauth2/authorization/github';
  }

  async function logout() {
    return http.post('/user/logout')
      .then(() => {
        assignUser()
      })
  }

  function assignUser(u: User | null = null) {
    if(u != null) {
      let username: keyof typeof u = 'username'
      let chart: keyof typeof u = 'chart'
      user.value.username = u[username]
      user.value.chart = u[chart]
    }
    else {
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
  }
})
