<template>
  <Teleport to="body">
    <!-- 팀 생성 모달 -->
    <BaseModal v-model="isOpen" title="팀 생성">
      <form @submit.prevent="handleSubmit">
        <BaseForm v-model="teamName" label="팀 이름" />
        <BaseForm v-model="description" label="소개" type="textarea" />
      </form>

      <template #footer>
        <BaseButton type="error" @click="closeModal">취소</BaseButton>
        <BaseButton type="primary" @click="handleSubmit">생성</BaseButton>
      </template>
    </BaseModal>

    <!-- 상태 메시지 공통 모달 -->
    <BaseModal v-model="showStatusModal" :title="modalTitle">
      <p class="modal-text">
        {{ modalMessage }}
      </p>
      <template #footer>
        <div class="modal-footer-buttons">
          <BaseButton :type="isError ? 'error' : 'primary'" @click="closeStatusModal">
            확인
          </BaseButton>
        </div>
      </template>
    </BaseModal>
  </Teleport>
</template>

<script setup>
  import { ref, watch } from 'vue';
  import BaseModal from '@/components/common/components/BaseModal.vue';
  import BaseButton from '@/components/common/components/BaseButton.vue';
  import BaseForm from '@/components/common/components/BaseForm.vue';
  import { createTeam } from '@/features/team/api/team';
  import { useTeamStore } from '@/store/team';

  const props = defineProps({
    modelValue: {
      type: Boolean,
      required: true,
    },
  });
  const emit = defineEmits(['update:modelValue', 'submit']);

  const isOpen = ref(props.modelValue);
  const teamName = ref('');
  const description = ref('');
  const teamStore = useTeamStore();

  // 상태 메시지 모달 상태
  const showStatusModal = ref(false);
  const modalTitle = ref('');
  const modalMessage = ref('');
  const isError = ref(false);

  const resetForm = () => {
    teamName.value = '';
    description.value = '';
  };

  // v-model 동기화
  watch(
    () => props.modelValue,
    val => {
      isOpen.value = val;
    }
  );

  watch(isOpen, val => {
    emit('update:modelValue', val);
    if (!val) resetForm();
  });

  // 제출 처리
  const handleSubmit = async () => {
    if (!teamName.value.trim()) {
      modalTitle.value = '입력 오류';
      modalMessage.value = '팀 이름을 입력해주세요.';
      isError.value = true;
      showStatusModal.value = true;
      return;
    }

    try {
      await createTeam({
        teamName: teamName.value,
        description: description.value,
      });

      await teamStore.fetchTeams();

      isError.value = false;
      modalTitle.value = '팀 생성 완료';
      modalMessage.value = '팀이 성공적으로 생성되었습니다.';
      isOpen.value = false;
    } catch (err) {
      console.error('팀 생성 실패:', err);
      isError.value = true;
      modalTitle.value = '팀 생성 실패';
      modalMessage.value = '팀 생성 중 문제가 발생했습니다. 다시 시도해주세요.';
    } finally {
      showStatusModal.value = true;
    }
  };

  const closeModal = () => {
    isOpen.value = false;
  };

  const closeStatusModal = () => {
    showStatusModal.value = false;
  };
</script>

<style scoped>
  .modal-text {
    text-align: center;
    line-height: 1.6;
    margin: 20px 0;
  }
  .modal-footer-buttons {
    display: flex;
    justify-content: flex-end;
    gap: 0.5rem;
  }
</style>
