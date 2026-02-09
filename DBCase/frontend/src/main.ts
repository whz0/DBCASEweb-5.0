import { createApp } from 'vue'
import App from './App.vue'
import PrimeVue from 'primevue/config'
import router from './router'
import { i18n } from './i18n'
import { createPinia } from 'pinia'

import 'bootstrap-icons/font/bootstrap-icons.min.css'

import Aura from '@primeuix/themes/aura'

const app = createApp(App)
const pinia = createPinia()

app.use(PrimeVue, {
  theme: {
    preset: Aura,
    options: {
      darkModeSelector: '.dark'
    }
  }
})

app.use(router)
app.use(i18n)
app.use(pinia)

app.mount('#app')
