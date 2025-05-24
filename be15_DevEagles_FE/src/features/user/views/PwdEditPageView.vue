<template>
  <div class="password-edit-view">
    <h1 class="page-title">비밀번호 변경</h1>

    <form class="form-box" @submit.prevent="handleSubmit">
      <BaseInput
        v-model="form.password"
        type="password"
        label="변경 비밀번호"
        placeholder="새로운 비밀번호를 입력해주세요"
        :error="errors.password"
      />
      <BaseInput
        v-model="form.confirmPassword"
        type="password"
        label="비밀번호 확인"
        placeholder="비밀번호를 다시 입력해주세요"
        :error="errors.confirmPassword"
      />

      <div class="button-group">
        <BaseButton type="primary" @click="handleSubmit">확인</BaseButton>
        <BaseButton type="secondary" outline @click="handleCancel">취소</BaseButton>
      </div>
    </form>

    <BaseModal v-model="isSuccessModalOpen" title="">
      <template #default>
        <p style="text-align: center; font-weight: 600">비밀번호 변경이 완료되었습니다.</p>
      </template>
      <template #footer>
        <div style="display: flex; justify-content: center">
          <BaseButton type="primary" @click="goToMyPage">확인</BaseButton>
        </div>
      </template>
    </BaseModal>
  </div>
</template>

<script setup>
  import { reactive, ref } from 'vue';
  import { useRouter } from 'vue-router';
  import BaseButton from '@/components/common/components/BaseButton.vue';
  import { editPassword } from '@/features/user/api/user.js';
  import BaseInput from '@/components/common/components/BaseForm.vue';
  import BaseModal from '@/components/common/components/BaseModal.vue';

  const router = useRouter();

  const form = reactive({
    password: '',
    confirmPassword: '',
  });

  const errors = reactive({
    password: '',
    confirmPassword: '',
  });

  const isSuccessModalOpen = ref(false);
  const goToMyPage = () => {
    isSuccessModalOpen.value = false;
    router.push('/mypage');
  };

  const validate = () => {
    let valid = true;

    if (!form.password) {
      errors.password = '비밀번호를 입력해주세요.';
      valid = false;
    } else if (!validatePassword(form.password)) {
      errors.password = '비밀번호는 영문자, 숫자, 특수문자를 포함한 8자 이상이어야 합니다.';
      valid = false;
    } else {
      errors.password = '';
    }

    if (!form.confirmPassword) {
      errors.confirmPassword = '비밀번호 확인을 입력해주세요.';
      valid = false;
    } else if (form.confirmPassword !== form.password) {
      errors.confirmPassword = '비밀번호가 일치하지 않습니다.';
      valid = false;
    } else {
      errors.confirmPassword = '';
    }

    return valid;
  };

  const validatePassword = password => {
    const regex = /^(?=.*[A-Za-z])(?=.*\d)(?=.*[!@#$%^&*()\-_=+{}[\]|;:'",.<>?/]).{8,}$/;
    return regex.test(password);
  };

  const handleSubmit = async () => {
    if (!validate()) return;

    try {
      await editPassword(form.password);
      isSuccessModalOpen.value = true;
    } catch (e) {
      console.error(e);
    }
  };

  const handleCancel = () => {
    router.back();
  };
</script>

<style scoped>
  .page-title {
    font-size: 22px;
    font-weight: bold;
    margin-bottom: 40px;
  }

  .password-edit-view {
    padding: 40px 20px;
    max-width: 440px;
    margin: 0 auto;
    background: white;
    border: 1px solid var(--color-gray-200);
    border-radius: 10px;
    margin-top: 90px;
  }

  .page-title {
    font-size: 20px;
    font-weight: bold;
    margin-bottom: 24px;
    text-align: center;
  }

  .form-box {
    display: flex;
    flex-direction: column;
    gap: 20px;
  }

  .button-group {
    display: flex;
    justify-content: center;
    gap: 16px;
    margin-top: 10px;
  }
</style>
