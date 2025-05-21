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
          <!-- 기본 컨텐츠 영역 -->
          디자인 참고용 샘플 파일
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
      default: '샘플',
    },
    channelDescription: {
      type: String,
      default: '샘플 설명',
    },
  });
</script>
