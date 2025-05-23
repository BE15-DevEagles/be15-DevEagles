import TeamInformationView from '@/features/team/views/TeamInformationView.vue';

export const teamRoutes = [
  {
    path: '/team/info/:teamId',
    name: 'TeamInfo',
    component: TeamInformationView,
  },
];
