import { createRouter, createWebHistory } from 'vue-router';
import { ErrorPage } from '@/components/common/layout';
import MyCalendarView from '@/features/todolist/views/MyCalendarView.vue';
import { calendarRoutes } from '@/features/todolist/router.js';

// 라우트 정의
const routes = [
  {
    path: '/',
    name: 'Home',
    component: () => import('@/views/Home.vue'),
  },

  // 404 페이지
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

  ...calendarRoutes,
];

// 라우터 생성
const router = createRouter({
  history: createWebHistory(),
  routes,
});

export default router;
