export const userRoutes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/features/user/views/LoginView.vue'),
  },
];
