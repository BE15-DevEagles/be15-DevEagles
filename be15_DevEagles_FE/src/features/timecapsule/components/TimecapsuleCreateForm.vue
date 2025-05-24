<template>
  <div class="timecapsule-form-container">
    <form @submit.prevent="openConfirmModal">
      <div class="form-row">
        <label for="openDate">생성 날짜</label>
        <input id="openDate" v-model="form.openDate" type="date" :min="today" required />
      </div>
      <div class="form-row">
        <label for="content">타임캡슐 내용</label>
        <textarea
          id="content"
          v-model="form.timecapsuleContent"
          rows="8"
          placeholder="타임캡슐 내용을 입력하세요"
          required
        />
      </div>
      <button
        type="submit"
        class="submit-btn"
        :disabled="!teamId || !form.openDate || !form.timecapsuleContent || dateError"
      >
        타임캡슐 생성하기
      </button>
      <div v-if="!teamId" class="text-red-500 mt-2 text-sm">팀을 먼저 선택해주세요.</div>
      <div v-if="dateError" class="text-red-500 mt-2 text-sm">
        생성 날짜는 오늘 이후만 가능합니다.
      </div>
    </form>

    <!-- 모달창 -->
    <BaseModal v-model="showConfirm" title="타임캡슐 생성 확인">
      <template #default> 타임캡슐을 저장하시겠습니까? </template>
      <template #footer>
        <BaseButton @click="showConfirm = false">취소</BaseButton>
        <BaseButton type="primary" @click="onSubmitConfirm">확인</BaseButton>
      </template>
    </BaseModal>
  </div>
</template>

<script setup>
  import { ref, computed } from 'vue';
  import { useTimecapsule } from '../composables/useTimecapsule';
  import { useTeamStore } from '@/store/team';
  import BaseModal from '@/components/common/components/BaseModal.vue';
  import BaseButton from '@/components/common/components/BaseButton.vue';

  const teamStore = useTeamStore();
  const teamId = computed(() => teamStore.currentTeamId);

  const today = new Date().toISOString().split('T')[0];

  const form = ref({
    timecapsuleContent: '',
    openDate: '',
  });

  const dateError = computed(() => {
    return form.value.openDate && form.value.openDate <= today;
  });

  const { createTimecapsuleAction } = useTimecapsule();

  const showConfirm = ref(false);

  function openConfirmModal() {
    if (!teamId.value || dateError.value) {
      return;
    }
    showConfirm.value = true;
  }

  async function onSubmitConfirm() {
    showConfirm.value = false;
    try {
      await createTimecapsuleAction({
        ...form.value,
        teamId: teamId.value,
      });
      window.location.reload();
    } catch (e) {
      // 에러 핸들링 필요시 여기에 추가
    }
  }
</script>

<style scoped>
  .timecapsule-form-container {
    background: #fff;
    border-radius: 18px;
    box-shadow: 0 4px 32px rgba(0, 0, 0, 0.08);
    padding: 48px 40px;
    margin: 36px auto;
    max-width: 800px;
    min-width: 400px;
  }

  .form-row {
    margin-bottom: 28px;
  }

  label {
    font-weight: 600;
    margin-bottom: 10px;
    display: block;
    font-size: 1.15rem;
  }

  input,
  textarea {
    width: 100%;
    padding: 16px 18px;
    border: 1px solid #e0e0e0;
    border-radius: 10px;
    font-size: 1.15rem;
    margin-top: 6px;
  }

  .submit-btn {
    width: 100%;
    background: var(--color-primary-300, #257180);
    color: #fff;
    border: none;
    padding: 18px 0;
    border-radius: 10px;
    font-weight: 700;
    font-size: 1.2rem;
    cursor: pointer;
    transition: background 0.2s;
  }
  .submit-btn:hover {
    background: var(--color-primary-400, #257180);
  }

  .text-red-500 {
    color: #e74c3c;
  }
</style>
