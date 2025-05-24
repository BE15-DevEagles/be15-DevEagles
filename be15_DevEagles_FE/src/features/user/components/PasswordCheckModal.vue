<template>
  <BaseModal v-model="modalVisible" title="비밀번호 확인">
    <div class="check-modal-body">
      <p class="modal-message">비밀번호를 입력해주세요.</p>
      <BaseInput v-model="password" type="password" placeholder="비밀번호" :error="error" />
      <div class="button-group">
        <BaseButton type="error" @click="close">취소</BaseButton>
        <BaseButton type="primary" @click="confirm">확인</BaseButton>
      </div>
    </div>
  </BaseModal>
</template>

<script setup>
  import { ref, watch } from 'vue';
  import BaseModal from '@/components/common/components/BaseModal.vue';
  import BaseButton from '@/components/common/components/BaseButton.vue';
  import { verifyPassword } from '@/features/user/api/user.js';
  import BaseInput from '@/components/common/components/BaseForm.vue';

  const props = defineProps({
    modelValue: Boolean,
  });
  const emit = defineEmits(['update:modelValue', 'success']);

  const modalVisible = ref(props.modelValue);
  const password = ref('');
  const error = ref('');

  watch(
    () => props.modelValue,
    val => (modalVisible.value = val)
  );

  watch(modalVisible, val => {
    emit('update:modelValue', val);
    if (!val) {
      password.value = '';
      error.value = '';
    }
  });

  const close = () => {
    modalVisible.value = false;
  };

  const confirm = async () => {
    try {
      const res = await verifyPassword({ password: password.value });
      if (res.data.success) {
        emit('success');
        close();
      } else {
        error.value = '비밀번호가 일치하지 않습니다.';
      }
    } catch (e) {
      error.value = '오류가 발생했어요.';
      console.error(e);
    }
  };
</script>

<style scoped>
  .check-modal-body {
    padding: 16px;
    text-align: center;
  }
  .modal-message {
    margin-bottom: 16px;
    font-weight: 500;
    font-size: 16px;
  }
  .button-group {
    display: flex;
    justify-content: center;
    gap: 12px;
    margin-top: 16px;
  }
</style>
