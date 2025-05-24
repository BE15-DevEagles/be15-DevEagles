<template>
  <BaseModal v-model="showModal" title="정말로 탈퇴하시겠습니까?">
    <div class="modal-content">
      <p class="description">
        회원 탈퇴 시 한 달간 휴면계정으로 전환되며,
        <br />한 달 이내에 로그인 시 탈퇴가 철회됩니다.
      </p>
      <BaseInput
        v-model="password"
        type="password"
        placeholder="비밀번호를 입력해주세요"
        :error="error"
      />
    </div>

    <template #footer>
      <BaseButton type="error" @click="closeModal">취소</BaseButton>
      <BaseButton type="primary" @click="handleConfirm">확인</BaseButton>
    </template>
  </BaseModal>

  <BaseModal v-model="completed" title="GoodBye~👋🏼" :closable="false">
    <div class="modal-body center-content">탈퇴가 완료되었습니다.</div>
    <template #footer>
      <BaseButton type="primary" @click="goHome">확인</BaseButton>
    </template>
  </BaseModal>
</template>

<script setup>
  import { computed, ref, watch } from 'vue';
  import { useRouter } from 'vue-router';
  import BaseModal from '@/components/common/components/BaseModal.vue';
  import BaseButton from '@/components/common/components/BaseButton.vue';
  import { logout, verifyPassword, withdrawUser } from '@/features/user/api/user';
  import BaseInput from '@/components/common/components/BaseForm.vue';
  import { useAuthStore } from '@/store/auth.js';

  const props = defineProps({
    modelValue: {
      type: Boolean,
      required: true,
    },
  });
  const emit = defineEmits(['update:modelValue']);

  const showModal = computed({
    get: () => props.modelValue,
    set: val => emit('update:modelValue', val),
  });
  const completed = ref(false);
  const password = ref('');
  const error = ref('');
  const router = useRouter();
  const authStore = useAuthStore();

  const closeModal = () => {
    showModal.value = false;
    password.value = '';
    error.value = '';
  };

  const handleConfirm = async () => {
    if (!password.value.trim()) {
      error.value = '비밀번호를 입력해주세요.';
      return;
    }

    try {
      const res = await verifyPassword({ password: password.value });
      if (res.data.success) {
        await withdrawUser();
        await logout();
        authStore.clearAuth();
        showModal.value = false;
        completed.value = true;
      } else {
        error.value = '비밀번호가 일치하지 않습니다.';
      }
    } catch (e) {
      error.value = '요청 중 오류가 발생했어요.';
      console.error(e);
    }
  };

  const goHome = () => {
    completed.value = false;
    router.push('/login');
  };
</script>

<style scoped>
  .modal-content {
    text-align: center;
    padding: 10px 0;
  }
  .description {
    color: var(--color-gray-500);
    font-size: 14px;
    margin-bottom: 20px;
    line-height: 1.5;
  }
  .center-content {
    display: flex;
    justify-content: center;
    align-items: center;
    text-align: center;
    min-height: 80px;
  }
</style>
