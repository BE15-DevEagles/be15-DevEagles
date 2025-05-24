<!-- views/MyPageView.vue -->
<template>
  <div class="mypage-container">
    <div v-if="isLoading">로딩 중...</div>
    <UserProfileCard
      v-else
      :user="user"
      @edit-user="handleEditUser"
      @change-password="handleChangePassword"
      @withdraw="handleWithdraw"
    />
  </div>
</template>

<script setup>
  import { useRouter } from 'vue-router';
  import UserProfileCard from '@/features/user/components/UserProfileCard.vue';
  import { onMounted, ref } from 'vue';
  import { mypage } from '@/features/user/api/user.js';
  import { useAuthStore } from '@/store/auth.js';

  const router = useRouter();
  const authStore = useAuthStore();

  const userId = ref(authStore.userId);
  const user = ref(null);
  const isLoading = ref(true);

  onMounted(async () => {
    try {
      console.log('회원 정보 불러오기 중');
      const res = await mypage();
      if (res.data.success) {
        user.value = res.data.data;
      }
    } catch (e) {
      console.error('회원 정보 불러오기 실패:', e);
    } finally {
      isLoading.value = false;
    }
  });

  function handleEditUser() {
    router.push('/user/edit'); // 회원정보 수정 라우트
  }

  function handleChangePassword() {
    router.push('/user/password'); // 비밀번호 변경 라우트
  }

  function handleWithdraw() {
    // 탈퇴 모달 또는 경고 띄우기
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
