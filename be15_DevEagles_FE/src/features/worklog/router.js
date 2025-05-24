export const workRoutes = [
  {
    path: '/worklog/my',
    name: 'Myworklog',
    component: () => import('@/features/worklog/views/WorkLogListView.vue'),
    meta: {
      layout: 'default',
      requiresAuth: true,
      title: '업무일지 목록',
      description: '모든 업무일지(팀 or 본인)를 확인할 수 있어요.',
    },
  },
  {
    path: '/worklog/create',
    name: 'WorklogCreate',
    component: () => import('@/features/worklog/views/WorkCreate.vue'),
    meta: {
      layout: 'default',
      requiresAuth: true,
      title: '업무일지 상세 페이지',
      description: '업무일지 상세 내용을 확인할 수 있어요.',
    },
  },
];
