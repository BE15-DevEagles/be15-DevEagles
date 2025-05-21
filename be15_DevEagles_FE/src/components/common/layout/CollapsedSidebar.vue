<template>
  <div class="flex flex-col h-full items-center">
    <div class="flex flex-col items-center py-4 space-y-4 flex-grow">
      <button
        class="w-8 h-8 flex items-center justify-center text-[var(--color-gray-500)] hover:text-[var(--color-primary-300)] transition-colors"
        @click="$emit('toggle-collapse')"
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
        @click="$emit('toggle-mode', 'team')"
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
        @click="$emit('toggle-mode', 'chat')"
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
        @click="$emit('close-chat')"
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
</template>

<script setup>
  import { defineProps, defineEmits } from 'vue';

  /**
   * Props:
   * currentMode - 현재 선택된 모드 ('team' 또는 'chat')
   * unreadCount - 읽지 않은 메시지 수
   * selectedChat - 현재 선택된 채팅 (채팅창이 열려있는 경우)
   *
   * 예시:
   * currentMode: 'team'
   * unreadCount: 3
   * selectedChat: { id: 1, name: '김경록', ... } 또는 null
   */
  const props = defineProps({
    currentMode: {
      type: String,
      required: true,
    },
    unreadCount: {
      type: Number,
      default: 0,
    },
    selectedChat: {
      type: Object,
      default: null,
    },
  });

  /**
   * Emits:
   * toggle-collapse - 사이드바 접기/펼치기 버튼 클릭 시 발생
   * toggle-mode - 모드 전환 버튼 클릭 시 발생, 인자로 선택한 모드 전달 ('team' 또는 'chat')
   * close-chat - 채팅창 닫기 버튼 클릭 시 발생
   *
   * 예시:
   * this.$emit('toggle-collapse')
   * this.$emit('toggle-mode', 'team')
   * this.$emit('close-chat')
   */
  defineEmits(['toggle-collapse', 'toggle-mode', 'close-chat']);
</script>
