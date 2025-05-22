<script setup>
  import { Layout } from '@/components/common/layout';
  import LoginView from '@/features/user/views/LoginView.vue';
  import { useAuthStore } from '@/store/auth.js';
  import { computed, onMounted } from 'vue';

  const authStore = useAuthStore();
  const isLoggedIn = computed(() => authStore.isAuthenticated);

  onMounted(() => {
    authStore.initAuth();
  });
</script>

<template>
  <Layout v-if="isLoggedIn" />
  <LoginView v-else />
</template>

<style scoped></style>
