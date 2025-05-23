<template>
  <div class="timecapsule-form-container">
    <form @submit.prevent="onSubmit">
      <div class="form-row">
        <label for="openDate">생성 날짜</label>
        <input id="openDate" v-model="form.openDate" type="date" required />
      </div>
      <div class="form-row">
        <label for="content">타임캡슐 내용</label>
        <textarea
          id="content"
          v-model="form.timecapsuleContent"
          rows="6"
          placeholder="타임캡슐 내용을 입력하세요"
          required
        />
      </div>
      <button type="submit" class="submit-btn">타임캡슐 생성하기</button>
    </form>
  </div>
</template>

<script setup>
  import { ref } from 'vue';
  import { useTimecapsule } from '../composables/useTimecapsule';
  import { useTeamStore } from '@/store/team'; // Pinia store import

  const teamStore = useTeamStore();

  const { createTimecapsuleAction } = useTimecapsule();

  const form = ref({
    timecapsuleContent: '',
    openDate: '',
  });

  const onSubmit = async () => {
    try {
      await createTimecapsuleAction({
        ...form.value,
        teamId: teamStore.currentTeamId, // store에서 teamId 바로 사용
      });
      alert('타임캡슐이 성공적으로 생성되었습니다!');
      // 필요하다면 라우터 이동 등 추가
    } catch (e) {
      alert('생성 실패: ' + (e.response?.data?.message || e.message));
    }
  };
</script>

<style scoped>
  .timecapsule-form-container {
    background: #fff;
    border-radius: 16px;
    box-shadow: 0 2px 16px rgba(0, 0, 0, 0.06);
    padding: 32px 24px;
    margin: 24px auto;
    max-width: 480px;
  }

  .form-row {
    margin-bottom: 18px;
  }

  label {
    font-weight: 600;
    margin-bottom: 6px;
    display: block;
  }

  input,
  textarea {
    width: 100%;
    padding: 8px 12px;
    border: 1px solid #e0e0e0;
    border-radius: 8px;
    font-size: 1rem;
    margin-top: 4px;
  }

  .submit-btn {
    width: 100%;
    background: var(--color-primary-300, #257180);
    color: #fff;
    border: none;
    padding: 12px 0;
    border-radius: 8px;
    font-weight: 700;
    font-size: 1.1rem;
    cursor: pointer;
    transition: background 0.2s;
  }
  .submit-btn:hover {
    background: var(--color-primary-400, #257180);
  }
</style>
