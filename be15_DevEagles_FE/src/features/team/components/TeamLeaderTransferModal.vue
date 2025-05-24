<template>
  <Teleport to="body">
    <!-- 팀장 양도 확인 모달 -->
    <BaseModal v-model="isOpen" title="팀장 양도 확인">
      <p class="modal-text">선택한 팀원에게 팀장 권한을 양도하시겠습니까?</p>

      <template #footer>
        <BaseButton type="error" @click="closeModal">취소</BaseButton>
        <BaseButton type="primary" :loading="loading" @click="handleSubmit">확인</BaseButton>
      </template>
    </BaseModal>

    <!-- 상태 메시지 모달 -->
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
  import { ref, computed } from 'vue';
  import BaseModal from '@/components/common/components/BaseModal.vue';
  import BaseButton from '@/components/common/components/BaseButton.vue';
  import { transferTeamLeader } from '@/features/team/api/team';

  const props = defineProps({
    modelValue: { type: Boolean, required: true },
    teamId: { type: Number, required: true },
    email: { type: String, required: true }, // 선택된 팀원 이메일
  });

  const emit = defineEmits(['update:modelValue', 'success', 'fail']);

  const isOpen = computed({
    get: () => props.modelValue,
    set: val => emit('update:modelValue', val),
  });

  const loading = ref(false);
  const showStatusModal = ref(false);
  const modalTitle = ref('');
  const modalMessage = ref('');
  const isError = ref(false);

  const closeModal = () => {
    isOpen.value = false;
  };

  const closeStatusModal = () => {
    showStatusModal.value = false;
    if (!isError.value) {
      closeModal();
      emit('success');
    }
  };

  const handleSubmit = async () => {
    loading.value = true;

    try {
      await transferTeamLeader(props.teamId, props.email);

      isError.value = false;
      modalTitle.value = '양도 완료';
      modalMessage.value = '팀장 권한이 성공적으로 양도되었습니다.';
    } catch (err) {
      isError.value = true;
      modalTitle.value = '양도 실패';
      modalMessage.value = err.response?.data?.message || '팀장 양도 중 문제가 발생했습니다.';
      emit('fail', modalMessage.value);
    } finally {
      loading.value = false;
      showStatusModal.value = true;
    }
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
