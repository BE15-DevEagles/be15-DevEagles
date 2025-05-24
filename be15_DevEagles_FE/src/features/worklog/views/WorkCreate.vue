<script setup>
  import { ref, computed, onMounted } from 'vue';
  import { useRoute, useRouter } from 'vue-router';
  import BaseButton from '@/components/common/components/BaseButton.vue';
  import BaseModal from '@/components/common/components/BaseModal.vue';
  import { generateSummary } from '@/features/worklog/api/worklog.js';
  import api from '@/api/axios';
  import { useTeamStore } from '@/store/team.js';

  const route = useRoute();
  const router = useRouter();
  const teamStore = useTeamStore();

  const form = ref({
    summary: '',
    content: '',
    notice: '',
    plan: '',
    date: '',
    username: route.query.username,
    teamname: '',
    teamId: Number(route.query.teamId),
  });

  const showSubmitModal = ref(false);
  const showCancelModal = ref(false);
  const loading = ref(false);
  const showTooltip = ref(false);

  const isFormComplete = computed(() => {
    return (
      form.value.summary &&
      form.value.content &&
      form.value.notice &&
      form.value.plan &&
      form.value.date &&
      form.value.username &&
      form.value.teamname
    );
  });

  onMounted(async () => {
    if (form.value.teamId) {
      await teamStore.setCurrentTeam(form.value.teamId);
      form.value.teamname = teamStore.currentTeam?.teamName || '가상팀';
    }
  });

  function openSubmitModal() {
    showSubmitModal.value = true;
  }

  function openCancelModal() {
    showCancelModal.value = true;
  }

  function formatDateTime(date) {
    return date ? `${date} 00:00:00` : null;
  }

  async function submit() {
    try {
      const payload = {
        summary: form.value.summary,
        workContent: form.value.content,
        note: form.value.notice,
        planContent: form.value.plan,
        writtenAt: formatDateTime(form.value.date),
      };

      await api.post(`/worklog/register/${form.value.teamId}`, payload);
      alert('등록이 완료되었습니다.');
      router.push('/worklog/my');
    } catch (error) {
      console.error('등록 실패:', error);
      alert('등록 중 오류가 발생했습니다.');
    } finally {
      showSubmitModal.value = false;
    }
  }

  function cancel() {
    showCancelModal.value = false;
    router.push('/worklog/my');
  }

  async function generateSummaryHandler() {
    try {
      loading.value = true;
      const response = await generateSummary({
        workContent: form.value.content,
        note: form.value.notice,
      });
      form.value.summary = response.data?.data?.summary || '';
    } catch (error) {
      console.error('요약 실패:', error);
      alert('AI 요약에 실패했습니다.');
    } finally {
      loading.value = false;
    }
  }

  function spellCheck() {
    // 추후 백엔드 맞춤법 검사 API 연동
  }
</script>

