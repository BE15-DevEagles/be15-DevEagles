<template>
  <BaseModal v-model="modalVisible" title="프로필 이미지 변경">
    <div class="profile-upload">
      <div class="profile-preview-wrapper">
        <div class="profile-preview" @click="triggerFileInput">
          <img :src="previewImage || defaultImage" alt="프로필 이미지" class="profile-img" />
        </div>
        <button v-if="previewImage" class="remove-image" @click.stop="removeImage">&times;</button>
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
      <BaseButton type="secondary" @click="emit('update:modelValue', false)">취소</BaseButton>
      <BaseButton type="primary" @click="submit">저장</BaseButton>
    </template>
  </BaseModal>
</template>

<script setup>
  import { ref, defineProps, defineEmits, watch } from 'vue';
  import BaseModal from '@/components/common/components/BaseModal.vue';
  import BaseButton from '@/components/common/components/BaseButton.vue';
  import defaultImage from '/assets/image/profile-default.png';
  import { updateTeamThumbnail } from '@/features/team/api/team'; // ✅ API import
  import { useRoute } from 'vue-router';

  const props = defineProps({
    modelValue: Boolean,
    currentUrl: String,
  });

  const emit = defineEmits(['update:modelValue', 'submit']);

  const modalVisible = ref(props.modelValue);
  const fileInput = ref(null);
  const selectedFile = ref(null);
  const previewImage = ref(props.currentUrl || null);

  const route = useRoute();
  const teamId = Number(route.params.teamId); // ✅ teamId 추출

  // v-model 동기화
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

  const submit = async () => {
    if (!selectedFile.value) {
      alert('이미지를 선택해주세요.');
      return;
    }

    try {
      await updateTeamThumbnail(teamId, selectedFile.value); // ✅ API 호출
      emit('submit'); // 부모에서 fetchTeamInfo 호출됨
      emit('update:modelValue', false); // 모달 닫기
      alert('팀 썸네일이 변경되었습니다.');
      window.location.reload();
    } catch (err) {
      alert('썸네일 변경에 실패했습니다.');
      console.error(err);
    }
  };
</script>
