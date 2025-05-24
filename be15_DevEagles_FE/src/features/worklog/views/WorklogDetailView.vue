<script setup>
  import { onMounted, ref, computed } from 'vue';
  import { useRoute } from 'vue-router';
  import { useWorklogStore } from '@/store/worklog';
  import api from '@/api/axios';

  const route = useRoute();
  const worklogId = route.params.id;

  const store = useWorklogStore();
  const preview = computed(() => store.preview);
  const full = ref(null);

  function formatDate(dateStr) {
    return dateStr ? new Date(dateStr).toLocaleDateString() : '-';
  }

  onMounted(async () => {
    window.scrollTo(0, 0);
    try {
      const res = await api.get(`/worklog/${worklogId}`);
      full.value = res.data.data;
    } catch (err) {
      console.error('상세 조회 실패:', err);
    }
  });
</script>

<template>
  <section class="p-6">
    <!-- preview 먼저 표시 -->
    <div v-if="preview">
      <h1 class="font-screen-title mb-4">{{ preview.userName }}의 업무일지</h1>
      <div class="mb-2 text-sm text-gray">작성일: {{ formatDate(preview.writtenAt) }}</div>
      <div class="mb-4 text-gray">{{ preview.summary }}</div>
    </div>

    <!-- 로딩 중 메시지 (preview는 위에서 계속 표시됨) -->
    <div v-if="!full" class="text-center py-10 animate-pulse text-gray-500">
      🔄 상세 내용을 불러오는 중...
    </div>

    <!-- 업무일지 상세 내용 -->
    <div v-else>
      <hr class="my-4" />
      <div class="mb-3"><strong>소속 팀:</strong> {{ full.teamName }}</div>
      <div class="mb-3"><strong>업무 내용:</strong> {{ full.workContent || '없음' }}</div>
      <div class="mb-3"><strong>특이 사항:</strong> {{ full.note || '없음' }}</div>
      <div class="mb-3"><strong>익일 계획:</strong> {{ full.planContent || '없음' }}</div>
    </div>
  </section>
</template>

<style scoped>
  @keyframes pulse {
    0% {
      opacity: 0.4;
    }
    50% {
      opacity: 1;
    }
    100% {
      opacity: 0.4;
    }
  }
  .animate-pulse {
    animation: pulse 1.5s ease-in-out infinite;
  }
</style>
