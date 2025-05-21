<template>
  <div class="flex flex-col h-screen bg-[var(--color-gray-100)]">
    <Header />
    <div class="flex flex-1 overflow-hidden min-w-0">
      <TeamSidebar />
      <Sidebar />
      <Content :current-channel="currentChannel" :channel-description="channelDescription">
        <slot></slot>
      </Content>
      <RightSidebar />
    </div>
  </div>
</template>

<script setup>
  import { ref } from 'vue';
  import Header from './Header.vue';
  import Sidebar from './Sidebar.vue';
  import TeamSidebar from './TeamSidebar.vue';
  import Content from './Content.vue';
  import RightSidebar from './RightSidebar.vue';

  // 현재 채널 정보
  const currentChannel = ref('일반');
  const channelDescription = ref('팀 채널 소통 공간');
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
