<template>
  <div class="ai-message-container">
    <!-- AI 메시지 버블 -->
    <div
      class="px-3 py-2 rounded-lg max-w-full break-words bg-gradient-to-r from-blue-50 to-indigo-50 border border-blue-200 text-gray-800"
      :class="[getMessageClasses(), { 'animate-pulse': isThinking }]"
    >
      <!-- 메시지 타입별 아이콘 -->
      <div v-if="showTypeIcon" class="flex items-center mb-2">
        <component :is="getTypeIcon()" class="w-4 h-4 mr-2" :class="getIconColor()" />
        <span class="text-xs font-medium" :class="getIconColor()">
          {{ getTypeLabel() }}
        </span>
      </div>

      <!-- 메시지 내용 -->
      <p class="font-body text-sm whitespace-pre-wrap">
        {{ message.content }}
      </p>

      <!-- 기분 질문인 경우 추가 정보 -->
      <div v-if="isMoodInquiry && showMoodHelp" class="mt-2 pt-2 border-t border-blue-200">
        <p class="text-xs text-blue-600 opacity-75">💭 자유롭게 답변해주세요!</p>
      </div>

      <!-- 처리 중 표시 -->
      <div v-if="isProcessing" class="mt-2 pt-2 border-t border-blue-200">
        <div class="flex items-center text-xs text-blue-600">
          <div class="loading-dots mr-2"></div>
          <span>처리 중...</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
  import { computed } from 'vue';
  import {
    getAiMessageType,
    isMoodInquiryMessage,
    isMoodFeedbackMessage,
    AI_CONSTANTS,
  } from '@/features/chat/utils/aiMessageUtils';

  const props = defineProps({
    message: {
      type: Object,
      required: true,
    },
    isThinking: {
      type: Boolean,
      default: false,
    },
    isProcessing: {
      type: Boolean,
      default: false,
    },
    showTypeIcon: {
      type: Boolean,
      default: true,
    },
    showMoodHelp: {
      type: Boolean,
      default: true,
    },
  });

  // Computed properties
  const messageType = computed(() => getAiMessageType(props.message));
  const isMoodInquiry = computed(() => isMoodInquiryMessage(props.message));
  const isMoodFeedback = computed(() => isMoodFeedbackMessage(props.message));

  // 메시지 타입별 스타일링
  const getMessageClasses = () => {
    const baseClasses = [];

    if (isMoodInquiry.value) {
      baseClasses.push(
        'border-l-4',
        'border-l-amber-400',
        'bg-gradient-to-r',
        'from-amber-50',
        'to-yellow-50'
      );
    } else if (isMoodFeedback.value) {
      baseClasses.push(
        'border-l-4',
        'border-l-green-400',
        'bg-gradient-to-r',
        'from-green-50',
        'to-emerald-50'
      );
    }

    return baseClasses;
  };

  // 타입별 아이콘
  const getTypeIcon = () => {
    switch (messageType.value) {
      case AI_CONSTANTS.MESSAGE_TYPES.MOOD_INQUIRY:
        return 'QuestionMarkCircleIcon';
      case AI_CONSTANTS.MESSAGE_TYPES.MOOD_FEEDBACK:
        return 'HeartIcon';
      default:
        return 'ChatBubbleLeftIcon';
    }
  };

  // 아이콘 색상
  const getIconColor = () => {
    switch (messageType.value) {
      case AI_CONSTANTS.MESSAGE_TYPES.MOOD_INQUIRY:
        return 'text-amber-600';
      case AI_CONSTANTS.MESSAGE_TYPES.MOOD_FEEDBACK:
        return 'text-green-600';
      default:
        return 'text-blue-600';
    }
  };

  // 타입 라벨
  const getTypeLabel = () => {
    switch (messageType.value) {
      case AI_CONSTANTS.MESSAGE_TYPES.MOOD_INQUIRY:
        return '기분 체크';
      case AI_CONSTANTS.MESSAGE_TYPES.MOOD_FEEDBACK:
        return '공감 응답';
      default:
        return 'AI 응답';
    }
  };

  // 아이콘 컴포넌트들 (Heroicons 대신 간단한 SVG로 대체)
  const QuestionMarkCircleIcon = {
    template: `
    <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="1.5" stroke="currentColor">
      <path stroke-linecap="round" stroke-linejoin="round" d="M9.879 7.519c.75-1.297 2.142-2.019 3.521-2.019s2.771.722 3.521 2.019c.75 1.297.75 2.961 0 4.258-.334.579-.909 1.052-1.613 1.363l-.518.2c-.316.122-.59.34-.79.625-.2.286-.308.626-.308.976v.926M12 16v.01" />
    </svg>
  `,
  };

  const HeartIcon = {
    template: `
    <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="1.5" stroke="currentColor">
      <path stroke-linecap="round" stroke-linejoin="round" d="M21 8.25c0-2.485-2.099-4.5-4.688-4.5-1.935 0-3.597 1.126-4.312 2.733-.715-1.607-2.377-2.733-4.313-2.733C5.1 3.75 3 5.765 3 8.25c0 7.22 9 12 9 12s9-4.78 9-12z" />
    </svg>
  `,
  };

  const ChatBubbleLeftIcon = {
    template: `
    <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="1.5" stroke="currentColor">
      <path stroke-linecap="round" stroke-linejoin="round" d="M7.5 8.25h9m-9 3H12m-9.75 1.51c0 1.608-.067 2.19.618 2.907.685.717 1.924.684 3.532.684h8.25c1.608 0 2.907.067 3.532-.618.717-.685.684-1.924.684-3.532V8.25c0-1.608.067-2.907-.618-3.532C18.307 4.067 17.068 4.1 15.46 4.1H7.21c-1.608 0-2.907-.067-3.532.618C3.067 5.343 3.1 6.582 3.1 8.19v8.25z" />
    </svg>
  `,
  };
</script>

<style scoped>
  .loading-dots {
    width: 12px;
    height: 12px;
    position: relative;
  }

  .loading-dots::before {
    content: '';
    position: absolute;
    width: 3px;
    height: 3px;
    border-radius: 50%;
    background: currentColor;
    animation: loading-dots 1.4s infinite;
  }

  .loading-dots::after {
    content: '';
    position: absolute;
    width: 3px;
    height: 3px;
    border-radius: 50%;
    background: currentColor;
    left: 5px;
    animation: loading-dots 1.4s infinite 0.2s;
  }

  @keyframes loading-dots {
    0%,
    80%,
    100% {
      opacity: 0;
    }
    40% {
      opacity: 1;
    }
  }

  .break-words {
    word-wrap: break-word;
    overflow-wrap: break-word;
  }
</style>
