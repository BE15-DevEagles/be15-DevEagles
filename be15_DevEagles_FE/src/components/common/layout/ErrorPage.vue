<template>
  <div class="flex flex-col items-center justify-center h-screen bg-[var(--color-gray-50)] p-4">
    <img src="/assets/image/logo-goody.png" alt="Goody Logo" class="mb-6 w-24 h-24" />

    <div class="bg-white rounded-lg shadow-drop p-8 max-w-md w-full text-center">
      <h1 class="font-screen-title text-[var(--color-error-300)] mb-4">{{ errorCode || '404' }}</h1>
      <h2 class="font-section-title mb-4">{{ errorTitle || '페이지를 찾을 수 없습니다' }}</h2>
      <p class="font-body text-[var(--color-gray-600)] mb-6">
        {{
          errorMessage ||
          '요청하신 페이지를 찾을 수 없습니다. 페이지가 삭제되었거나 주소가 변경되었을 수 있습니다.'
        }}
      </p>

      <div class="flex flex-col sm:flex-row gap-4 justify-center">
        <button
          class="bg-[var(--color-gray-200)] text-[var(--color-gray-700)] px-6 py-2 rounded-md font-one-liner-semibold hover:bg-[var(--color-gray-300)]"
          @click="goBack"
        >
          이전 페이지로
        </button>
        <button
          class="bg-[var(--color-primary-main)] text-white px-6 py-2 rounded-md font-one-liner-semibold hover:bg-[var(--color-primary-400)]"
          @click="goHome"
        >
          홈으로 이동
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
  import { useRouter } from 'vue-router';

  const router = useRouter();

  // Props
  const props = defineProps({
    errorCode: {
      type: String,
      default: '404',
    },
    errorTitle: {
      type: String,
      default: '페이지를 찾을 수 없습니다',
    },
    errorMessage: {
      type: String,
      default:
        '요청하신 페이지를 찾을 수 없습니다. 페이지가 삭제되었거나 주소가 변경되었을 수 있습니다.',
    },
  });

  // Methods
  const goBack = () => {
    router.back();
  };

  const goHome = () => {
    router.push('/');
  };
</script>
