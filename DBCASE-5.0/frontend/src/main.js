import { createApp } from 'vue';
import { createRouter, createWebHistory } from 'vue-router';
import App from './App.vue';
import MainPage from "@/views/MainPage.vue";
import ProfilePage from './views/ProfilePage.vue';
import HelpPage from './views/HelpPage.vue';

import 'bootstrap/dist/css/bootstrap.min.css';
import 'bootstrap/dist/js/bootstrap.bundle.min';
import 'bootstrap-icons/font/bootstrap-icons.min.css';

const routes = [
    {path: '/', component: MainPage},
    {path: '/profile', component: ProfilePage},
    {path: '/help', component: HelpPage}
];

const router = createRouter({
    history: createWebHistory(),
    routes
});

createApp(App).use(router).mount('#app');
