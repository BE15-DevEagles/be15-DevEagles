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
              v-if="chat.userThumbnail"
              :src="chat.userThumbnail"
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
      <div v-for="(message, idx) in chat.messages" :key="idx" class="mb-4">
        <div class="flex" :class="message.isMe ? 'justify-end' : 'justify-start'">
          <div
            v-if="!message.isMe"
            class="w-8 h-8 rounded-md overflow-hidden flex-shrink-0 mr-2 self-end"
          >
            <img
              v-if="chat.userThumbnail"
              :src="chat.userThumbnail"
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
                  message.isMe ? 'text-[var(--color-gray-100)]' : 'text-[var(--color-gray-400)]'
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
</template>

<script setup>
  import { ref, defineProps, defineEmits } from 'vue';

  /**
   * Props:
   * chat - 선택된 채팅 객체
   *
   * 예시:
   * {
   *   id: 1,
   *   name: '김경록',
   *   isOnline: true,
   *   userThumbnail: null,
   *   lastMessage: '안녕하세요! 오늘 회의 자료 확인했습니다.',
   *   lastMessageTime: '14:12 전',
   *   unreadCount: 0,
   *   messages: [
   *     { text: '안녕하세요!', time: '14:12', isMe: false },
   *     { text: '네, 확인했습니다.', time: '14:15', isMe: true },
   *   ],
   * }
   */
  const props = defineProps({
    chat: {
      type: Object,
      required: true,
    },
  });

  const newMessage = ref('');

  /**
   * Emits:
   * close - 채팅창 닫기 버튼 클릭 시 발생
   * send-message - 메시지 전송 시 발생, 인자로 메시지 텍스트와 채팅 ID 전달
   *
   * 예시:
   * this.$emit('close')
   * this.$emit('send-message', { text: '안녕하세요', chatId: 1 })
   */
  const emit = defineEmits(['close', 'send-message']);

  function sendMessage() {
    if (!newMessage.value.trim()) return;

    emit('send-message', {
      text: newMessage.value,
      chatId: props.chat.id,
    });

    newMessage.value = '';
  }
</script>
