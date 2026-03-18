import { storeToRefs } from 'pinia'
import { createRouter, createWebHistory } from 'vue-router'

import { useAuthStore } from '@/stores/authStore.ts'
import ErrorPage from '@/views/ErrorPage.vue'
import LoginPage from '@/views/LoginPage.vue'
import MainPage from '@/views/MainPage.vue'
import NotFoundPage from '@/views/NotFoundPage.vue'
import OauthSuccess from '@/views/OauthSuccess.vue'
import ProfilePage from '@/views/ProfilePage.vue'
import RegisterPage from '@/views/RegisterPage.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    { name: 'Home', path: '/', component: MainPage, meta: { requiresAuth: true } },
    { name: 'Login', path: '/login', component: LoginPage },
    { name: 'Register', path: '/register', component: RegisterPage },
    { name: 'Oauth', path: '/oauth2/success', component: OauthSuccess },
    { name: 'Error', path: '/error', component: ErrorPage },
    { name: 'Profile', path: '/profile', component: ProfilePage, meta: { requiresAuth: true } },
    { name: 'NotFound', path: '/:pathMatch(.*)*', component: NotFoundPage },
  ],
})

router.beforeEach(async (to, _from) => {
  if (to.meta.requiresAuth) {
    const auth = useAuthStore()
    const { user } = storeToRefs(auth)
    console.log(user.value.username)
    if (auth.user.username == '' && to.name != 'Login') return { name: 'Login' }
  }
})

export default router
