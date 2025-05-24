<template>
  <Teleport to="body">
    <BaseModal v-model="modalVisible" title="프로필 이미지 변경">
      <div class="profile-upload">
        <div class="profile-preview-wrapper">
          <div class="profile-preview" @click="triggerFileInput">
            <img :src="previewImage || defaultImage" alt="프로필 이미지" class="profile-img" />
          </div>
          <button v-if="previewImage" class="remove-image" @click.stop="removeImage">
            &times;
          </button>
        </div>
        <input
          id="profile"
          ref="fileInput"
          type="file"
          accept="image/*"
          hidden
          @change="handleImageUpload"
        />
      </div>

      <template #footer>
        <BaseButton type="secondary" @click="closeModal">취소</BaseButton>
        <BaseButton type="primary" @click="submit">저장</BaseButton>
      </template>
    </BaseModal>

    <BaseModal v-model="showStatusModal" :title="modalTitle">
      <p class="modal-text">{{ modalMessage }}</p>
      <template #footer>
        <BaseButton :type="isError ? 'error' : 'primary'" @click="handleFinalConfirm">
          확인
        </BaseButton>
      </template>
    </BaseModal>
  </Teleport>
</template>

<script setup>
  import { ref, defineProps, defineEmits, watch } from 'vue';
  import BaseModal from '@/components/common/components/BaseModal.vue';
  import BaseButton from '@/components/common/components/BaseButton.vue';
  import defaultImage from '/assets/image/profile-default.png';
  import { updateTeamThumbnail } from '@/features/team/api/team';

  const props = defineProps({
    modelValue: Boolean,
    currentUrl: String,
    teamId: Number,
  });

  const emit = defineEmits(['update:modelValue', 'submit']);

  const modalVisible = ref(props.modelValue);
  const fileInput = ref(null);
  const selectedFile = ref(null);
  const previewImage = ref(props.currentUrl || null);

  const showStatusModal = ref(false);
  const modalTitle = ref('');
  const modalMessage = ref('');
  const isError = ref(false);

  watch(
    () => props.modelValue,
    val => {
      modalVisible.value = val;
    }
  );
  watch(modalVisible, val => {
    emit('update:modelValue', val);
  });

  const triggerFileInput = () => {
    fileInput.value?.click();
  };

  const handleImageUpload = e => {
    const file = e.target.files[0];
    if (file) {
      selectedFile.value = file;
      const reader = new FileReader();
      reader.onload = e => {
        previewImage.value = e.target.result;
      };
      reader.readAsDataURL(file);
    }
  };

  const removeImage = () => {
    previewImage.value = null;
    selectedFile.value = null;
  };

  const closeModal = () => {
    emit('update:modelValue', false);
  };

  const submit = async () => {
    if (!selectedFile.value) {
      isError.value = true;
      modalTitle.value = '이미지 없음';
      modalMessage.value = '이미지를 선택해주세요.';
      showStatusModal.value = true;
      return;
    }

    try {
      await updateTeamThumbnail(props.teamId, selectedFile.value);
      emit('submit');
      emit('update:modelValue', false);
      isError.value = false;
      modalTitle.value = '변경 완료';
      modalMessage.value = '팀 썸네일이 성공적으로 변경되었습니다.';
    } catch (err) {
      console.error(err);
      isError.value = true;
      modalTitle.value = '변경 실패';
      modalMessage.value = '썸네일 변경에 실패했습니다.';
    } finally {
      showStatusModal.value = true;
    }
  };

  const handleFinalConfirm = () => {
    showStatusModal.value = false;
    if (!isError.value) {
      window.location.reload();
    }
  };
</script>

<style scoped>
  .modal-text {
    text-align: center;
    line-height: 1.6;
    margin: 20px 0;
  }
</style>
