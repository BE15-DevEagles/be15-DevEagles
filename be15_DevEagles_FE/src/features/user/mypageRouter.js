export const myPageRoutes = [
  {
    path: '/mypage',
    name: 'MyPage',
    component: () => import('@/features/user/views/MyPageView.vue'),
  },
  {
    path: '/mypage/edit',
    name: 'UserEdit',
    component: () => import('@/features/user/views/UserEditPageView.vue'),
  },
  {
    path: '/mypage/editpwd',
    name: 'PwdEdit',
    component: () => import('@/features/user/views/PwdEditPageView.vue'),
  },
];
