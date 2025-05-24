<template>
  <div
    class="absolute inset-0 bg-[var(--color-neutral-white)] z-10 flex flex-col h-full w-full min-w-0"
  >
    <!-- 채팅방 헤더 -->
    <div class="p-3 border-b border-[var(--color-gray-200)] flex items-center justify-between">
      <div class="flex items-center min-w-0">
        <button
          class="mr-2 text-[var(--color-gray-500)] hover:text-[var(--color-primary-300)] transition-colors"
          @click="$emit('close')"
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
              v-if="chat.thumbnail"
              :src="chat.thumbnail"
              :alt="chat.name"
              class="w-full h-full object-cover"
            />
            <div
              v-else
              class="w-full h-full bg-[var(--color-primary-300)] flex items-center justify-center text-white font-small-semibold"
            >
              {{ chat.name.charAt(0) }}
            </div>
          </div>
          <div class="min-w-0">
            <h3 class="font-one-liner-semibold whitespace-nowrap overflow-hidden text-ellipsis">
              {{ chat.name }}
            </h3>
            <div class="flex items-center gap-2">
              <p
                v-if="chat.isOnline !== undefined"
                class="text-[var(--color-gray-500)] font-small leading-tight whitespace-nowrap overflow-hidden text-ellipsis"
              >
                {{ chat.isOnline ? '온라인' : '오프라인' }}
              </p>
              <div
                v-if="isConnected"
                class="w-2 h-2 bg-green-400 rounded-full"
                title="실시간 연결됨"
              ></div>
              <div v-else class="w-2 h-2 bg-gray-400 rounded-full" title="연결 끊김"></div>
            </div>
          </div>
        </div>
      </div>

      <!-- 알림 설정 버튼 -->
      <div class="flex items-center gap-2">
        <button
          :disabled="notificationLoading"
          class="p-2 rounded-md transition-colors"
          :class="[
            isNotificationEnabled
              ? 'text-[var(--color-primary-300)] hover:bg-[var(--color-primary-50)]'
              : 'text-[var(--color-gray-400)] hover:bg-[var(--color-gray-100)]',
            notificationLoading ? 'opacity-50 cursor-not-allowed' : 'cursor-pointer',
          ]"
          :title="
            notificationLoading
              ? '설정 변경 중...'
              : isNotificationEnabled
                ? '알림 끄기'
                : '알림 켜기'
          "
          @click="toggleNotifications"
        >
          <svg
            v-if="!notificationLoading"
            xmlns="http://www.w3.org/2000/svg"
            class="h-5 w-5"
            fill="none"
            viewBox="0 0 24 24"
            stroke="currentColor"
          >
            <path
              v-if="isNotificationEnabled"
              stroke-linecap="round"
              stroke-linejoin="round"
              stroke-width="2"
              d="M15 17h5l-1.405-1.405A2.032 2.032 0 0118 14.158V11a6.002 6.002 0 00-4-5.659V5a2 2 0 10-4 0v.341C7.67 6.165 6 8.388 6 11v3.159c0 .538-.214 1.055-.595 1.436L4 17h5m6 0v1a3 3 0 11-6 0v-1m6 0H9"
            />
            <path
              v-else
              stroke-linecap="round"
              stroke-linejoin="round"
              stroke-width="2"
              d="M5.586 15H4l1.405-1.405A2.032 2.032 0 006 12.158V11a6.002 6.002 0 014-5.659V5a2 2 0 114 0v.341C16.67 6.165 18 8.388 18 11v1.159c0 .538.214 1.055.595 1.436L20 15h-1.586M9 18v-1a3 3 0 116 0v1M9 18H6m9-3l-6-6"
            />
          </svg>
          <!-- 로딩 스피너 -->
          <svg
            v-else
            class="animate-spin h-5 w-5"
            xmlns="http://www.w3.org/2000/svg"
            fill="none"
            viewBox="0 0 24 24"
          >
            <circle
              class="opacity-25"
              cx="12"
              cy="12"
              r="10"
              stroke="currentColor"
              stroke-width="4"
            ></circle>
            <path
              class="opacity-75"
              fill="currentColor"
              d="m4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"
            ></path>
          </svg>
        </button>
      </div>
    </div>

    <!-- 채팅 내용 -->
    <div
      ref="messagesContainer"
      class="flex-grow overflow-y-auto p-3 bg-[var(--color-gray-50)]"
      @scroll="handleInfiniteScroll"
    >
      <!-- 과거 메시지 로딩 인디케이터 -->
      <div v-if="infiniteScrollLoading" class="flex justify-center items-center py-4">
        <div class="flex items-center gap-2">
          <div class="loading-spinner-small"></div>
          <p class="text-[var(--color-gray-500)] text-sm">이전 메시지를 불러오는 중...</p>
        </div>
      </div>

      <div
        v-if="isLoading && messages.length === 0"
        class="flex justify-center items-center h-full"
      >
        <div class="flex flex-col items-center gap-2">
          <div class="loading-spinner"></div>
          <p class="text-[var(--color-gray-500)]">메시지를 불러오는 중...</p>
        </div>
      </div>

      <div
        v-else-if="error && messages.length === 0"
        class="flex justify-center items-center h-full"
      >
        <div class="text-center">
          <p class="text-red-500 mb-2">{{ error }}</p>
          <button
            class="text-[var(--color-primary-300)] hover:underline"
            @click="retryLoadMessages"
          >
            다시 시도
          </button>
        </div>
      </div>

      <div
        v-else-if="groupedMessages.length === 0 && !isLoading"
        class="flex justify-center items-center h-full"
      >
        <div class="text-center">
          <p class="text-[var(--color-gray-500)] mb-2">메시지가 없습니다.</p>
          <p class="text-[var(--color-gray-400)] text-sm">첫 메시지를 보내보세요!</p>
        </div>
      </div>

      <div v-else class="space-y-4 min-h-full">
        <!-- 메시지 그룹 표시 -->
        <template v-for="(group, index) in groupedMessages" :key="`group-${index}`">
          <!-- 날짜 구분선 -->
          <div v-if="group.type === 'date'" class="flex items-center gap-4 my-6">
            <div class="flex-1 h-px bg-[var(--color-gray-300)]"></div>
            <div
              class="text-xs text-[var(--color-gray-500)] bg-[var(--color-gray-50)] px-3 py-1 rounded-full"
            >
              {{ formatDate(group.date) }}
            </div>
            <div class="flex-1 h-px bg-[var(--color-gray-300)]"></div>
          </div>

          <!-- 메시지 그룹 -->
          <div
            v-else-if="group.type === 'messages'"
            class="flex gap-2"
            :class="group.messages[0].isMe ? 'flex-row-reverse' : 'flex-row'"
          >
            <!-- 상대방 프로필 이미지 -->
            <div
              v-if="!group.messages[0].isMe"
              class="w-8 h-8 rounded-md overflow-hidden flex-shrink-0 self-end"
            >
              <img
                v-if="chat.thumbnail"
                :src="chat.thumbnail"
                :alt="chat.name"
                class="w-full h-full object-cover"
              />
              <div
                v-else
                class="w-full h-full bg-[var(--color-primary-300)] flex items-center justify-center text-white font-small-semibold"
              >
                {{ chat.name?.charAt(0) || '?' }}
              </div>
            </div>

            <!-- 메시지들 -->
            <div class="flex flex-col gap-1 max-w-[70%]">
              <!-- 발신자 이름 (상대방 메시지인 경우) -->
              <div v-if="!group.messages[0].isMe" class="text-xs text-[var(--color-gray-500)] px-2">
                {{ group.messages[0].senderName || chat.name }}
              </div>

              <!-- 개별 메시지들 -->
              <div
                v-for="(message, msgIndex) in group.messages"
                :key="`msg-${message.id}-${msgIndex}`"
                class="flex items-end gap-2"
                :class="message.isMe ? 'flex-row-reverse' : 'flex-row'"
              >
                <!-- 메시지 버블 -->
                <AiMessageBubble
                  v-if="isCurrentChatAi && isAiMessage(message)"
                  :message="message"
                  :is-thinking="isAiThinking"
                  :is-processing="isProcessingMood"
                />
                <div
                  v-else
                  class="px-3 py-2 rounded-lg max-w-full break-words"
                  :class="[
                    message.isMe
                      ? 'bg-[var(--color-primary-300)] text-white'
                      : 'bg-white text-[var(--color-gray-700)] border border-[var(--color-gray-200)]',
                    msgIndex === 0 && message.isMe ? 'rounded-tr-sm' : '',
                    msgIndex === 0 && !message.isMe ? 'rounded-tl-sm' : '',
                    msgIndex === group.messages.length - 1 && message.isMe ? 'rounded-br-sm' : '',
                    msgIndex === group.messages.length - 1 && !message.isMe ? 'rounded-bl-sm' : '',
                  ]"
                >
                  <p class="font-body text-sm whitespace-pre-wrap">{{ message.content }}</p>
                </div>

                <!-- 시간 표시 (마지막 메시지에만) -->
                <div
                  v-if="msgIndex === group.messages.length - 1"
                  class="text-xs text-[var(--color-gray-400)] flex-shrink-0 self-end pb-1"
                >
                  {{ formatMessageTime(message.timestamp) }}
                </div>
              </div>
            </div>
          </div>
        </template>
      </div>
    </div>

    <!-- 메시지 입력 영역 -->
    <div class="p-3 border-t border-[var(--color-gray-200)] bg-white">
      <div class="flex items-center bg-[var(--color-gray-100)] rounded-lg p-2">
        <input
          v-model="newMessage"
          type="text"
          :placeholder="
            isWaitingForMood ? '기분을 자유롭게 표현해주세요...' : '메시지를 입력하세요...'
          "
          class="flex-grow bg-transparent outline-none font-body px-2 text-sm"
          :disabled="isSending"
          @keyup.enter="handleSendMessage"
          @focus="handleInputFocus"
        />
        <button
          class="p-2 rounded-lg transition-colors flex-shrink-0"
          :class="[
            newMessage.trim() && !isSending
              ? 'text-[var(--color-primary-300)] hover:bg-[var(--color-primary-50)]'
              : 'text-[var(--color-gray-400)] cursor-not-allowed',
          ]"
          :disabled="!newMessage.trim() || isSending"
          @click="handleSendMessage"
        >
          <svg
            v-if="!isSending"
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
          <svg
            v-else
            class="animate-spin h-5 w-5"
            xmlns="http://www.w3.org/2000/svg"
            fill="none"
            viewBox="0 0 24 24"
          >
            <circle
              class="opacity-25"
              cx="12"
              cy="12"
              r="10"
              stroke="currentColor"
              stroke-width="2"
            ></circle>
            <path
              class="opacity-75"
              fill="currentColor"
              d="m4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"
            ></path>
          </svg>
        </button>
      </div>

      <!-- 연결 상태 표시 -->
      <div v-if="!isConnected" class="text-xs text-orange-500 mt-2 text-center">
        실시간 연결이 끊어졌습니다. 메시지가 즉시 전달되지 않을 수 있습니다.
      </div>

      <!-- AI 응답 대기 상태 표시 -->
      <div
        v-if="isCurrentChatAi && isAiThinking"
        class="text-xs text-blue-500 mt-2 text-center flex items-center justify-center gap-2"
      >
        <div class="loading-spinner-small"></div>
        <span>수리 AI가 응답을 준비하고 있어요...</span>
      </div>
    </div>
  </div>
