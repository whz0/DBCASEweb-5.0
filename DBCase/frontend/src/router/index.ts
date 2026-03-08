import { createRouter, createWebHistory } from 'vue-router'
import MainPage from '@/views/MainPage.vue'
import NotFoundPage from '@/views/NotFoundPage.vue'
import ErrorPage from '@/views/ErrorPage.vue'
import LoginPage from "@/views/LoginPage.vue";
import ProfilePage from "@/views/ProfilePage.vue";
import OauthSuccess from "@/views/OauthSuccess.vue";
import { useAuthStore } from "@/stores/authStore.ts";
import {storeToRefs} from "pinia";

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    { name: 'Home', path: '/', component: MainPage, meta: {requiresAuth: true}},
    { name: 'Login', path: '/login', component: LoginPage },
    { name: 'Oauth', path: '/oauth2/success', component: OauthSuccess},
    { name: 'Error', path: '/error', component: ErrorPage },
    { name: 'Profile', path: '/profile', component: ProfilePage, meta: {requiresAuth: true}},
    { name: 'NotFound', path: '/:pathMatch(.*)*', component: NotFoundPage }
  ],
})

router.beforeEach(async (to, from) => {

  if(to.meta.requiresAuth) {
    const auth = useAuthStore()
    const {user} = storeToRefs(auth)
    console.log(user.value.username)
    if(auth.user.username == '' && to.name != 'Login') return {name: 'Login'}
  }
})

export default router
