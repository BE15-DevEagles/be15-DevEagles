<script setup>
  import { ref, computed } from 'vue';
  import BaseModal from '@/components/common/components/BaseModal.vue';
  import BaseForm from '@/components/common/components/BaseForm.vue';
  import BaseButton from '@/components/common/components/BaseButton.vue';

  // 부모 컴포넌트로부터 v-model 제어
  const props = defineProps({
    modelValue: {
      type: Boolean,
      required: true,
    },
  });

  const emit = defineEmits(['update:modelValue', 'submit']);

  // 상태
  const email = ref('');

  // ✅ computed로 v-model 바인딩
  const isOpen = computed({
    get: () => props.modelValue,
    set: val => emit('update:modelValue', val),
  });

  // 닫기
  const closeModal = () => {
    isOpen.value = false;
  };

  // 제출
  const handleSubmit = () => {
    if (!email.value) return;
    emit('submit', email.value);
    email.value = '';
    closeModal();
  };
</script>

<template>
  <Teleport to="body">
    <BaseModal v-model="isOpen" title="팀원 초대">
      <form @submit.prevent="handleSubmit">
        <BaseForm v-model="email" label="팀원 ID(이메일)" />
      </form>

      <template #footer>
        <BaseButton type="secondary" @click="closeModal">취소</BaseButton>
        <BaseButton type="primary" @click="handleSubmit">초대</BaseButton>
      </template>
    </BaseModal>
  </Teleport>
</template>
