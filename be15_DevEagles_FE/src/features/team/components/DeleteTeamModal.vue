<template>
  <Teleport to="body">
    <BaseModal v-model="isOpen" title="팀 삭제 확인">
      <p class="modal-text mb-4">
        팀을 삭제하려면 아래에 팀명을 정확히 입력하세요.<br />
        <strong class="text-[var(--color-gray-700)]">팀명: {{ teamNameFromParent }}</strong>
      </p>
      <BaseForm v-model="teamNameInput" label="팀명 확인" />

      <template #footer>
        <BaseButton type="error" @click="closeModal">취소</BaseButton>
        <BaseButton type="primary" :loading="loading" @click="handleDelete"> 삭제 </BaseButton>
      </template>
    </BaseModal>

    <BaseModal v-model="showStatusModal" :title="modalTitle">
      <p class="modal-text">{{ modalMessage }}</p>
      <template #footer>
        <BaseButton :type="isError ? 'error' : 'primary'" @click="handleFinalConfirm">
          확인
        </BaseButton>
      </template>
    </BaseModal>
  </Teleport>
</template>

<script setup>
  import { ref, computed, watch } from 'vue';
  import BaseModal from '@/components/common/components/BaseModal.vue';
  import BaseForm from '@/components/common/components/BaseForm.vue';
  import BaseButton from '@/components/common/components/BaseButton.vue';
  import { deleteTeam, getTeamDetail } from '@/features/team/api/team';

  const props = defineProps({
    modelValue: { type: Boolean, required: true },
    teamId: { type: Number, required: true },
  });

  const emit = defineEmits(['update:modelValue']);

  const isOpen = computed({
    get: () => props.modelValue,
    set: val => emit('update:modelValue', val),
  });

  const teamNameInput = ref('');
  const teamNameFromParent = ref('');
  const loading = ref(false);

  const showStatusModal = ref(false);
  const modalTitle = ref('');
  const modalMessage = ref('');
  const isError = ref(false);

  watch(isOpen, async val => {
    if (val) {
      teamNameInput.value = '';
      try {
        const data = await getTeamDetail(props.teamId);
        teamNameFromParent.value = data.teamName;
      } catch (e) {
        console.error('팀 정보 불러오기 실패:', e);
        teamNameFromParent.value = '';
        isError.value = true;
        modalTitle.value = '불러오기 실패';
        modalMessage.value = '팀 정보를 불러오지 못했습니다.';
        showStatusModal.value = true;
      }
    }
  });

  const closeModal = () => {
    isOpen.value = false;
  };

  const handleDelete = async () => {
    if (teamNameInput.value.trim() !== teamNameFromParent.value.trim()) {
      isError.value = true;
      modalTitle.value = '입력 오류';
      modalMessage.value = '팀명을 정확히 입력해주세요.';
      showStatusModal.value = true;
      return;
    }

    loading.value = true;

    try {
      await deleteTeam(props.teamId);
      isError.value = false;
      modalTitle.value = '삭제 완료';
      modalMessage.value = '팀이 성공적으로 삭제되었습니다.';
    } catch (err) {
      isError.value = true;
      modalTitle.value = '삭제 실패';
      modalMessage.value = err.response?.data?.message || '팀 삭제 중 오류가 발생했습니다.';
    } finally {
      loading.value = false;
      isOpen.value = false;
      showStatusModal.value = true;
    }
  };

  const handleFinalConfirm = () => {
    showStatusModal.value = false;
    if (!isError.value) {
      window.location.href = '/';
    }
  };
</script>

<style scoped>
  .modal-text {
    text-align: center;
    line-height: 1.6;
    margin: 20px 0;
  }
</style>
