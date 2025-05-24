export const myPageRoutes = [
  {
    path: '/mypage',
    name: 'MyPage',
    component: () => import('@/features/user/views/MyPageView.vue'),
  },
];
