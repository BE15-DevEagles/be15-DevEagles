<template>
  <div class="profile-card-vertical">
    <div class="profile-image-wrapper">
      <img :src="profileImage || defaultImage" alt="프로필 이미지" class="profile-img" />
    </div>

    <div class="user-name">{{ user.userName }}</div>
    <div class="user-phone">{{ user.phoneNumber }}</div>
    <div class="user-email">
      <span>{{ user.email }}</span>
    </div>

    <div class="button-group">
      <button class="btn btn-outline" @click="$emit('edit-user')">회원 정보 수정</button>
      <button class="btn btn-outline" @click="$emit('change-password')">비밀번호 변경</button>
    </div>

    <div class="withdraw">
      <button class="btn-withdraw" @click="$emit('withdraw')">회원탈퇴</button>
    </div>
  </div>
</template>

<script setup>
  import { ref } from 'vue';
  import { useAuthStore } from '@/store/auth.js';

  const authStore = useAuthStore();
  const defaultImage = '/assets/image/profile-default.png';
  const profileImage = ref(authStore.userThumbnailUrl);

  defineProps({
    user: {
      type: Object,
      required: true,
    },
  });
</script>

<style scoped>
  .profile-card-vertical {
    display: flex;
    flex-direction: column;
    align-items: center;
    border: 1px solid var(--color-gray-200);
    padding: 40px 24px;
    border-radius: 16px;
    background-color: #fff;
    width: 100%;
    max-width: 400px;
    margin: 0 auto;
    font-family: 'Noto Sans KR', sans-serif;
    color: var(--color-gray-800);
  }

  .profile-image-wrapper {
    width: 100px;
    height: 100px;
    border-radius: 50%;
    overflow: hidden;
    margin-bottom: 24px;
    border: 1px solid var(--color-gray-300);
  }

  .profile-img {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }

  .user-name {
    font-size: 20px;
    font-weight: 600;
    margin-bottom: 8px;
  }

  .user-phone {
    font-size: 15px;
    color: var(--color-gray-600);
    margin-bottom: 4px;
  }

  .user-email {
    font-size: 14px;
    color: var(--color-gray-700);
    margin-bottom: 32px;
  }

  .button-group {
    display: flex;
    flex-direction: column;
    gap: 14px;
    width: 100%;
    align-items: center;
    margin-bottom: 24px;
  }

  .button-group .btn {
    width: 100%;
    max-width: 240px;
    padding: 10px 16px;
    font-size: 14px;
    font-weight: 500;
    border-radius: 8px;
    transition: all 0.2s ease-in-out;
    border: 1px solid var(--color-gray-300);
    background-color: white;
    color: var(--color-gray-800);
    cursor: pointer;
  }

  /* ✅ hover 시 확실히 보이도록 강조 */
  .button-group .btn:hover {
    background-color: var(--color-primary-100);
    border-color: var(--color-primary);
    color: var(--color-primary-300);
  }

  .btn-withdraw {
    font-size: 13px;
    color: var(--color-gray-400);
    background: none;
    border: none;
    cursor: pointer;
    transition: color 0.2s;
    padding: 4px;
  }

  .btn-withdraw:hover {
    color: var(--color-error-400);
    cursor: pointer;
  }
</style>
