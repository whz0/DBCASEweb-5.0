import { createRouter, createWebHistory } from 'vue-router'
import MainPage from '@/views/MainPage.vue'
import ProfilePage from '@/views/ProfilePage.vue'
import HelpPage from '@/views/HelpPage.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {path: '/', component: MainPage},
    {path: '/help', component: HelpPage},
    {path: '/profile', component: ProfilePage}
  ],
})

export default router
