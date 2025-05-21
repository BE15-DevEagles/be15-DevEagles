<template>
  <main class="bg-[var(--color-gray-50)] flex-1 overflow-auto">
    <div class="p-4 h-full">
      <div class="bg-[var(--color-neutral-white)] rounded-lg shadow-drop h-full p-4">
        <div class="border-b border-[var(--color-gray-200)] pb-3 mb-4">
          <div class="flex items-center justify-between">
            <h1 class="font-section-title">
              <span class="text-[var(--color-gray-600)]">#</span>
              {{ currentChannel }}
            </h1>
          </div>
          <p class="text-[var(--color-gray-500)] font-one-liner mt-1">
            {{ channelDescription }}
          </p>
        </div>

        <div class="flex-grow overflow-y-auto space-y-4 mb-4">
          <slot>
            <!-- 기본 컨텐츠 영역 -->
            <div v-for="(message, index) in messages" :key="index" class="flex items-start mb-4">
              <div
                class="w-10 h-10 rounded-md bg-[var(--color-secondary-main)] text-white flex items-center justify-center mr-3 flex-shrink-0"
              >
                {{ message.sender.substring(0, 2) }}
              </div>
              <div class="flex-grow">
                <div class="flex items-center">
                  <span class="font-one-liner-semibold mr-2">{{ message.sender }}</span>
                  <span class="text-[var(--color-gray-400)] font-small">{{ message.time }}</span>
                </div>
                <p class="font-body text-[var(--color-gray-700)]">{{ message.content }}</p>
              </div>
            </div>
          </slot>
        </div>

        <div class="border-t border-[var(--color-gray-200)] pt-4">
          <div class="flex items-center justify-between px-2 pt-2"></div>
        </div>
      </div>
    </div>
  </main>
</template>

<script setup>
  // Props
  const props = defineProps({
    currentChannel: {
      type: String,
      default: '일반',
    },
    channelDescription: {
      type: String,
      default: '프로젝트에 관한 일반적인 대화를 나누는 채널입니다.',
    },
  });
</script>
