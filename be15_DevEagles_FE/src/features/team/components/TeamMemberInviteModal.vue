<script setup>
  import { ref, computed } from 'vue';
  import BaseModal from '@/components/common/components/BaseModal.vue';
  import BaseForm from '@/components/common/components/BaseForm.vue';
  import BaseButton from '@/components/common/components/BaseButton.vue';
  import api from '@/api/axios';
  import { useRoute } from 'vue-router';

  const props = defineProps({
    modelValue: {
      type: Boolean,
      required: true,
    },
  });

  const emit = defineEmits(['update:modelValue', 'success', 'fail']);

  const email = ref('');
  const loading = ref(false);
  const errorMessage = ref('');

  const isOpen = computed({
    get: () => props.modelValue,
    set: val => emit('update:modelValue', val),
  });

  const route = useRoute();
  const teamId = computed(() => Number(route.params.teamId));

  const closeModal = () => {
    isOpen.value = false;
    email.value = '';
    errorMessage.value = '';
  };

  const handleSubmit = async () => {
    if (!email.value) return;

    loading.value = true;
    errorMessage.value = '';

    try {
      await api.post(`/team/members/${teamId.value}/invite`, { email: email.value });
      emit('success');
      alert('팀원 초대가 완료되었습니다.');
      closeModal();
    } catch (err) {
      const msg = err.response?.data?.message || '팀원 초대에 실패했습니다. 다시 시도해주세요.';
      errorMessage.value = msg;
      emit('fail', msg);
    } finally {
      loading.value = false;
    }
  };
</script>

<template>
  <Teleport to="body">
    <BaseModal v-model="isOpen" title="팀원 초대">
      <form @submit.prevent="handleSubmit">
        <BaseForm v-model="email" label="팀원 이메일" />
        <p v-if="errorMessage" class="text-sm text-red-500 mt-1">{{ errorMessage }}</p>
      </form>

      <template #footer>
        <BaseButton type="secondary" @click="closeModal">취소</BaseButton>
        <BaseButton type="primary" :loading="loading" @click="handleSubmit">초대</BaseButton>
      </template>
    </BaseModal>
  </Teleport>
</template>
