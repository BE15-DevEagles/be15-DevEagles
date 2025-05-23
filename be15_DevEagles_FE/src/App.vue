<template>
  <RouterView />
</template>

<script setup>
  import { useAuthStore } from '@/store/auth';
  import { onMounted } from 'vue';

  const authStore = useAuthStore();
  onMounted(() => {
    authStore.initAuth();
  });
</script>

<style scoped></style>
