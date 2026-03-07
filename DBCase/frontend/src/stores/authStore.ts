import { defineStore } from 'pinia'
import {ref} from "vue";
import http from "@/plugins/axios.ts"

interface User {
  username: string
}

export const useAuthStore = defineStore('auth', ()=> {

  const user = ref(null)
  async function login(
    credential: Object,
    onError: (message: string, severity: 'error' | 'warn' | 'info') => void
  ) {
    http
      .post('/user/login', credential)
      .then(() => {
        http.get('/user/me')
          .then(({data}) => {
            user.value = data
            let key: keyof typeof data = 'username'
            console.log(data[key])
            localStorage.setItem('user', data)
            window.location.replace('/')
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

        onError(message, severity);
      })
  }

    async function validateToken() {
      return http.get('/user/me')
        .then(({data}) => {
          localStorage.setItem('user', data)
        })
        .catch(() => {
          localStorage.removeItem('user')
        })
    }

    async function oauth2Login() {
      return http.post('/login/oauth2/code/github')
        .then(({data}) => {
          localStorage.setItem('user', data)
        })
        .catch(() => {
          localStorage.removeItem('user')
        })
    }

    async function logout() {
      return http.post('/user/logout')
        .then(() => {
          localStorage.removeItem('user')
        })

    }

  return {
    user,
    login,
    logout,
    validateToken,
    oauth2Login,
  }
})
