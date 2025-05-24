<template>
  <div class="flex flex-col h-screen bg-[var(--color-gray-800)]">
    <Header />
    <div class="flex flex-1 overflow-hidden min-w-0">
      <TeamSidebar />
      <Sidebar />
      <Content :current-page="currentPage" :page-description="pageDescription" />
      <RightSidebar
        :is-collapsed="isSidebarCollapsed"
        @update:is-collapsed="handleSidebarCollapse"
      />
    </div>
    <Footer />
  </div>
</template>

<script setup>
  import { ref, computed } from 'vue';
  import { useTeamStore } from '@/store/team';
  import Header from './Header.vue';
  import Sidebar from './Sidebar.vue';
  import TeamSidebar from './TeamSidebar.vue';
  import Content from './Content.vue';
  import RightSidebar from './RightSidebar.vue';
  import Footer from './Footer.vue';
  import { useRoute } from 'vue-router';

  const teamStore = useTeamStore();
  const isSidebarCollapsed = ref(false);
  const handleSidebarCollapse = val => {
    console.log('[Sidebar 상태] isSidebarCollapsed 변경됨 →', val);
    isSidebarCollapsed.value = val;
  };

  const route = useRoute();
  const isMyPage = computed(() => route.path.startsWith('/mypage'));
  const currentPage = computed(() =>
    isMyPage.value ? '마이페이지' : teamStore.currentTeam?.name || '일반'
  );

  const pageDescription = computed(() =>
    isMyPage.value ? '' : teamStore.currentTeam?.description || '팀 채널 소통 공간'
  );
</script>

<style>
  html,
  body {
    height: 100%;
    margin: 0;
    padding: 0;
    font-family: 'Noto Sans KR', sans-serif;
    color: var(--color-gray-800);
    background-color: var(--color-gray-100);
  }

  ::-webkit-scrollbar {
    width: 8px;
    height: 8px;
  }

  ::-webkit-scrollbar-track {
    background: transparent;
  }

  ::-webkit-scrollbar-thumb {
    background: var(--color-gray-400);
    border-radius: 4px;
  }

  ::-webkit-scrollbar-thumb:hover {
    background: var(--color-gray-500);
  }

  /* 모든 버튼의 기본 스타일 */
  button {
    transition: all 0.2s;
  }

  /* 모든 인풋의 기본 스타일 */
  input,
  textarea,
  select {
    outline: none;
    transition: all 0.2s;
  }

  /* 기본 포커스 스타일 */
  *:focus-visible {
    outline: 2px solid var(--color-primary-300);
    outline-offset: 2px;
  }
</style>
