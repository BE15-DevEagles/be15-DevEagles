<template>
  <main class="bg-[var(--color-gray-50)] flex-1 overflow-auto">
    <div class="p-4 h-full">
      <div class="bg-[var(--color-neutral-white)] rounded-lg shadow-drop h-full p-4 overflow-auto">
        <div class="border-b border-[var(--color-gray-200)] pb-3 mb-4">
          <div class="flex items-center justify-between">
            <h1 class="font-section-title">
              <span class="text-[var(--color-gray-600)]">#</span>
              {{ currentPage }}
            </h1>
          </div>
          <p class="text-[var(--color-gray-500)] font-one-liner mt-1">
            {{ pageDescription }}
          </p>
        </div>

        <div class="flex-grow overflow-y-auto space-y-4 mb-4">
          <!-- 라우터 뷰를 통해 동적으로 내용을 표시 -->
          <router-view :key="routeKey"></router-view>
        </div>
      </div>
    </div>
  </main>
</template>

<script setup>
  import { useRoute } from 'vue-router';
  import { useTeamStore } from '@/store/team';
  import { computed } from 'vue';

  const route = useRoute();
  const teamStore = useTeamStore();

  const routeKey = computed(() => {
    return `${route.name}-${teamStore.currentTeamId}`;
  });

  // Props
  const props = defineProps({
    currentPage: {
      type: String,
      default: '샘플',
    },
    pageDescription: {
      type: String,
      default: '샘플 설명',
    },
  });
</script>
