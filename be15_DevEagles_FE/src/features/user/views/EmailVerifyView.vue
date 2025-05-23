<template>
  <div class="verify-container">
    <div class="verify-box">
      <img :src="Logo" alt="로고" class="logo center-logo" />
      <p class="message">{{ message }}</p>
      <BaseButton v-if="!verified" type="primary" class="verify-button" @click="verifyEmail">
        이메일 인증하기
      </BaseButton>
      <BaseButton v-if="verified" type="secondary" class="login-button" @click="goToLogin">
        로그인 페이지로 이동
      </BaseButton>
    </div>
  </div>
</template>

<script setup>
  import { ref } from 'vue';
  import { useRoute, useRouter } from 'vue-router';
  import BaseButton from '@/components/common/components/BaseButton.vue';
  import Logo from '/assets/image/logo-goody-with-text.png';
  import { verifyEmailCode } from '@/features/user/api/user.js';

  const route = useRoute();
  const router = useRouter();
  const message = ref('이메일 인증을 진행하려면 아래 버튼을 클릭해주세요.');
  const verified = ref(false);

  const verifyEmail = async () => {
    const email = route.query.email;
    const code = route.query.code;

    try {
      await verifyEmailCode({ email, authCode: code });
      message.value = '✅ 인증이 완료되었습니다!';
      verified.value = true;
    } catch (error) {
      console.error('인증 실패: ', error);
      message.value = '❌ 인증에 실패했습니다. 다시 시도해주세요.';
    }
  };

  const goToLogin = () => {
    router.push('/login');
  };
</script>

<style scoped>
  .verify-container {
    display: flex;
    justify-content: center;
    align-items: center;
    height: 100vh;
    background-color: var(--color-gray-100);
  }

  .verify-box {
    background: white;
    padding: 48px 32px;
    border-radius: 12px;
    text-align: center;
    box-shadow: var(--shadow-drop);
    width: 400px;
  }

  .logo {
    height: 48px;
    margin-bottom: 24px;
  }

  .center-logo {
    display: block;
    margin-left: auto;
    margin-right: auto;
  }

  .title {
    font-size: 20px;
    font-weight: 700;
    margin-bottom: 12px;
  }

  .message {
    font-size: 14px;
    margin-bottom: 24px;
    color: var(--color-gray-800);
  }

  .verify-button {
    width: 100%;
    margin-bottom: 12px;
  }

  .login-button {
    width: 100%;
    background-color: var(--color-gray-200);
    color: var(--color-gray-700);
  }
</style>
