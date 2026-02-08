import { createApp } from 'vue'
import App from './App.vue'
import PrimeVue from 'primevue/config'
import router from './router'
import { i18n } from './i18n'

import 'bootstrap-icons/font/bootstrap-icons.min.css'

import Aura from '@primeuix/themes/aura'

const app = createApp(App)

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

app.mount('#app')