</template>

<script setup>
  import { ref, computed, watch, nextTick, onBeforeUnmount } from 'vue';
  import { useAuthStore } from '@/store/auth';
  import { useNotifications } from '@/features/chat/composables/useNotifications';
  import { useMessages } from '@/features/chat/composables/useMessages';
  import { useChatWebSocket } from '@/features/chat/composables/useChatWebSocket';
  import { useChatRoom } from '@/features/chat/composables/useChatRoom';
  import { useChatInfiniteScroll } from '@/features/chat/composables/useChatInfiniteScroll';
  import { useChatScroll } from '@/features/chat/composables/useChatScroll';
  import { useChatWindow } from '@/features/chat/composables/useChatWindow';
  import { useDebounce } from '@/features/chat/composables/useDebounce';
  import { useAiChat } from '@/features/chat/composables/useAiChat';
  import AiMessageBubble from './AiMessageBubble.vue';
  import api from '@/api/axios';

  const props = defineProps({
    chat: {
      type: Object,
      required: true,
    },
  });

  const emit = defineEmits(['close']);

  // 상태 관리
  const newMessage = ref('');
  const messagesContainer = ref(null);
  const authStore = useAuthStore();

  // 컴포저블 사용
  const {
    messages,
    groupedMessages,
    addMessage,
    addTemporaryMessage,
    clearMessages,
    setMessages,
    formatMessageTime,
    formatDate,
    getMessageCount,
    getLastMessage,
    getOldestMessage,
  } = useMessages();

  const {
    isConnected,
    subscribeToChat,
    unsubscribeFromChat,
    markMessageAsRead,
    markLatestMessageAsRead,
    initializeAiChat,
  } = useChatWebSocket();

  const {
    isLoading,
    isSending,
    error,
    loadChatHistory,
    sendMessage,
    markChatAsRead,
    updateChatInfo,
    clearError,
  } = useChatRoom();

  const {
    isNotificationEnabled: checkNotificationEnabled,
    toggleNotification,
    isLoading: notificationLoading,
  } = useNotifications();

  const {
    shouldScrollToBottom,
    setScrollContainer,
    scrollToBottom,
    maintainScrollPosition,
    handleScroll,
    reset: resetScroll,
  } = useChatScroll();

  const { initializeChat, handleIncomingMessage, loadMoreMessages } = useChatWindow();

  // AI 채팅 컴포저블 추가
  const {
    isAiThinking,
    isProcessingMood,
    setAiThinking,
    handleUserMessageForMood,
    handleAiMessage,
    isAiChatRoom,
    isAiMessage,
    isWaitingForMoodResponse,
    findPendingMoodInquiry,
    clearError: clearAiError,
  } = useAiChat();

  // 무한 스크롤 콜백 함수
  const infiniteScrollCallback = () => {
    return loadMoreMessages(props.chat?.id, {
      getOldestMessage,
      loadChatHistory,
      setMessages,
      messages,
      maintainScrollPosition,
    });
  };

  const {
    isLoading: infiniteScrollLoading,
    hasMore,
    reset: resetInfiniteScroll,
    checkAndLoadMore,
  } = useChatInfiniteScroll(infiniteScrollCallback);

  // 디바운스 설정
  const { debouncedFn: debouncedMarkAsRead } = useDebounce(markLatestMessageAsRead, 500);

  // 계산된 속성
  const isNotificationEnabled = computed(() => {
    return props.chat?.id ? checkNotificationEnabled(props.chat.id) : true;
  });

  // AI 채팅방 여부 확인
  const isCurrentChatAi = computed(() => {
    return isAiChatRoom(props.chat);
  });

  // 기분 질문 대기 중인지 확인
  const isWaitingForMood = computed(() => {
    return isCurrentChatAi.value && isWaitingForMoodResponse(messages.value);
  });

  // 스크롤 핸들러
  const handleInfiniteScroll = event => {
    handleScroll(event, scrollTop => {
      if (hasMore.value && !infiniteScrollLoading.value && !isLoading.value) {
        console.log('[ChatWindow] 무한 스크롤 트리거:', { scrollTop });
        checkAndLoadMore(scrollTop);
      }
    });
  };

  // 메시지 핸들러
  const onIncomingMessage = message => {
    // AI 메시지 처리
    if (isCurrentChatAi.value && isAiMessage(message)) {
      handleAiMessage(message);
    }

    handleIncomingMessage(message, {
      currentChatId: props.chat?.id,
      addMessage,
      updateChatInfo,
      markMessageAsRead: (chatId, messageId) => {
        debouncedMarkAsRead(chatId, messageId);
      },
      scrollToBottom,
    });
  };

  const handleReadStatusMessage = readStatus => {
    console.log('[ChatWindow] 읽음 상태 수신:', readStatus);
  };

  // 액션 함수들
  const initChat = async () => {
    await initializeChat(props.chat?.id, {
      loadChatHistory,
      setMessages,
      subscribeToChat: chatId =>
        subscribeToChat(chatId, onIncomingMessage, handleReadStatusMessage),
      markChatAsRead,
      clearError,
      onReady: async () => {
        // 스크롤 컨테이너 설정
        if (messagesContainer.value) {
          setScrollContainer(messagesContainer.value);
        }
        // 하단으로 스크롤
        nextTick(() => scrollToBottom(true));

        // AI 채팅방인 경우 초기화
        if (isCurrentChatAi.value && props.chat?.id) {
          console.log('[ChatWindow] AI 채팅방 감지:', {
            chatId: props.chat.id,
            chatType: props.chat.type,
            chatName: props.chat.name,
            isAiChat: props.chat.isAiChat,
          });

          // 백엔드에서 실제 채팅방 정보 확인
          try {
            const response = await api.get(`/chatrooms/${props.chat.id}`);
            console.log('[ChatWindow] 백엔드 채팅방 정보:', response.data);
          } catch (error) {
            console.error('[ChatWindow] 채팅방 정보 조회 실패:', error);
          }

          console.log('[ChatWindow] AI 채팅방 초기화 시작:', props.chat.id);
          const success = initializeAiChat(props.chat.id);
          console.log('[ChatWindow] AI 초기화 결과:', success);
        } else {
          console.log('[ChatWindow] 일반 채팅방:', {
            isCurrentChatAi: isCurrentChatAi.value,
            chatId: props.chat?.id,
            chatType: props.chat?.type,
          });
        }
      },
    });
  };

  const handleSendMessage = async () => {
    if (!newMessage.value.trim() || isSending.value || !props.chat?.id) {
      return;
    }

    const messageText = newMessage.value.trim();
    console.log('[ChatWindow] 메시지 전송 시작:', messageText);

    try {
      // 즉시 UI에 임시 메시지 추가
      const tempMessage = addTemporaryMessage(
        messageText,
        authStore.userId,
        authStore.userName || authStore.nickname
      );

      // AI 채팅방에서 기분 질문 답변 처리
      if (isCurrentChatAi.value) {
        console.log('[ChatWindow] AI 채팅방에서 메시지 전송:', {
          message: messageText,
          chatId: props.chat.id,
          isAiChat: isCurrentChatAi.value,
        });

        const isMoodAnswer = await handleUserMessageForMood(tempMessage, messages.value);
        if (isMoodAnswer) {
          console.log('[ChatWindow] 기분 질문 답변으로 처리됨');
          setAiThinking(true); // AI 응답 대기 상태 표시
        } else {
          console.log('[ChatWindow] 일반 AI 메시지로 전송');
          setAiThinking(true); // AI 응답 대기 상태 표시
        }
      }

      // 입력창 초기화 및 스크롤
      newMessage.value = '';
      scrollToBottom(true);

      // 실제 메시지 전송
      const sentMessage = await sendMessage(props.chat.id, messageText);

      if (sentMessage) {
        console.log('[ChatWindow] 메시지 전송 성공');
        updateChatInfo(props.chat.id, sentMessage);
      } else {
        console.error('[ChatWindow] 메시지 전송 실패');
      }
    } catch (err) {
      console.error('[ChatWindow] 메시지 전송 중 오류:', err);
    }
  };

  const handleInputFocus = () => {
    // 입력창 포커스 시 읽음 처리
    if (props.chat?.id) {
      const lastMessage = getLastMessage();
      if (lastMessage && !lastMessage.isMe) {
        markMessageAsRead(props.chat.id, lastMessage.id);
      }
    }
  };

  const retryLoadMessages = () => {
    console.log('[ChatWindow] 메시지 재로드 시도');
    initChat();
  };

  const toggleNotifications = async () => {
    if (props.chat?.id && !notificationLoading.value) {
      try {
        const newState = await toggleNotification(props.chat.id);
        console.log(`[ChatWindow] 알림 상태 변경: ${newState ? '켜짐' : '꺼짐'}`);
      } catch (error) {
        console.error('[ChatWindow] 알림 설정 변경 실패:', error);
      }
    }
  };

  // 감시자
  watch(
    () => props.chat?.id,
    async (newChatId, oldChatId) => {
      console.log('[ChatWindow] 채팅방 변경:', { oldChatId, newChatId });

      if (newChatId && newChatId !== oldChatId) {
        // 기존 구독 해제
        unsubscribeFromChat();

        // 모든 상태 클리어
        clearMessages();
        clearError();
        clearAiError();
        resetInfiniteScroll();
        resetScroll();

        // 짧은 지연 후 새 채팅방 초기화
        setTimeout(() => {
          if (props.chat?.id === newChatId) {
            initChat();
          }
        }, 100);
      } else if (!newChatId) {
        unsubscribeFromChat();
        clearMessages();
        resetInfiniteScroll();
        resetScroll();
      }
    },
    { immediate: true }
  );

  // 컴포넌트 정리
  onBeforeUnmount(() => {
    console.log('[ChatWindow] 컴포넌트 정리');
    unsubscribeFromChat();
    clearMessages();
  });
</script>

<style scoped>
  .loading-spinner {
    width: 24px;
    height: 24px;
    border: 2px solid var(--color-gray-200);
    border-top: 2px solid var(--color-primary-300);
    border-radius: 50%;
    animation: spin 1s linear infinite;
  }

  .loading-spinner-small {
    width: 16px;
    height: 16px;
    border: 2px solid var(--color-gray-200);
    border-top: 2px solid var(--color-primary-300);
    border-radius: 50%;
    animation: spin 1s linear infinite;
  }

  @keyframes spin {
    0% {
      transform: rotate(0deg);
    }
    100% {
      transform: rotate(360deg);
    }
  }

  .break-words {
    word-wrap: break-word;
    overflow-wrap: break-word;
  }
</style>
