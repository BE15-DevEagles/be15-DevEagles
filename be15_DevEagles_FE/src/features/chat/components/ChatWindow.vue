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
            <p
              v-if="chat.isOnline !== undefined"
              class="text-[var(--color-gray-500)] font-small leading-tight whitespace-nowrap overflow-hidden text-ellipsis"
            >
              {{ chat.isOnline ? '온라인' : '오프라인' }}
            </p>
          </div>
        </div>
      </div>
    </div>

    <!-- 채팅 내용 -->
    <div ref="messagesContainer" class="flex-grow overflow-y-auto p-3 bg-[var(--color-gray-50)]">
      <div v-if="isLoading" class="flex justify-center items-center h-full">
        <p class="text-[var(--color-gray-500)]">메시지를 불러오는 중...</p>
      </div>
      <div v-else-if="messages.length === 0" class="flex justify-center items-center h-full">
        <p class="text-[var(--color-gray-500)]">메시지가 없습니다.</p>
      </div>
      <div v-else>
        <div v-for="message in messages" :key="message.id" class="mb-4">
          <div class="flex" :class="message.isMe ? 'justify-end' : 'justify-start'">
            <div
              v-if="!message.isMe"
              class="w-8 h-8 rounded-md overflow-hidden flex-shrink-0 mr-2 self-end"
            >
              <img
                v-if="message.senderThumbnail"
                :src="message.senderThumbnail"
                :alt="message.senderName"
                class="w-full h-full object-cover"
              />
              <div
                v-else
                class="w-full h-full bg-[var(--color-primary-300)] flex items-center justify-center text-white font-small-semibold"
              >
                {{ message.senderName?.charAt(0) || '?' }}
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
              <p class="font-body whitespace-pre-line text-sm">{{ message.content }}</p>
              <div class="text-right mt-1">
                <span
                  class="text-xs"
                  :class="
                    message.isMe ? 'text-[var(--color-gray-100)]' : 'text-[var(--color-gray-400)]'
                  "
                  >{{ formatMessageTime(message.timestamp) }}</span
                >
              </div>
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
          :disabled="isSending"
          @keyup.enter="sendMessage"
        />
        <button
          class="p-1 text-[var(--color-gray-500)] hover:text-[var(--color-primary-300)] transition-colors"
          :disabled="isSending"
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
</template>

<script setup>
  import { ref, defineProps, defineEmits, onMounted, onUnmounted, watch, nextTick } from 'vue';
  import { getChatHistory } from '../api/chatService';
  import { useAuthStore } from '@/store/auth';
  import { useChatStore } from '@/store/chat';

  const props = defineProps({
    chat: {
      type: Object,
      required: true,
    },
  });

  const emit = defineEmits(['close', 'send-message']);

  const newMessage = ref('');
  const messages = ref([]);
  const isLoading = ref(false);
  const isSending = ref(false);
  const messagesContainer = ref(null);
  const authStore = useAuthStore();
  const chatStore = useChatStore();

  // 메시지 시간 포맷팅
  function formatMessageTime(timestamp) {
    if (!timestamp) return '';

    const date = new Date(timestamp);
    const now = new Date();
    const today = new Date(now.getFullYear(), now.getMonth(), now.getDate());
    const messageDate = new Date(date.getFullYear(), date.getMonth(), date.getDate());

    if (messageDate.getTime() === today.getTime()) {
      return date.toLocaleTimeString('ko-KR', {
        hour: '2-digit',
        minute: '2-digit',
        hour12: false,
      });
    } else {
      return date.toLocaleDateString('ko-KR', {
        month: 'short',
        day: 'numeric',
        hour: '2-digit',
        minute: '2-digit',
      });
    }
  }

  // 채팅 메시지 로드
  async function loadChatHistory() {
    if (!props.chat?.id) return;

    try {
      isLoading.value = true;
      const response = await getChatHistory(props.chat.id);

      if (response && Array.isArray(response)) {
        messages.value = response.map(msg => ({
          ...msg,
          isMe: msg.senderId === authStore.userId,
        }));

        await nextTick();
        scrollToBottom();
      }
    } catch (error) {
      console.error('채팅 이력 로드 실패:', error);
    } finally {
      isLoading.value = false;
    }
  }

  // 메시지 전송
  async function sendMessage() {
    if (!newMessage.value.trim() || isSending.value) return;

    const messageText = newMessage.value.trim();
    isSending.value = true;

    try {
      // store를 통해 메시지 전송
      emit('send-message', { chatId: props.chat.id, message: messageText });
      newMessage.value = '';
    } catch (error) {
      console.error('메시지 전송 실패:', error);
    } finally {
      isSending.value = false;
    }
  }

  // 스크롤을 맨 아래로
  function scrollToBottom() {
    if (messagesContainer.value) {
      messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight;
    }
  }

  // 채팅방 변경 시 처리
  watch(
    () => props.chat?.id,
    (newChatId, oldChatId) => {
      if (newChatId && newChatId !== oldChatId) {
        messages.value = [];
        loadChatHistory();
      }
    },
    { immediate: true }
  );
</script>
