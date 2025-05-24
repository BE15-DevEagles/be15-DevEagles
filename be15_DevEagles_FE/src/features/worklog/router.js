export const workRoutes = [
  {
    path: '/worklog/my',
    name: 'Myworklog',
    component: () => import('@/features/worklog/views/WorkLogListView.vue'),
    meta: {
      layout: 'default',
      requiresAuth: true,
      title: '나의 업무일지',
      description: '나의 모든 업무일지를 확이할 수 있어요.',
    },
  },
  {
    path: '/worklog/:id',
    name: 'WorklogDetail',
    component: () => import('@/features/worklog/views/WorklogDetailView.vue'),
    props: true,
  },
];
