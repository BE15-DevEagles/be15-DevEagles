<template>
  <aside
    class="bg-[var(--color-neutral-white)] border-l border-[var(--color-gray-200)] h-full transition-all duration-200 flex flex-col shadow-drop flex-shrink-0 flex-grow-0 overflow-x-hidden overflow-y-hidden w-[340px] min-w-[340px] max-w-[340px]"
    :class="isCollapsed ? 'w-12 min-w-[48px] max-w-[48px]' : ''"
  >
    <!-- 접힌 상태일 때 -->
    <CollapsedSidebar
      v-if="isCollapsed"
      :current-mode="currentMode"
      :unread-count="chatStore.unreadCount"
      :selected-chat="selectedChat"
      @toggle-collapse="toggleCollapse"
      @toggle-mode="toggleMode"
      @close-chat="closeChat"
    />

    <!-- 펼친 상태일 때 -->
    <div v-if="!isCollapsed && shouldRenderContent" class="flex flex-col h-full relative min-w-0">
      <!-- 상단 헤더 -->
      <SidebarHeader class="flex-shrink-0" @toggle-collapse="toggleCollapse" />

      <!-- 채팅창이 열려 있을 때 - 사이드바 위에 오버레이 -->
      <transition name="fade">
        <ChatWindow
          v-if="selectedChat"
          :chat="selectedChat"
          @close="closeChat"
          @send-message="handleSendMessage"
        />
      </transition>

      <div v-if="!selectedChat" class="flex flex-col h-full overflow-y-hidden flex-grow">
        <!-- 콘텐츠 영역 (팀원 목록 + 분할선 + 채팅 목록) -->
        <div ref="contentContainer" class="flex flex-col h-full relative">
          <!-- 팀원 목록 (상단) -->
          <TeamMemberList
            :team-members="teamMembers"
            :style="{ height: `${teamListHeight}%` }"
            @view-worklog="viewWorkLog"
            @start-chat="startChatWithMember"
          />

          <!-- 조절 가능한 분할선 -->
          <div
            class="cursor-ns-resize border-t border-b border-[var(--color-gray-200)] bg-[var(--color-gray-100)] h-2 flex-shrink-0 flex items-center justify-center"
            @mousedown="startResize"
          >
            <div class="w-10 h-1 bg-[var(--color-gray-300)] rounded-full"></div>
          </div>

          <!-- 채팅 목록 (하단) -->
          <ChatList
            :chats="chatStore.chats"
            class="flex-grow"
            :style="{ height: `${100 - teamListHeight}%` }"
            @select-chat="selectChat"
          />
        </div>
      </div>
    </div>

    <!-- 펼쳐지는 중 & 아직 콘텐츠 렌더링 전 -->
    <div
      v-else-if="!isCollapsed && !shouldRenderContent"
      class="flex-grow flex items-center justify-center p-4 text-[var(--color-gray-400)]"
    ></div>
  </aside>
</template>

<script setup>
  import { onBeforeUnmount, ref, onMounted, computed, watch } from 'vue';
  import CollapsedSidebar from './CollapsedSidebar.vue';
  import { useSidebar } from './composables/useSidebar';
  import { useTeamStore } from '@/store/team';
  import { useChatStore } from '@/store/chat';

  import TeamMemberList from '@/features/team/components/TeamMemberList.vue';
  import ChatList from '@/features/chat/components/ChatList.vue';
  import ChatWindow from '@/features/chat/components/ChatWindow.vue';
  import SidebarHeader from '@/components/common/layout/SidebarHeader.vue';

  const teamStore = useTeamStore();
  const chatStore = useChatStore();

  // 팀원 목록
  const teamMembers = computed(() => {
    return teamStore.teamMembers;
  });

  // 리사이저 관련 상태
  const teamListHeight = ref(50);
  const contentContainer = ref(null);
  const isDragging = ref(false);
  const initialY = ref(0);
  const initialHeight = ref(0);

  // 리사이징 시작
  const startResize = e => {
    isDragging.value = true;
    initialY.value = e.clientY;
    initialHeight.value = teamListHeight.value;

    // 전역 이벤트 리스너 등록
    document.addEventListener('mousemove', onMouseMove);
    document.addEventListener('mouseup', stopResize);
  };

  const onMouseMove = e => {
    if (!isDragging.value) return;

    const containerHeight = contentContainer.value?.offsetHeight || 0;
    if (containerHeight === 0) return;

    const deltaY = e.clientY - initialY.value;
    const deltaPercent = (deltaY / containerHeight) * 100;
    let newHeight = initialHeight.value + deltaPercent;

    newHeight = Math.max(20, Math.min(80, newHeight));
    teamListHeight.value = newHeight;
  };

  // 리사이징 종료
  const stopResize = () => {
    isDragging.value = false;
    document.removeEventListener('mousemove', onMouseMove);
    document.removeEventListener('mouseup', stopResize);
  };

  // 컴포넌트 언마운트 시 이벤트 리스너 정리
  onBeforeUnmount(() => {
    stopResize();
    cleanup();
  });

  // 사이드바 상태 관리
  const {
    isCollapsed,
    shouldRenderContent,
    currentMode,
    selectedChat,
    toggleCollapse,
    toggleMode,
    cleanup,
  } = useSidebar();

  // 현재 팀이 변경될 때마다 데이터 갱신
  watch(
    () => teamStore.currentTeamId,
    (newTeamId, oldTeamId) => {
      if (newTeamId && newTeamId !== oldTeamId) {
        // 채팅창이 열려있는 경우 닫기
        selectedChat.value = null;
      }
    }
  );

  // 채팅창 닫기
  const closeChat = () => {
    selectedChat.value = null;
  };

  // 채팅 선택
  const selectChat = chat => {
    selectedChat.value = chat;
    chatStore.markChatAsRead(chat.id);
  };

  // 팀원과 채팅 시작
  const startChatWithMember = member => {
    // 실제 구현에서는 회원 ID로 채팅방을 찾거나 생성
    // 테스트 목적으로 임시 로직 사용
    const existingChat = chatStore.chats.find(c => c.name === member.name);
    if (existingChat) {
      selectChat(existingChat);
    } else {
      const chatId = `chat-${Date.now()}`;
      const newChat = {
        id: chatId,
        name: member.name,
        isOnline: member.isOnline,
        userThumbnail: member.thumbnail,
        lastMessage: '',
        lastMessageTime: '방금',
        unreadCount: 0,
        messages: [],
      };

      // 실제로는 API를 통해 채팅방을 생성하고 결과를 받아야 함
      chatStore.chats.unshift(newChat);
      selectChat(newChat);
    }
  };

  // 메시지 전송 처리
  const handleSendMessage = ({ text, chatId }) => {
    chatStore.sendMessage(chatId, text);
  };

  // 일지보기 버튼 클릭 처리
  const viewWorkLog = member => {
    // 현재는 간단하게 알림만 표시
    alert(`${member.name}님의 작업 로그: 준비 중입니다.`);
  };
</script>

<style scoped>
  /* 페이드 인/아웃 애니메이션 */
  .fade-enter-active,
  .fade-leave-active {
    transition: all 0.3s ease;
  }

  .fade-enter-from,
  .fade-leave-to {
    opacity: 0;
    transform: translateY(-20px);
  }

  .fade-enter-to,
  .fade-leave-from {
    opacity: 1;
    transform: translateY(0);
  }
</style>
