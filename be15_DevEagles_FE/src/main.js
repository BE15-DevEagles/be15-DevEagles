import { createApp } from 'vue';
import App from './App.vue';
import router from './router';
import { createPinia } from 'pinia';
import { useAuthStore } from '@/store/auth.js';
import { setupChat } from './features/chat/config/chatConfig';
import './assets/css/index.css';

const app = createApp(App);
const pinia = createPinia();

app.use(router);
app.use(pinia);

app.mount('#app');

const authStore = useAuthStore();
authStore.initAuth();

// 인증된 사용자인 경우 웹소켓 연결 및 채팅 설정
if (authStore.isAuthenticated) {
  setupChat();
}

// router.beforeEach((to, from, next) => {
//   if (to.matched.some(record => record.meta.requiresAuth)) {
//     if (!authStore.isAuthenticated) {
//       next({
//         path: '/',
//         query: { redirect: to.fullPath },
//       });
//     } else {
//       next();
//     }
//   } else {
//     next();
//   }
// });
