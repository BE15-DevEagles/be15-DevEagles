import { createApp } from 'vue';
import { createPinia } from 'pinia';
import App from './App.vue';
import router from './router';
import { useAuthStore } from '@/store/auth.js';
import { setupChat } from './features/chat/config/chatConfig';
import './assets/css/index.css';

const app = createApp(App);
const pinia = createPinia();

app.use(pinia);
app.use(router);

app.mount('#app');

const authStore = useAuthStore();
authStore.initAuth().then(() => {
  if (authStore.isAuthenticated) {
    setupChat();
  }
});

// 라우터 가드는 필요하면 나중에 활성화
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
