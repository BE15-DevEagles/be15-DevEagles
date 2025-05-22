<template>
  <div>
    <BaseButton @click="showModal = true">모달 열기</BaseButton>

    <BaseModal v-model="showModal" title="사용자 정보">
      <form @submit.prevent="submitForm">
        <BaseInput v-model="username" label="사용자명" />
        <BaseInput v-model="email" label="이메일" type="email" :error="emailError" />
      </form>
      <template #footer>
        <BaseButton type="secondary" @click="showModal = false">취소</BaseButton>
        <BaseButton type="primary" @click="submitForm">저장</BaseButton>
      </template>
    </BaseModal>

    <BasePagination v-model:current-page="currentPage" :total-pages="10" />
  </div>
</template>

<script setup>
  import { ref } from 'vue';
  import BaseModal from '@/components/common/components/BaseModal.vue';
  import BaseButton from '@/components/common/components/BaseButton.vue';
  import BaseInput from '@/components/common/components/BaseForm.vue';
  import BasePagination from '@/components/common/components/Pagaination.vue';

  const showModal = ref(false);
  const username = ref('');
  const email = ref('');
  const emailError = ref('');
  const currentPage = ref(1);

  function submitForm() {
    if (!email.value.includes('@')) {
      emailError.value = '이메일 형식이 올바르지 않습니다.';
      return;
    }
    emailError.value = '';
    showModal.value = false;
    // 여기에 실제 제출 로직 추가 가능
  }
</script>
