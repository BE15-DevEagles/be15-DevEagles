<template>
  <BaseModal v-model="showModal" title="아이디찾기">
    <p class="modal-description">회원정보의 등록한 전화번호를 입력해주세요.</p>

    <div class="form-container">
      <div class="form-row">
        <label class="form-label">이름</label>
        <div class="form-input">
          <BaseInput v-model="userName" placeholder="이름을 입력해주세요" />
        </div>
      </div>

      <div class="form-row">
        <label class="form-label">전화번호</label>
        <div class="form-input">
          <BaseInput v-model="phoneNumber" placeholder="전화번호를 입력해주세요" />
        </div>
      </div>
    </div>

    <template #footer>
      <div class="footer-center">
        <BaseButton type="primary" @click="handleSubmit">인증하기</BaseButton>
      </div>
    </template>
  </BaseModal>
</template>

<script setup>
  import { ref, computed } from 'vue';
  import BaseModal from '@/components/common/components/BaseModal.vue';
  import BaseInput from '@/components/common/components/BaseForm.vue';
  import BaseButton from '@/components/common/components/BaseButton.vue';

  const props = defineProps({ modelValue: Boolean });
  const emit = defineEmits(['update:modelValue', 'submit']);

  const showModal = computed({
    get: () => props.modelValue,
    set: val => emit('update:modelValue', val),
  });

  const userName = ref('');
  const phoneNumber = ref('');

  const handleSubmit = () => {
    emit('submit', { userName: userName.value, phoneNumber: phoneNumber.value });
    userName.value = '';
    phoneNumber.value = '';
  };
</script>

<style scoped>
  .modal-description {
    text-align: center;
    font-size: 14px;
    color: var(--color-gray-500);
    margin-bottom: 20px;
  }

  .form-input {
    flex: 1;
    margin-left: 20px;
    margin-right: 50px;
  }

  .form-container {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 16px;
    margin: 16px 0;
  }

  .form-row {
    display: flex;
    align-items: center;
    width: 100%;
    max-width: 400px;
    gap: 16px;
  }

  .form-label {
    width: 80px;
    font-weight: 600;
    font-size: 14px;
    text-align: right;
    flex-shrink: 0;
  }

  .footer-center {
    display: flex;
    justify-content: center;
    padding-top: 8px;
    width: 100%;
  }
</style>
