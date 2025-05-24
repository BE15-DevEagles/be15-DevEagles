<script setup>
  import { onMounted, ref } from 'vue';
  import { useRoute, useRouter } from 'vue-router';
  import api from '@/api/axios.js';

  // 댓글 컴포넌트 준비
  import CommentForm from '@/features/comment/components/CommentForm.vue';
  import CommentList from '@/features/comment/view/CommentList.vue';

  const route = useRoute();
  const router = useRouter();
  const worklogId = route.params.id;

  const full = ref(null);
  const commentListKey = ref(0);

  function formatDate(dateStr) {
    return dateStr ? new Date(dateStr).toLocaleDateString() : '-';
  }

  function refreshComments() {
    commentListKey.value++;
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
  <section class="p-6 max-w-4xl mx-auto space-y-6">
    <!-- 제목 영역 -->
    <div v-if="full" class="border-b border-gray-300 pb-4">
      <h1 class="text-3xl font-bold text-dark mb-4 text-center">{{ full.summary }}</h1>
      <div class="flex justify-between px-2 text-sm text-gray-700 font-semibold mb-1">
        <span class="text-left">👤 작성자: {{ full.userName }}</span>
        <span class="text-right">🧑‍🤝‍🧑 소속 팀: {{ full.teamName }}</span>
      </div>
      <div class="text-sm text-gray-500 pl-2 mt-1">🗓 작성일: {{ formatDate(full.writtenAt) }}</div>
    </div>

    <!-- 로딩 중 메시지 -->
    <div v-if="!full" class="text-center py-10 animate-pulse text-gray-500">
      🔄 상세 내용을 불러오는 중...
    </div>

    <!-- 본문 내용 -->
    <div v-else class="space-y-8">
      <!-- 보고서 형식 카드 -->
      <div class="bg-gray-50 p-6 rounded-lg shadow border border-gray-200">
        <h2 class="text-lg font-bold text-gray-800 mb-4 border-b pb-2">📌 업무 내용</h2>
        <div class="text-gray-700 whitespace-pre-line leading-relaxed">
          {{ full.workContent || '없음' }}
        </div>
      </div>

      <div class="bg-gray-50 p-6 rounded-lg shadow border border-gray-200">
        <h2 class="text-lg font-bold text-gray-800 mb-4 border-b pb-2">⚠️ 특이 사항</h2>
        <div class="text-gray-700 whitespace-pre-line leading-relaxed">
          {{ full.note || '없음' }}
        </div>
      </div>

      <div class="bg-gray-50 p-6 rounded-lg shadow border border-gray-200">
        <h2 class="text-lg font-bold text-gray-800 mb-4 border-b pb-2">📅 익일 업무 계획</h2>
        <div class="text-gray-700 whitespace-pre-line leading-relaxed">
          {{ full.planContent || '없음' }}
        </div>
      </div>

      <!-- 댓글 영역 -->
      <div class="bg-white p-6 rounded-lg shadow border border-gray-200">
        <h2 class="text-lg font-bold text-gray-800 mb-4 border-b pb-2">💬 댓글</h2>
        <CommentForm :worklog-id="worklogId" @comment-added="refreshComments" />
        <CommentList :key="commentListKey" :worklog-id="worklogId" />
      </div>
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