<template>
  <section class="p-6">
    <div class="mb-4">
      <label class="form-label font-semibold">업무일지 제목</label>
      <div class="flex items-center gap-2">
        <!-- 입력창 -->
        <input
          v-model="form.summary"
          class="input flex-1 min-w-0"
          placeholder="제목을 입력하세요"
        />

        <!-- 버튼 + ? 아이콘 (absolute 배치) -->
        <div class="relative">
          <!-- ? 아이콘 -->
          <div class="tooltip-container absolute -top-8 left-1/2 -translate-x-1/2">
            <span
              class="tooltip-icon"
              @mouseenter="showTooltip = true"
              @mouseleave="showTooltip = false"
              >?</span
            >
            <div class="tooltip-content left-align" :class="{ visible: showTooltip }">
              업무 내용과 특이사항을 기반으로 제목을 자동 생성해줍니다.
            </div>
          </div>

          <!-- AI 요약 버튼 -->
          <button
            class="btn btn-primary whitespace-nowrap px-4"
            :disabled="!form.content || !form.notice"
            @click="generateSummaryHandler"
          >
            AI 요약
          </button>
        </div>
      </div>
    </div>
    <div class="mb-4 flex gap-4 items-end">
      <div class="flex flex-col">
        <label class="form-label">작성일자</label>
        <input v-model="form.date" type="date" class="input" />
      </div>
      <div class="flex flex-col">
        <label class="form-label">이름</label>
        <input v-model="form.username" type="text" class="input" disabled />
      </div>
      <div class="flex flex-col">
        <label class="form-label">팀명</label>
        <input v-model="form.teamname" type="text" class="input" disabled />
      </div>
    </div>

    <div class="mb-4">
      <label class="form-label font-semibold">업무일지 내용</label>
      <textarea v-model="form.content" class="input h-32" />
    </div>

    <div class="mb-4">
      <label class="form-label font-semibold">특이사항</label>
      <textarea v-model="form.notice" class="input h-24" />
    </div>

    <div class="mb-4">
      <label class="form-label font-semibold">익일 업무계획</label>
      <textarea v-model="form.plan" class="input h-24" />
    </div>

    <div class="flex gap-3 mt-6">
      <BaseButton class="btn btn-primary" :disabled="!isFormComplete" @click="openSubmitModal">
        제출
      </BaseButton>

      <BaseButton class="btn btn-warning" :disabled="!isFormComplete" @click="spellCheck">
        맞춤법 검사
      </BaseButton>

      <BaseButton class="btn btn-danger" @click="openCancelModal"> 취소 </BaseButton>
    </div>

    <BaseModal v-model="showSubmitModal" title="제출 확인">
      <template #default>업무일지를 제출하시겠습니까?</template>
      <template #footer>
        <BaseButton @click="submit">확인</BaseButton>
        <BaseButton @click="showSubmitModal = false">취소</BaseButton>
      </template>
    </BaseModal>

    <BaseModal v-model="showCancelModal" title="작성 취소">
      <template #default>작성을 취소하시겠습니까?</template>
      <template #footer>
        <BaseButton class="btn btn-danger" @click="cancel">확인</BaseButton>
        <BaseButton class="btn" @click="showCancelModal = false">계속 작성</BaseButton>
      </template>
    </BaseModal>

    <div v-if="loading" class="loading-overlay">
      <div class="loading-spinner"></div>
    </div>
  </section>
</template>

<style scoped>
  .tooltip-container {
    position: relative;
    display: inline-block;
  }

  .tooltip-icon {
    width: 1rem;
    height: 1rem;
    background-color: var(--color-info-400);
    color: white;
    border-radius: 50%; /* 완전한 원형 */
    display: flex; /* flex로 중앙 정렬 */
    align-items: center;
    justify-content: center;
    font-weight: bold;
    cursor: pointer;
    font-size: 1rem;
  }

  .tooltip-content {
    position: absolute;
    top: 50%;
    transform: translateY(-50%);
    background-color: var(--color-gray-800);
    color: white;
    padding: 0.4rem 0.6rem;
    border-radius: 0.5rem;
    font-size: 0.75rem;
    white-space: nowrap;
    opacity: 0;
    visibility: hidden;
    transition:
      opacity 0.2s ease,
      visibility 0.2s ease;
    z-index: 10;
  }

  .tooltip-content.left-align {
    right: 110%;
    left: auto;
  }

  .tooltip-content.visible {
    opacity: 1;
    visibility: visible;
  }

  .loading-overlay {
    position: fixed;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    background-color: rgba(128, 128, 128, 0.5);
    z-index: 9999;
    display: flex;
    justify-content: center;
    align-items: center;
  }
  .loading-spinner {
    width: 3rem;
    height: 3rem;
    border: 6px solid var(--color-gray-100);
    border-top: 6px solid var(--color-primary-main);
    border-radius: 50%;
    animation: spin 1s linear infinite;
  }
  @keyframes spin {
    to {
      transform: rotate(360deg);
    }
  }
  .btn-warning {
    background-color: var(--color-warning-500); /* 진한 색을 기본값으로! */
    color: var(--color-neutral-white);
  }

  .btn-warning:hover {
    background-color: var(--color-warning-600);
  }

  .btn-danger {
    background-color: var(--color-error-500); /* 기본 상태부터 진하게 */
    color: var(--color-neutral-white);
  }

  .btn-danger:hover {
    background-color: var(--color-error-600);
  }
</style>
