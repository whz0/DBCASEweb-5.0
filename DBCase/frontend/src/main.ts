import 'bootstrap-icons/font/bootstrap-icons.min.css'

import Aura from '@primeuix/themes/aura'
import { createPinia } from 'pinia'
import { ToastService } from 'primevue'
import PrimeVue from 'primevue/config'
import { createApp } from 'vue'
import VueKonva from 'vue-konva'

import { useAuthStore } from '@/stores/authStore.ts'

import App from './App.vue'
import { i18n } from './i18n'
import router from './router'

const app = createApp(App)
const pinia = createPinia()

app.use(ToastService)
app.use(PrimeVue, {
  theme: {
    preset: Aura,
    options: {
      darkModeSelector: '.dark',
    },
  },
})

app.use(router)
app.use(i18n)
app.use(pinia)
app.use(VueKonva)

const { init } = useAuthStore()
init()

app.mount('#app')
