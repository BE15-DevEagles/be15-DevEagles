<template>
  <div class="profile-upload">
    <div class="profile-preview-wrapper">
      <div class="profile-preview" @click="triggerFileInput">
        <img :src="previewImage || defaultImg" alt="프로필 이미지" class="profile-img" />
        <div class="upload-overlay">{{ label }}</div>
      </div>
      <button
        v-if="previewImage && !previewImage.includes('profile-default.png')"
        class="remove-image"
        @click.stop="removeImage"
      >
        &times;
      </button>
    </div>
    <input ref="fileInput" type="file" accept="image/*" hidden @change="handleImageUpload" />
  </div>
</template>

<script setup>
  import { ref, watch, defineProps, defineEmits } from 'vue';

  const props = defineProps({
    modelValue: String,
    label: {
      type: String,
      default: '이미지 등록',
    },
    defaultImage: {
      type: String,
      default: '/assets/image/profile-default.png',
    },
  });

  const emit = defineEmits(['update:modelValue', 'update:file']);

  const fileInput = ref(null);
  const previewImage = ref(props.modelValue);
  const defaultImg = props.defaultImage;

  watch(
    () => props.modelValue,
    newVal => {
      previewImage.value = newVal;
    },
    { immediate: true }
  );

  const triggerFileInput = () => {
    fileInput.value?.click();
  };

  const handleImageUpload = e => {
    const file = e.target.files[0];
    if (file) {
      const reader = new FileReader();
      reader.onload = e => {
        previewImage.value = e.target.result;
        emit('update:modelValue', previewImage.value);
        emit('update:file', file);
      };
      reader.readAsDataURL(file);
    }
  };

  const removeImage = () => {
    previewImage.value = null;
    emit('update:modelValue', null);
    emit('update:file', null);
  };
</script>

<style scoped>
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
</style>
