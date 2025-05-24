<template>
  <div class="profile-card-vertical">
    <ProfileUpload
      v-if="isEditMode"
      v-model:model-value="profilePreview"
      @update:file="onFileUpdate"
    />
    <div v-else class="profile-image-wrapper">
      <img :src="profilePreview || defaultImage" class="profile-img" alt="유저 프로필" />
    </div>

    <div class="user-field">
      <label v-if="isEditMode" for="userName">이름</label>
      <input
        v-if="isEditMode"
        id="userName"
        v-model="localUser.userName"
        type="text"
        @input="updateUser"
      />
      <span v-else>{{ user.userName }}</span>
    </div>

    <div class="user-field">
      <label v-if="isEditMode" for="phoneNumber">전화번호</label>
      <input
        v-if="isEditMode"
        id="phoneNumber"
        v-model="localUser.phoneNumber"
        type="text"
        @input="updateUser"
      />
      <span v-else>{{ user.phoneNumber }}</span>
    </div>

    <div class="user-email">
      <span>{{ user.email }}</span>
    </div>

    <div v-if="isEditMode" class="button-group">
      <button class="btn btn-primary" type="button" @click="$emit('submit')">저장하기</button>
    </div>

    <div v-else class="button-group">
      <button class="btn btn-outline" @click="$emit('edit-user')">회원 정보 수정</button>
      <button class="btn btn-outline" @click="$emit('change-password')">비밀번호 변경</button>
    </div>

    <div v-if="!isEditMode" class="withdraw">
      <button class="btn-withdraw" @click="$emit('withdraw')">회원탈퇴</button>
    </div>
  </div>
</template>

<script setup>
  import { ref, reactive, watch } from 'vue';
  import ProfileUpload from '@/features/user/components/ProfileUpload.vue';

  const props = defineProps({
    user: {
      type: Object,
      required: true,
    },
    isEditMode: {
      type: Boolean,
      default: false,
    },
  });

  const emit = defineEmits(['update:user', 'submit']);

  const defaultImage = '/assets/image/profile-default.png';

  const localUser = reactive({
    userName: props.user.userName || '',
    phoneNumber: props.user.phoneNumber || '',
    thumbnailUrl: props.user.thumbnailUrl || '',
  });

  const profilePreview = ref(props.user.thumbnailUrl || '');
  const profileFile = ref(null);

  watch(
    () => props.user,
    val => {
      localUser.userName = val.userName || '';
      localUser.phoneNumber = val.phoneNumber || '';
      localUser.thumbnailUrl = val.thumbnailUrl || '';
      profilePreview.value = val.thumbnailUrl || '';
      profileFile.value = null;
    },
    { immediate: true, deep: true }
  );

  const updateUser = () => {
    emit('update:user', {
      ...localUser,
      profileImage: profileFile.value,
    });
  };

  const onFileUpdate = file => {
    profileFile.value = file;
    updateUser();
  };
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

  .user-field {
    width: 100%;
    max-width: 240px;
    display: flex;
    flex-direction: column;
    align-items: center;
    margin-bottom: 16px;
  }

  .user-field label {
    font-size: 13px;
    color: var(--color-gray-600);
    margin-bottom: 6px;
    align-self: flex-start;
  }

  .user-email {
    font-size: 14px;
    color: var(--color-gray-700);
    margin-bottom: 32px;
    text-align: center;
  }

  input[type='text'] {
    font-size: 15px;
    border: 1px solid var(--color-gray-300);
    border-radius: 6px;
    padding: 6px 10px;
    width: 100%;
    max-width: 240px;
    text-align: center;
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

  .button-group .btn:hover {
    background-color: var(--color-primary-100);
    border-color: var(--color-primary);
    color: var(--color-primary-300);
  }

  .btn-primary {
    background-color: var(--color-primary);
    border: none;
    color: white;
  }
  .btn-primary:hover {
    background-color: var(--color-primary-600);
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
  }
</style>
