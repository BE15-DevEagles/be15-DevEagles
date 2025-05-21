<template>
  <aside
    class="bg-[var(--color-neutral-white)] border-l border-[var(--color-gray-200)] h-full transition-all duration-200 flex flex-col shadow-drop flex-shrink-0 flex-grow-0 overflow-x-hidden w-[340px] min-w-[340px] max-w-[340px]"
    :class="isCollapsed ? 'w-12 min-w-[48px] max-w-[48px]' : ''"
  >
    <!-- 접힌 상태일 때 -->
    <div v-if="isCollapsed" class="flex flex-col h-full items-center">
      <div class="flex flex-col items-center py-4 space-y-4 flex-grow">
        <button
          class="w-8 h-8 flex items-center justify-center text-[var(--color-gray-500)] hover:text-[var(--color-primary-300)] transition-colors"
          @click="toggleCollapse"
        >
          <svg
            xmlns="http://www.w3.org/2000/svg"
            class="h-5 w-5"
            fill="none"
            viewBox="0 0 24 24"
            stroke="currentColor"
          >
            <path
              stroke-linecap="round"
              stroke-linejoin="round"
              stroke-width="2"
              d="M15 19l-7-7 7-7"
            />
          </svg>
        </button>

        <button
          class="w-8 h-8 rounded-md flex items-center justify-center transition-colors relative"
          :class="
            currentMode === 'team'
              ? 'bg-[var(--color-primary-300)] text-white'
              : 'text-[var(--color-gray-500)] hover:text-[var(--color-primary-300)]'
          "
          @click="toggleMode('team')"
        >
          <svg
            xmlns="http://www.w3.org/2000/svg"
            class="h-5 w-5"
            fill="none"
            viewBox="0 0 24 24"
            stroke="currentColor"
          >
            <path
              stroke-linecap="round"
              stroke-linejoin="round"
              stroke-width="2"
              d="M12 4.354a4 4 0 110 5.292M15 21H3v-1a6 6 0 0112 0v1zm0 0h6v-1a6 6 0 00-9-5.197M13 7a4 4 0 11-8 0 4 4 0 018 0z"
            />
          </svg>
        </button>

        <button
          class="w-8 h-8 rounded-md flex items-center justify-center transition-colors relative"
          :class="
            currentMode === 'chat'
              ? 'bg-[var(--color-primary-300)] text-white'
              : 'text-[var(--color-gray-500)] hover:text-[var(--color-primary-300)]'
          "
          @click="toggleMode('chat')"
        >
          <svg
            xmlns="http://www.w3.org/2000/svg"
            class="h-5 w-5"
            fill="none"
            viewBox="0 0 24 24"
            stroke="currentColor"
          >
            <path
              stroke-linecap="round"
              stroke-linejoin="round"
              stroke-width="2"
              d="M8 12h.01M12 12h.01M16 12h.01M21 12c0 4.418-4.03 8-9 8a9.863 9.863 0 01-4.255-.949L3 20l1.395-3.72C3.512 15.042 3 13.574 3 12c0-4.418 4.03-8 9-8s9 3.582 9 8z"
            />
          </svg>

          <div
            v-if="unreadCount > 0"
            class="absolute -top-1 -right-1 w-4 h-4 bg-[var(--color-error-300)] rounded-full flex items-center justify-center"
          >
            <span class="text-white text-xs font-xs-semibold">{{
              unreadCount > 9 ? '9+' : unreadCount
            }}</span>
          </div>
        </button>
      </div>

      <!-- 하단 버튼 -->
      <div class="p-2">
        <button
          v-if="selectedChat"
          class="w-8 h-8 flex items-center justify-center text-[var(--color-gray-500)] hover:text-[var(--color-primary-300)] rounded-md transition-colors"
          @click="toggleExpandChat(null)"
        >
          <svg
            xmlns="http://www.w3.org/2000/svg"
            class="h-5 w-5"
            fill="none"
            viewBox="0 0 24 24"
            stroke="currentColor"
          >
            <path
              stroke-linecap="round"
              stroke-linejoin="round"
              stroke-width="2"
              d="M6 18L18 6M6 6l12 12"
            />
          </svg>
        </button>
      </div>
    </div>

    <!-- 펼친 상태일 때 -->
    <div v-if="!isCollapsed && shouldRenderContent" class="flex flex-col h-full relative min-w-0">
      <!-- 상단 접기 버튼 -->
      <div class="p-3 border-b border-[var(--color-gray-200)] flex items-center justify-between">
        <button
          class="text-[var(--color-gray-600)] hover:text-[var(--color-primary-300)] transition-colors mr-2"
          @click="toggleCollapse"
        >
          <svg
            xmlns="http://www.w3.org/2000/svg"
            class="h-5 w-5"
            fill="none"
            viewBox="0 0 24 24"
            stroke="currentColor"
          >
            <path
              stroke-linecap="round"
              stroke-linejoin="round"
              stroke-width="2"
              d="M9 5l7 7-7 7"
            />
          </svg>
        </button>
      </div>

      <!-- 채팅창이 열려 있을 때 - 사이드바 위에 오버레이 -->
      <transition name="fade">
        <div
          v-if="selectedChat"
          class="absolute inset-0 bg-[var(--color-neutral-white)] z-10 flex flex-col h-full w-full min-w-0"
        >
          <!-- 채팅방 헤더 -->
          <div
            class="p-3 border-b border-[var(--color-gray-200)] flex items-center justify-between"
          >
            <div class="flex items-center min-w-0">
              <button
                class="mr-2 text-[var(--color-gray-500)] hover:text-[var(--color-primary-300)] transition-colors"
                @click="toggleExpandChat(null)"
              >
                <svg
                  xmlns="http://www.w3.org/2000/svg"
                  class="h-5 w-5"
                  fill="none"
                  viewBox="0 0 24 24"
                  stroke="currentColor"
                >
                  <path
                    stroke-linecap="round"
                    stroke-linejoin="round"
                    stroke-width="2"
                    d="M10 19l-7-7m0 0l7-7m-7 7h18"
                  />
                </svg>
              </button>
              <div class="flex items-center min-w-0">
                <div class="w-8 h-8 rounded-md overflow-hidden flex-shrink-0 mr-2">
                  <img
                    v-if="selectedChat.avatar"
                    :src="selectedChat.avatar"
                    :alt="selectedChat.name"
                    class="w-full h-full object-cover"
                  />
                  <div
                    v-else
                    class="w-full h-full bg-[var(--color-primary-300)] flex items-center justify-center text-white font-small-semibold"
                  >
                    {{ selectedChat.name.charAt(0) }}
                  </div>
                </div>
                <div class="min-w-0">
                  <h3
                    class="font-one-liner-semibold whitespace-nowrap overflow-hidden text-ellipsis"
                  >
                    {{ selectedChat.name }}
                  </h3>
                  <p
                    v-if="selectedChat.isOnline !== undefined"
                    class="text-[var(--color-gray-500)] font-small leading-tight whitespace-nowrap overflow-hidden text-ellipsis"
                  >
                    {{ selectedChat.isOnline ? '온라인' : '오프라인' }}
                  </p>
                </div>
              </div>
            </div>
            <div>
              <button
                class="text-[var(--color-gray-500)] hover:text-[var(--color-primary-300)] transition-colors"
              >
                <svg
                  xmlns="http://www.w3.org/2000/svg"
                  class="h-5 w-5"
                  fill="none"
                  viewBox="0 0 24 24"
                  stroke="currentColor"
                >
                  <path
                    stroke-linecap="round"
                    stroke-linejoin="round"
                    stroke-width="2"
                    d="M12 5v.01M12 12v.01M12 19v.01M12 6a1 1 0 110-2 1 1 0 010 2zm0 7a1 1 0 110-2 1 1 0 010 2zm0 7a1 1 0 110-2 1 1 0 010 2z"
                  />
                </svg>
              </button>
            </div>
          </div>

          <!-- 채팅 내용 -->
          <div class="flex-grow overflow-y-auto p-3 bg-[var(--color-gray-50)]">
            <div v-for="(message, idx) in selectedChat.messages" :key="idx" class="mb-4">
              <div class="flex" :class="message.isMe ? 'justify-end' : 'justify-start'">
                <div
                  v-if="!message.isMe"
                  class="w-8 h-8 rounded-md overflow-hidden flex-shrink-0 mr-2 self-end"
                >
                  <img
                    v-if="selectedChat.avatar"
                    :src="selectedChat.avatar"
                    :alt="selectedChat.name"
                    class="w-full h-full object-cover"
                  />
                  <div
                    v-else
                    class="w-full h-full bg-[var(--color-primary-300)] flex items-center justify-center text-white font-small-semibold"
                  >
                    {{ selectedChat.name.charAt(0) }}
                  </div>
                </div>

                <div
                  class="max-w-[70%] rounded-lg px-3 py-2 shadow-sm"
                  :class="
                    message.isMe
                      ? 'bg-[var(--color-primary-300)] text-white'
                      : 'bg-white text-[var(--color-gray-700)]'
                  "
                >
                  <p class="font-body whitespace-pre-line text-sm">{{ message.text }}</p>
                  <div class="text-right">
                    <span
                      class="text-xs"
                      :class="
                        message.isMe
                          ? 'text-[var(--color-gray-100)]'
                          : 'text-[var(--color-gray-400)]'
                      "
                      >{{ message.time }}</span
                    >
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- 메시지 입력 영역 -->
          <div class="p-3 border-t border-[var(--color-gray-200)]">
            <div class="flex items-center bg-[var(--color-gray-100)] rounded-lg p-2">
              <input
                v-model="newMessage"
                type="text"
                placeholder="메시지를 입력하세요..."
                class="flex-grow bg-transparent outline-none font-body px-2"
                @keyup.enter="sendMessage"
              />
              <button
                class="p-1 text-[var(--color-gray-500)] hover:text-[var(--color-primary-300)] transition-colors"
                @click="sendMessage"
              >
                <svg
                  xmlns="http://www.w3.org/2000/svg"
                  class="h-5 w-5"
                  fill="none"
                  viewBox="0 0 24 24"
                  stroke="currentColor"
                >
                  <path
                    stroke-linecap="round"
                    stroke-linejoin="round"
                    stroke-width="2"
                    d="M12 19l9 2-9-18-9 18 9-2zm0 0v-8"
                  />
                </svg>
              </button>
            </div>
          </div>
        </div>
      </transition>

      <!-- 팀원 목록 (상단) -->
      <div class="border-b border-[var(--color-gray-200)]">
        <div class="p-3 border-b border-[var(--color-gray-200)]">
          <h2 class="font-section-inner">팀원</h2>
        </div>

        <div class="max-h-[40vh] overflow-y-auto">
          <div
            v-for="(member, idx) in teamMembers"
            :key="idx"
            class="p-3 border-b border-[var(--color-gray-200)] hover:bg-[var(--color-gray-100)] transition-colors"
          >
            <div class="flex items-center">
              <div class="relative mr-3 flex-shrink-0">
                <div
                  class="w-10 h-10 rounded-md overflow-hidden bg-[var(--color-primary-300)] flex items-center justify-center text-white font-one-liner-semibold"
                >
                  {{ member.name.charAt(0) }}
                </div>
                <div
                  class="absolute -bottom-1 -right-1 w-3 h-3 rounded-full border-2 border-white"
                  :class="
                    member.isOnline ? 'bg-[var(--color-success-300)]' : 'bg-[var(--color-gray-400)]'
                  "
                ></div>
              </div>

              <div class="flex-grow mr-2">
                <h3 class="font-one-liner-semibold">{{ member.name }}</h3>
              </div>

              <div class="flex space-x-2">
                <button
                  class="bg-[var(--color-info-500)] text-white px-2 py-1 rounded-md text-xs font-xs-semibold hover:bg-[var(--color-info-600)] transition-colors"
                  @click="viewWorkLog(member)"
                >
                  일지보기
                </button>
                <button
                  class="bg-[var(--color-primary-300)] text-white px-2 py-1 rounded-md text-xs font-xs-semibold hover:bg-[var(--color-primary-400)] transition-colors"
                  @click="startChat(member)"
                >
                  대화하기
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 채팅 목록 (하단) -->
      <div class="flex-grow flex flex-col">
        <div class="p-3 border-b border-[var(--color-gray-200)]">
          <h2 class="font-section-inner">채팅 목록</h2>
        </div>

        <div class="flex-grow overflow-y-auto">
          <div
            v-for="(chat, idx) in chats"
            :key="idx"
            class="p-3 border-b border-[var(--color-gray-200)] hover:bg-[var(--color-gray-100)] cursor-pointer transition-colors"
            @click="toggleExpandChat(chat)"
          >
            <div class="flex items-start">
              <div class="relative mr-3 flex-shrink-0">
                <div
                  class="w-10 h-10 rounded-md overflow-hidden bg-[var(--color-primary-300)] flex items-center justify-center text-white font-one-liner-semibold"
                >
                  {{ chat.name.charAt(0) }}
                </div>
                <div
                  v-if="chat.isOnline !== undefined"
                  class="absolute -bottom-1 -right-1 w-3 h-3 rounded-full border-2 border-white"
                  :class="
                    chat.isOnline ? 'bg-[var(--color-success-300)]' : 'bg-[var(--color-gray-400)]'
                  "
                ></div>
              </div>

              <div class="flex-grow overflow-hidden">
                <div class="flex items-center justify-between">
                  <h3 class="font-one-liner-semibold truncate">{{ chat.name }}</h3>
                  <span class="text-xs text-[var(--color-gray-500)]">{{
                    chat.lastMessageTime
                  }}</span>
                </div>
                <p class="text-[var(--color-gray-500)] font-small truncate">
                  {{ chat.lastMessage }}
                </p>
              </div>

              <div v-if="chat.unreadCount" class="ml-2 flex-shrink-0">
                <div
                  class="bg-[var(--color-error-300)] text-white rounded-full text-xs w-5 h-5 flex items-center justify-center font-xs-semibold"
                >
                  {{ chat.unreadCount > 99 ? '99+' : chat.unreadCount }}
                </div>
              </div>
            </div>
          </div>
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
  import { ref, computed, watch } from 'vue';

  // 사이드바 상태
  const isCollapsed = ref(false);
  const shouldRenderContent = ref(!isCollapsed.value); // 콘텐츠 렌더링 제어 상태
  let contentRenderTimer = null; // setTimeout 타이머 ID

  const currentMode = ref('team'); // 'team' 또는 'chat'
  const selectedChat = ref(null);
  const newMessage = ref('');

  // 팀원 데이터
  const teamMembers = ref([
    {
      id: 1,
      name: '김경록',
      isOnline: true,
      avatar: null,
      messages: [],
      workLog: ['오늘 할 일 완료', '새로운 기능 구현', '회의 참석'],
    },
    {
      id: 2,
      name: '박준석',
      isOnline: true,
      avatar: null,
      messages: [],
      workLog: ['API 개발', '데이터베이스 최적화', '버그 수정'],
    },
    {
      id: 3,
      name: '류현진',
      isOnline: false,
      avatar: null,
      messages: [],
      workLog: ['UI 디자인', '아이콘 제작', '프로토타입 테스트'],
    },
    {
      id: 4,
      name: '코디 폰스',
      isOnline: true,
      avatar: null,
      messages: [],
      workLog: ['일정 관리', '팀 미팅', '요구사항 분석'],
    },
  ]);

  // 채팅 데이터
  const chats = ref([
    {
      id: 1,
      name: '김경록',
      isOnline: true,
      avatar: null,
      lastMessage: '안녕하세요! 오늘 회의 자료 확인했습니다.',
      lastMessageTime: '14:12 전',
      unreadCount: 0,
      messages: [
        { text: '안녕하세요! 오늘 회의 자료 확인했습니다.', time: '14:12', isMe: false },
        { text: '네! 확인 감사합니다.', time: '14:15', isMe: true },
        { text: '추가 자료는 언제쯤 받을 수 있을까요?', time: '14:20', isMe: false },
        { text: '내일 오전까지 보내드리겠습니다.', time: '14:22', isMe: true },
      ],
    },
    {
      id: 2,
      name: '박준석',
      isOnline: true,
      avatar: null,
      lastMessage: '일정입니다!',
      lastMessageTime: '어제',
      unreadCount: 2,
      messages: [
        { text: '안녕하세요, 다음 주 일정 공유드립니다.', time: '어제 15:30', isMe: false },
        { text: '감사합니다. 일정 확인했습니다.', time: '어제 16:45', isMe: true },
        { text: '미팅 시간이 30분 당겨질 수 있을까요?', time: '어제 17:20', isMe: false },
        { text: '네, 가능합니다!', time: '어제 17:22', isMe: false },
      ],
    },
    {
      id: 3,
      name: '류현진',
      isOnline: false,
      avatar: null,
      lastMessage: '음이에용 뭐 먹을까요...',
      lastMessageTime: '2일 전',
      unreadCount: 0,
      messages: [
        { text: '점심 뭐 먹을까요?', time: '2일 전 12:30', isMe: false },
        { text: '저는 김치찌개 어떨까요?', time: '2일 전 12:31', isMe: true },
        { text: '좋아요! 김치찌개 먹어요.', time: '2일 전 12:32', isMe: false },
      ],
    },
    {
      id: 4,
      name: '코디 폰스',
      isOnline: true,
      avatar: null,
      lastMessage: 'how are you bro ...',
      lastMessageTime: '5일 전',
      unreadCount: 0,
      messages: [
        { text: 'Hello there!', time: '5일 전 09:15', isMe: false },
        { text: 'Hey, how are you?', time: '5일 전 09:17', isMe: true },
        { text: "I'm good, thanks! how are you bro?", time: '5일 전 09:20', isMe: false },
      ],
    },
  ]);

  // 읽지 않은 메시지 수 계산
  const unreadCount = computed(() => {
    return chats.value.reduce((total, chat) => total + (chat.unreadCount || 0), 0);
  });

  // isCollapsed 상태 변경 감지
  watch(isCollapsed, newVal => {
    // eslint-disable-next-line no-undef
    if (contentRenderTimer) clearTimeout(contentRenderTimer);

    if (newVal) {
      // true가 됨, 즉 접힘
      shouldRenderContent.value = false; // 내용 즉시 숨김
      if (selectedChat.value) {
        // 사이드바 접힐 때 열려있는 채팅창 닫기
        selectedChat.value = null;
      }
    } else {
      // false가 됨, 즉 펼쳐짐
      // 펼쳐지는 애니메이션 동안 내용 숨김
      shouldRenderContent.value = false;
      // eslint-disable-next-line no-undef
      contentRenderTimer = setTimeout(() => {
        shouldRenderContent.value = true; // 애니메이션 후 내용 표시
      }, 200); // transition duration (200ms)과 일치
    }
  });

  // 사이드바 접기/펼치기 토글
  const toggleCollapse = () => {
    isCollapsed.value = !isCollapsed.value;
    // selectedChat 초기화 로직은 watch로 이동
  };

  // 모드 전환 (팀원/채팅)
  const toggleMode = mode => {
    currentMode.value = mode;

    // 모드 전환 시 채팅창 닫기
    if (!isCollapsed.value) {
      selectedChat.value = null;
    }
  };

  // 채팅창 열기/닫기
  const toggleExpandChat = chat => {
    selectedChat.value = chat;

    // 채팅창을 열면 해당 채팅의 읽지 않은 메시지 수를 0으로 설정
    if (chat) {
      const chatIndex = chats.value.findIndex(c => c.id === chat.id);
      if (chatIndex !== -1) {
        chats.value[chatIndex].unreadCount = 0;
      }
    }
  };

  // 팀원과 채팅 시작
  const startChat = member => {
    // 해당 팀원과의 채팅이 이미 있는지 확인
    let chat = chats.value.find(c => c.id === member.id);

    // 채팅이 없으면 새로 생성
    if (!chat) {
      chat = {
        id: member.id,
        name: member.name,
        isOnline: member.isOnline,
        avatar: member.avatar,
        lastMessage: '',
        lastMessageTime: '방금',
        unreadCount: 0,
        messages: [],
      };
      chats.value.unshift(chat);
    }

    // 채팅창 열기
    toggleExpandChat(chat);
  };

  // 일지보기 버튼 클릭 처리
  const viewWorkLog = member => {
    // 일지보기 기능
    // TODO: 실제 구현에서는 모달이나 상세 페이지로 이동
    // member.workLog를 활용하여 일지를 보여줌
    // 임시로 아무 동작도 하지 않게 함
    // 실제 구현에서는 아래와 같은 형태로 사용
    // showWorkLogModal(member.id, member.workLog);
  };

  // 메시지 전송
  const sendMessage = () => {
    if (!newMessage.value.trim() || !selectedChat.value) {
      return;
    }

    // 현재 시간 생성
    const now = new Date();
    const hours = now.getHours().toString().padStart(2, '0');
    const minutes = now.getMinutes().toString().padStart(2, '0');
    const timeString = `${hours}:${minutes}`;

    // 새 메시지 객체 생성
    const message = {
      text: newMessage.value,
      time: timeString,
      isMe: true,
    };

    // 채팅 메시지에 추가
    const chatIndex = chats.value.findIndex(c => c.id === selectedChat.value.id);
    if (chatIndex !== -1) {
      chats.value[chatIndex].messages.push(message);
      chats.value[chatIndex].lastMessage = newMessage.value;
      chats.value[chatIndex].lastMessageTime = '방금';

      // 해당 채팅을 목록의 맨 위로 이동
      const chat = chats.value.splice(chatIndex, 1)[0];
      chats.value.unshift(chat);

      // 선택된 채팅도 업데이트
      selectedChat.value = chat;
    }

    // 입력창 초기화
    newMessage.value = '';

    // 자동 응답 구현 (예시)
    globalThis.setTimeout(() => {
      if (selectedChat.value) {
        const autoReply = {
          text: `[자동응답] "${message.text}"에 대한 답변은 조금 후에 드리겠습니다.`,
          time: `${hours}:${(parseInt(minutes) + 1).toString().padStart(2, '0')}`,
          isMe: false,
        };

        const idx = chats.value.findIndex(c => c.id === selectedChat.value.id);
        if (idx !== -1) {
          chats.value[idx].messages.push(autoReply);
          chats.value[idx].lastMessage = autoReply.text;
          selectedChat.value = chats.value[idx];
        }
      }
    }, 1000);
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
