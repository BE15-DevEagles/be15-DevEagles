<template>
  <div
    class="flex flex-col border-t border-[var(--color-gray-200)] h-full overflow-hidden flex-grow-0"
  >
    <div class="p-3 border-b border-[var(--color-gray-200)] flex-shrink-0">
      <h2 class="font-section-inner">채팅 목록</h2>
    </div>

    <div class="flex-grow overflow-y-auto h-full" style="height: calc(100% - 49px)">
      <div
        v-for="(chat, idx) in chats"
        :key="idx"
        class="p-3 border-b border-[var(--color-gray-200)] hover:bg-[var(--color-gray-100)] cursor-pointer transition-colors"
        @click="$emit('select-chat', chat)"
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
              <span class="text-xs text-[var(--color-gray-500)]">{{ chat.lastMessageTime }}</span>
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
</template>

<script setup>
  import { defineProps, defineEmits } from 'vue';

  /**
   * Props:
   * chats - 채팅 목록 배열
   *
   * 예시:
   * [
   *   {
   *     id: 1,
   *     name: '김경록',
   *     isOnline: true,
   *     userThumbnail: null,
   *     lastMessage: '안녕하세요! 오늘 회의 자료 확인했습니다.',
   *     lastMessageTime: '14:12 전',
   *     unreadCount: 0,
   *     messages: [
   *       { text: '안녕하세요!', time: '14:12', isMe: false },
   *       { text: '네, 확인했습니다.', time: '14:15', isMe: true },
   *     ],
   *   },
   *   ...
   * ]
   */
  defineProps({
    chats: {
      type: Array,
      required: true,
    },
  });

  /**
   * Emits:
   * select-chat - 채팅을 클릭했을 때 발생, 인자로 선택한 채팅 객체 전달
   *
   * 예시:
   * this.$emit('select-chat', { id: 1, name: '김경록', ... })
   */
  defineEmits(['select-chat']);
</script>
