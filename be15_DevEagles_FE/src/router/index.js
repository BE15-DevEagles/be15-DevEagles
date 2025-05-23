import { createRouter, createWebHistory } from 'vue-router';
import { Layout, ErrorPage } from '@/components/common/layout';
import timecapsuleRoutes from '@/features/timecapsule/router.js';

const routes = [
  {
    path: '/',
    component: Layout,
    children: [{ path: '', name: 'Home', component: () => import('@/views/Home.vue') }],
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: ErrorPage,
    props: {
      errorCode: '404',
      errorTitle: '페이지를 찾을 수 없습니다',
      errorMessage: '요청하신 페이지가 존재하지 않습니다. URL을 확인해 주세요.',
    },
  },
];

const router = createRouter({
  history: createWebHistory(),
  routes,
});

export default router;
