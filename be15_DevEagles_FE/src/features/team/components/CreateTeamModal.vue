<template>
  <Teleport to="body">
    <BaseModal v-model="isOpen" title="팀 생성">
      <form @submit.prevent="handleSubmit">
        <BaseForm v-model="teamName" label="팀 이름" />
        <BaseForm v-model="description" label="소개" type="textarea" />
      </form>

      <template #footer>
        <BaseButton type="secondary" @click="closeModal">취소</BaseButton>
        <BaseButton type="primary" @click="handleSubmit">생성</BaseButton>
      </template>
    </BaseModal>
  </Teleport>
</template>

<script setup>
  import { ref, watch } from 'vue';
  import BaseModal from '@/components/common/components/BaseModal.vue';
  import BaseButton from '@/components/common/components/BaseButton.vue';
  import BaseForm from '@/components/common/components/BaseForm.vue';
  import { createTeam } from '@/features/team/api/team';
  import { useTeamStore } from '@/store/team';

  const props = defineProps({
    modelValue: {
      type: Boolean,
      required: true,
    },
  });
  const emit = defineEmits(['update:modelValue', 'submit']);
  const isOpen = ref(props.modelValue);
  const teamName = ref('');
  const description = ref('');
  const teamStore = useTeamStore();

  // v-model 동기화
  watch(
    () => props.modelValue,
    val => {
      isOpen.value = val;
    }
  );
  watch(isOpen, val => {
    emit('update:modelValue', val);
  });

  // 제출 처리
  const handleSubmit = async () => {
    if (!teamName.value.trim()) {
      alert('팀 이름을 입력해주세요.');
      return;
    }

    try {
      await createTeam({
        teamName: teamName.value,
        description: description.value,
      });

      alert('팀 생성이 완료되었습니다!');

      await teamStore.fetchTeams();
      closeModal();
    } catch (err) {
      console.error('팀 생성 실패:', err);
      alert('팀 생성 중 문제가 발생했습니다.');
    }
  };

  const closeModal = () => {
    isOpen.value = false;
  };
</script>
