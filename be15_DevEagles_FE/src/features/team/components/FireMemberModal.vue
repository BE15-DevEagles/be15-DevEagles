<template>
  <Teleport to="body">
    <!-- 추방 확인 모달 -->
    <BaseModal v-model="isOpen" title="팀원 추방 확인">
      <p class="modal-text">
        정말 해당 팀원을 추방하시겠습니까?<br />
        추방된 팀원은 다시 초대할 수 없습니다.
      </p>
      <template #footer>
        <BaseButton type="error" @click="cancel">취소</BaseButton>
        <BaseButton type="primary" @click="confirmFire">추방</BaseButton>
      </template>
    </BaseModal>

    <!-- 상태 모달 -->
    <BaseModal v-model="showStatusModal" :title="modalTitle">
      <p class="modal-text">{{ modalMessage }}</p>
      <template #footer>
        <BaseButton :type="isError ? 'error' : 'primary'" @click="showStatusModal = false"
          >확인</BaseButton
        >
      </template>
    </BaseModal>
  </Teleport>
</template>

<script setup>
  import { ref, watch } from 'vue';
  import BaseModal from '@/components/common/components/BaseModal.vue';
  import BaseButton from '@/components/common/components/BaseButton.vue';
  import { fireTeamMember } from '@/features/team/api/team';

  // ✅ email을 props로 받도록 수정
  const props = defineProps({
    modelValue: Boolean,
    userId: Number, // UI용 (선택 사항)
    userEmail: String, // ✅ 실제 API 호출에 사용
    teamId: Number,
  });
  const emit = defineEmits(['update:modelValue', 'kicked']);

  // 모달 열림/닫힘 상태 동기화
  const isOpen = ref(props.modelValue);
  watch(
    () => props.modelValue,
    val => (isOpen.value = val)
  );
  watch(isOpen, val => emit('update:modelValue', val));

  // 상태 모달
  const showStatusModal = ref(false);
  const modalTitle = ref('');
  const modalMessage = ref('');
  const isError = ref(false);

  const cancel = () => {
    isOpen.value = false;
  };

  const confirmFire = async () => {
    try {
      await fireTeamMember(props.teamId, props.userEmail); // ✅ 이메일로 API 호출
      modalTitle.value = '추방 완료';
      modalMessage.value = '팀원이 성공적으로 추방되었습니다.';
      isError.value = false;
      emit('kicked');
      isOpen.value = false;
    } catch (err) {
      isError.value = true;
      modalTitle.value = '추방 실패';
      modalMessage.value = err?.response?.data?.message || '알 수 없는 오류가 발생했습니다.';
    } finally {
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
</style>
