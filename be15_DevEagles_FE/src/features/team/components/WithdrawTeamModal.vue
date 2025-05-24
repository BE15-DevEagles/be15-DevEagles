<template>
  <Teleport to="body">
    <!-- 팀 탈퇴 확인 모달 -->
    <BaseModal v-model="isOpen" title="팀 탈퇴 확인">
      <p class="modal-text">정말로 이 팀에서 탈퇴하시겠습니까?</p>

      <template #footer>
        <BaseButton type="error" @click="closeModal">취소</BaseButton>
        <BaseButton type="primary" :loading="loading" @click="handleWithdraw">확인</BaseButton>
      </template>
    </BaseModal>

    <!-- 상태 메시지 모달 -->
    <BaseModal v-model="showStatusModal" :title="modalTitle">
      <p class="modal-text">
        {{ modalMessage }}
      </p>
      <template #footer>
        <div class="modal-footer-buttons">
          <BaseButton :type="isError ? 'error' : 'primary'" @click="handleFinalConfirm">
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
  import { withdrawTeam } from '@/features/team/api/team';

  const props = defineProps({
    modelValue: { type: Boolean, required: true },
    teamId: { type: Number, required: true },
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

  const handleFinalConfirm = () => {
    showStatusModal.value = false;
    if (!isError.value) {
      window.location.href = '/';
    }
  };

  const handleWithdraw = async () => {
    loading.value = true;

    try {
      await withdrawTeam(props.teamId);

      isError.value = false;
      modalTitle.value = '탈퇴 완료';
      modalMessage.value = '팀에서 성공적으로 탈퇴하였습니다.';
      emit('success');
    } catch (err) {
      isError.value = true;
      modalTitle.value = '탈퇴 실패';
      modalMessage.value = err.response?.data?.message || '팀 탈퇴 중 오류가 발생했습니다.';
      emit('fail', modalMessage.value);
    } finally {
      loading.value = false;
      isOpen.value = false;
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
