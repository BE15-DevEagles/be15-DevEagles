<!-- views/MyPageView.vue -->
<template>
  <div class="mypage-container">
    <h1 class="page-title">마이페이지</h1>
    <UserProfileCard @edit-user="goToEditUser" @change-password="goToChangePassword" />
  </div>
</template>

<script setup>
  import { useRouter } from 'vue-router';
  import UserProfileCard from '@/features/user/components/UserProfileCard.vue';

  const router = useRouter();

  const fetchUserDetail = async () => {};

  function goToEditUser() {
    router.push('/user/edit'); // 회원정보 수정 라우트
  }

  function goToChangePassword() {
    router.push('/user/password'); // 비밀번호 변경 라우트
  }
</script>

<style scoped>
  .mypage-container {
    padding: 40px 20px;
    max-width: 800px;
    margin: 0 auto;
  }

  .page-title {
    font-size: 24px;
    font-weight: bold;
    margin-bottom: 30px;
  }
</style>
