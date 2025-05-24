<template>
  <div class="signup-container">
    <div class="signup-box">
      <h2 class="font-section-title signup-title">회원가입</h2>

      <!-- 프로필 이미지 업로드 -->
      <ProfileUpload v-model="previewImage" @update:file="selectedFile = $event" />

      <div class="form-fields">
        <!-- E-mail 입력 -->
        <div class="label-row">
          <label for="email">E-mail</label>
        </div>
        <BaseInput
          id="email"
          v-model="form.email"
          placeholder="가입하실 E-mail 주소를 입력해주세요."
          type="email"
          :error="errors.email"
          @blur="checkEmail"
          @focus="clearError('email')"
        />
        <p v-if="emailCheckMessage" class="validation-msg">{{ emailCheckMessage }}</p>

        <div class="label-row">
          <label for="password">비밀번호</label>
          <p class="password-rule">특수문자, 영문자, 숫자를 포함한 8자리 이상</p>
        </div>
        <BaseInput
          v-model="form.password"
          placeholder="비밀번호를 입력해주세요."
          type="password"
          :error="errors.password"
          @focus="clearError('password')"
        />

        <div class="label-row">
          <label for="userName">이름</label>
        </div>
        <BaseInput
          v-model="form.userName"
          placeholder="이름을 입력해주세요."
          type="text"
          :error="errors.userName"
          @focus="clearError('userName')"
        />

        <!-- 전화번호 -->
        <div class="label-row">
          <label for="phone">전화번호</label>
        </div>
        <BaseInput
          id="phone"
          v-model="form.phoneNumber"
          placeholder="전화번호를 입력해주세요."
          type="text"
          :error="errors.phoneNumber"
          @blur="checkPhone"
          @focus="clearError('phoneNumber')"
        />
        <p v-if="phoneCheckMessage" class="validation-msg">{{ phoneCheckMessage }}</p>

        <div class="submit-button">
          <BaseButton type="primary" class="full-width" @click="handleSubmit">회원 가입</BaseButton>
        </div>
      </div>
    </div>

    <BaseModal v-model="showConfirmModal" title="">
      <p class="modal-text">
        입력하신 이메일로 인증을<br />
        완료하시면 회원가입이 완료됩니다,
      </p>
      <template #footer>
        <div class="modal-footer-buttons">
          <BaseButton type="error" @click="showConfirmModal = false">취소</BaseButton>
          <BaseButton type="primary" @click="submit">확인</BaseButton>
        </div>
      </template>
    </BaseModal>
  </div>
</template>

