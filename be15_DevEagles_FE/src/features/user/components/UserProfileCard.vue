<template>
  <div class="profile-card">
    <div class="profile-left">
      <div class="profile-image-wrapper" @click="triggerFileInput">
        <img :src="previewImage || defaultImage" alt="프로필 이미지" class="profile-img" />
        <div class="upload-overlay">변경</div>
        <input ref="fileInput" type="file" accept="image/*" hidden @change="handleImageUpload" />
      </div>
    </div>

    <div class="profile-right">
      <div class="user-name">{{ user.name }}</div>
      <div class="user-phone">{{ user.phone }}</div>
      <div class="user-email">
        <a :href="`mailto:${user.email}`">{{ user.email }}</a>
      </div>

      <div class="button-group">
        <button class="btn btn-outline" @click="$emit('edit-user')">회원 정보 수정</button>
        <button class="btn btn-outline" @click="$emit('change-password')">비밀번호 변경</button>
      </div>

      <div class="withdraw">
        <button class="btn btn-link disabled" disabled>회원탈퇴</button>
      </div>
    </div>
  </div>
</template>

<script setup>
  import { ref, reactive } from 'vue';

  const defaultImage = '/assets/image/profile-default.png';
  const previewImage = ref('');
  const fileInput = ref(null);

  const user = reactive({
    name: '김이글',
    phone: '010-1234-5678',
    email: 'deveagles@email.com',
  });

  function triggerFileInput() {
    fileInput.value.click();
  }

  function handleImageUpload(event) {
    const file = event.target.files[0];
    if (!file) return;

    const reader = new FileReader();
    reader.onload = e => {
      previewImage.value = e.target.result;
    };
    reader.readAsDataURL(file);
  }
</script>

<style scoped>
  .profile-card {
    display: flex;
    gap: 24px;
    padding: 24px;
    border: 1px solid var(--color-gray-200);
    border-radius: 12px;
    background-color: #fff;
    max-width: 700px;
    margin: 0 auto;
    align-items: center;
  }

  .profile-left {
    flex-shrink: 0;
  }

  .profile-image-wrapper {
    position: relative;
    width: 96px;
    height: 96px;
    border-radius: 50%;
    overflow: hidden;
    cursor: pointer;
    border: 1px solid var(--color-gray-300);
  }

  .profile-img {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }

  .upload-overlay {
    position: absolute;
    bottom: 0;
    width: 100%;
    background: rgba(0, 0, 0, 0.4);
    color: white;
    font-size: 12px;
    text-align: center;
    padding: 4px 0;
  }

  .profile-right {
    flex: 1;
    display: flex;
    flex-direction: column;
    gap: 6px;
  }

  .user-name {
    font-size: 20px;
    font-weight: bold;
  }

  .user-phone {
    color: var(--color-gray-600);
    font-size: 15px;
  }

  .user-email a {
    color: var(--color-primary);
    font-size: 14px;
    text-decoration: underline;
  }

  .button-group {
    display: flex;
    gap: 12px;
    margin-top: 12px;
  }

  .withdraw {
    font-size: 13px;
    color: var(--color-gray-400);
    margin-top: 4px;
  }
</style>
