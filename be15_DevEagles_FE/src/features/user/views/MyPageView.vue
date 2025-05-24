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
    <PasswordCheckModal
      v-if="showPasswordCheck && routeUrl"
      v-model="showPasswordCheck"
      @success="router.push(routeUrl)"
    />
    <UserWithdrawModal v-model="showWithDrawCheck" />
  </div>
</template>

<script setup>
  import { useRouter } from 'vue-router';
  import UserProfileCard from '@/features/user/components/UserProfileCard.vue';
  import { onMounted, ref } from 'vue';
  import PasswordCheckModal from '@/features/user/components/PasswordCheckModal.vue';
  import UserWithdrawModal from '@/features/user/components/UserWithdrawModal.vue';
  import { mypage } from '@/features/user/api/user.js';

  const router = useRouter();

  const isLoading = ref(true);
  const user = ref({
    userName: '',
    phoneNumber: '',
    thumbnailUrl: '',
    email: '',
  });

  onMounted(async () => {
    try {
      const res = await mypage();
      if (res.data.success) {
        user.value = res.data.data;
        console.log(user.value.thumbnailUrl);
      }
    } catch (e) {
      console.error('회원 정보 불러오기 실패:', e);
    } finally {
      isLoading.value = false;
    }
  });

  const showPasswordCheck = ref(false);
  const routeUrl = ref(null);
  const handleEditUser = () => {
    routeUrl.value = '/mypage/edit';
    showPasswordCheck.value = true;
  };

  const handleChangePassword = () => {
    routeUrl.value = '/mypage/editpwd';
    showPasswordCheck.value = true;
  };

  const showWithDrawCheck = ref(false);
  function handleWithdraw() {
    showWithDrawCheck.value = true;
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
