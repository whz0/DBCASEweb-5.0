import { createRouter, createWebHistory } from 'vue-router'
import MainPage from '@/views/MainPage.vue'
import NotFoundPage from '@/views/NotFoundPage.vue'
import ErrorPage from '@/views/ErrorPage.vue'
import LoginPage from "@/views/LoginPage.vue";

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {path: '/login', component: LoginPage},
    {path: '/', component: MainPage},
    {path: '/error', component: ErrorPage},
    {path: '/:pathMatch(.*)*', component: NotFoundPage}
  ],
})

export default router