<script setup>
  import { ref } from 'vue';
  import BaseInput from '@/components/common/components/BaseForm.vue';
  import BaseButton from '@/components/common/components/BaseButton.vue';
  import { signUp, checkDuplicate, sendAuth } from '@/features/user/api/user.js';
  import BaseModal from '@/components/common/components/BaseModal.vue';
  import { useRouter } from 'vue-router';
  import ProfileUpload from '@/features/user/components/ProfileUpload.vue';

  const router = useRouter();

  const form = ref({
    email: '',
    password: '',
    userName: '',
    phoneNumber: '',
  });

  const errors = ref({
    email: '',
    password: '',
    userName: '',
    phoneNumber: '',
  });

  const emailCheckMessage = ref('');
  const phoneCheckMessage = ref('');

  const emailChecked = ref(false);
  const phoneChecked = ref(false);

  const showConfirmModal = ref(false);

  const previewImage = ref(null);
  const selectedFile = ref(null);

  const clearError = field => {
    errors.value[field] = '';
  };

  const validate = () => {
    let valid = true;
    Object.keys(errors.value).forEach(key => (errors.value[key] = ''));

    if (!form.value.email) {
      errors.value.email = '이메일을 입력해주세요.';
      valid = false;
    } else if (!emailChecked.value) {
      errors.value.email = '이메일 중복 확인이 필요합니다.';
      valid = false;
    }

    const password = form.value.password;
    if (!password) {
      errors.value.password = '비밀번호를 입력해주세요.';
      valid = false;
    } else if (
      !/^(?=.*[A-Za-z])(?=.*\d)(?=.*[!@#$%^&*()_+\-=\\[\]{};':"\\|,.<>/?]).{8,}$/.test(password)
    ) {
      errors.value.password = '사용할 수 없는 비밀번호입니다.';
      valid = false;
    }

    if (!form.value.userName) {
      errors.value.userName = '이름을 입력해주세요.';
      valid = false;
    }

    if (!form.value.phoneNumber) {
      errors.value.phoneNumber = '전화번호를 입력해주세요.';
      valid = false;
    } else if (!phoneChecked.value) {
      errors.value.phoneNumber = '전화번호 중복 확인이 필요합니다.';
      valid = false;
    }

    return valid;
  };

  const handleSubmit = () => {
    emailCheckMessage.value = '';
    phoneCheckMessage.value = '';
    if (!validate()) return;
    showConfirmModal.value = true;
  };

  const submit = async () => {
    showConfirmModal.value = false;

    const jsonData = {
      email: form.value.email,
      password: form.value.password,
      userName: form.value.userName,
      phoneNumber: form.value.phoneNumber,
    };

    const formData = new FormData();
    formData.append('request', new Blob([JSON.stringify(jsonData)], { type: 'application/json' }));

    if (selectedFile.value) {
      formData.append('profile', selectedFile.value);
    }

    try {
      await signUp(formData);
      await sendAuth({ email: form.value.email });
      router.push('/login');
    } catch (err) {
      console.error('회원가입 실패 😢', err);
    }
  };

  const checkEmail = async () => {
    emailChecked.value = false;
    if (!form.value.email) {
      emailCheckMessage.value = '';
      return;
    }
    try {
      const res = await checkDuplicate({ email: form.value.email });
      if (res.data.data) {
        emailCheckMessage.value = '';
        emailChecked.value = true;
      } else {
        emailCheckMessage.value = '이미 사용 중인 이메일주소입니다.';
        emailChecked.value = false;
      }
    } catch (error) {
      console.log(error);
      emailCheckMessage.value = '중복 확인 중 오류가 발생했습니다.';
      emailChecked.value = false;
    }
  };

  const checkPhone = async () => {
    phoneChecked.value = false;
    if (!form.value.phoneNumber) {
      phoneCheckMessage.value = '';
      return;
    }
    try {
      const res = await checkDuplicate({ phoneNumber: form.value.phoneNumber });
      if (res.data.data) {
        phoneCheckMessage.value = '';
        phoneChecked.value = true;
      } else {
        phoneCheckMessage.value = '이미 사용 중인 전화번호입니다.';
        phoneChecked.value = false;
      }
    } catch (error) {
      console.log(error);
      phoneCheckMessage.value = '중복 확인 중 오류가 발생했습니다.';
      phoneChecked.value = false;
    }
  };
</script>

<style scoped>
  .signup-container {
    display: flex;
    flex-direction: column;
    align-items: center;
    padding: 60px 20px;
    background-color: var(--color-gray-50);
  }

  .signup-box {
    background: white;
    padding: 40px;
    border-radius: 16px;
    box-shadow: var(--shadow-drop);
    width: 560px;
    border: 1px solid var(--color-gray-200);
    display: flex;
    flex-direction: column;
    align-items: center;
  }

  .signup-title {
    text-align: center;
    margin-bottom: 24px;
  }

  .profile-upload {
    display: flex;
    justify-content: center;
    margin-bottom: 32px;
    position: relative;
  }

  .profile-preview-wrapper {
    position: relative;
    width: 120px;
    height: 120px;
  }

  .profile-preview {
    width: 120px;
    height: 120px;
    border-radius: 50%;
    overflow: hidden;
    border: 2px solid var(--color-gray-200);
    cursor: pointer;
    position: relative;
  }

  .profile-img {
    width: 100%;
    height: 100%;
    object-fit: cover;
    display: block;
  }

  .upload-overlay {
    position: absolute;
    bottom: 0;
    width: 100%;
    background-color: rgba(0, 0, 0, 0.5);
    color: white;
    font-size: 12px;
    text-align: center;
    padding: 4px 0;
    font-weight: 500;
  }

  .remove-image {
    position: absolute;
    top: -8px;
    right: -8px;
    background: rgba(0, 0, 0, 0.6);
    color: white;
    border: none;
    border-radius: 50%;
    width: 24px;
    height: 24px;
    font-size: 16px;
    line-height: 24px;
    text-align: center;
    cursor: pointer;
    z-index: 2;
  }

  .form-fields {
    width: 360px;
    display: flex;
    flex-direction: column;
    gap: 16px;
  }

  .label-row {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 4px;
    font-weight: 600;
    font-size: 14px;
    color: var(--color-gray-900);
  }

  .check-text {
    font-size: 12px;
    color: var(--color-gray-500);
    cursor: pointer;
    font-weight: 500;
  }

  .validation-msg {
    font-size: 12px;
    color: var(--color-error, red);
    margin-top: -8px;
    margin-bottom: 4px;
  }

  .submit-button {
    margin-top: 32px;
    width: 100%;
  }

  .full-width {
    width: 100%;
  }

  .password-rule {
    font-size: 12px;
    font-weight: normal;
    color: var(--color-gray-500);
    margin-left: 8px;
  }

  .modal-text {
    text-align: center;
    font-size: 16px;
    font-weight: 700;
    line-height: 1.5;
    padding: 16px 0;
  }
  .modal-footer-buttons {
    display: flex;
    justify-content: center;
    gap: 12px;
  }
</style>
