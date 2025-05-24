<template>
  <div class="user-edit-view">
    <h1 class="page-title">회원 정보 수정</h1>
    <UserProfileCard
      :user="user"
      is-edit-mode
      :errors="errors"
      @update:user="handleUserChange"
      @submit="handleSave"
    />

    <BaseModal v-model="isSuccessModalOpen" title="">
      <template #default>
        <p style="text-align: center; font-weight: 600">회원 정보가 수정되었습니다.</p>
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
  import { reactive, onMounted, ref } from 'vue';
  import { useRouter } from 'vue-router';
  import UserProfileCard from '@/features/user/components/UserProfileCard.vue';
  import { mypage, updateUserInfo } from '@/features/user/api/user.js';
  import BaseButton from '@/components/common/components/BaseButton.vue';
  import BaseModal from '@/components/common/components/BaseModal.vue';

  const router = useRouter();

  const user = reactive({
    userName: '',
    phoneNumber: '',
    thumbnailUrl: '',
    profileImage: null,
  });

  const originalUser = reactive({
    userName: '',
    phoneNumber: '',
  });

  const errors = reactive({
    userName: '',
    phoneNumber: '',
  });

  onMounted(async () => {
    try {
      const res = await mypage();
      if (res.data.success) {
        const u = res.data.data;
        user.userName = u.userName || '';
        user.phoneNumber = autoFormatPhone(u.phoneNumber || '');
        user.thumbnailUrl = u.thumbnailUrl || '';
        user.profileImage = null;
        originalUser.userName = u.userName || '';
        originalUser.phoneNumber = u.phoneNumber || '';
      }
    } catch (e) {
      console.error(e);
    }
  });

  const isSuccessModalOpen = ref(false);
  const goToMyPage = () => {
    isSuccessModalOpen.value = false;
    router.push('/mypage');
  };

  const handleUserChange = payload => {
    console.log('[변경된 사용자 정보]', payload);
    Object.assign(user, payload);
    errors.userName = '';
    errors.phoneNumber = '';
  };

  const autoFormatPhone = value => {
    const cleaned = value.replace(/[^0-9]/g, '');
    if (cleaned.length === 11) {
      return cleaned.replace(/(\d{3})(\d{4})(\d{4})/, '$1-$2-$3');
    } else if (cleaned.length === 10) {
      return cleaned.replace(/(\d{3})(\d{3,4})(\d{4})/, '$1-$2-$3');
    }
    return value;
  };

  const removeHyphenPhone = value => value.replace(/-/g, '');

  const validate = () => {
    let valid = true;

    const trimmedName = typeof user.userName === 'string' ? user.userName.trim() : '';
    let trimmedPhone = typeof user.phoneNumber === 'string' ? user.phoneNumber.trim() : '';

    if (trimmedName === '') {
      errors.userName = '이름을 입력해주세요.';
      valid = false;
    } else {
      errors.userName = '';
    }

    const formattedPhone = autoFormatPhone(trimmedPhone);
    const phoneRegex = /^01[0|1|6|7|8|9]-\d{3,4}-\d{4}$/;

    if (!phoneRegex.test(formattedPhone)) {
      errors.phoneNumber = '유효한 전화번호 형식을 입력해주세요. (예: 010-1234-5678)';
      valid = false;
    } else {
      errors.phoneNumber = '';
    }

    return valid;
  };

  const handleSave = async () => {
    if (!validate()) {
      console.log('❌ 유효성 검사 실패');
      return;
    }

    const isUnchanged =
      user.userName === originalUser.userName &&
      removeHyphenPhone(user.phoneNumber) === originalUser.phoneNumber;

    try {
      const formData = new FormData();
      const requestPayload = {
        userName: user.userName,
        phoneNumber: removeHyphenPhone(user.phoneNumber),
      };

      formData.append(
        'request',
        new Blob([JSON.stringify(requestPayload)], {
          type: 'application/json',
        })
      );

      if (user.profileImage instanceof File) {
        formData.append('profile', user.profileImage);
      }
      await updateUserInfo(formData);
      isSuccessModalOpen.value = true;
    } catch (e) {
      console.error('[업데이트 오류]', e);
    }
  };
</script>

<style scoped>
  .user-edit-view {
    padding: 40px 20px;
    max-width: 500px;
    margin: 0 auto;
  }

  .page-title {
    font-size: 22px;
    font-weight: bold;
    margin-bottom: 32px;
  }

  .error-text {
    color: var(--color-error-500);
    font-size: 13px;
    margin-top: 6px;
    text-align: left;
  }
</style>
