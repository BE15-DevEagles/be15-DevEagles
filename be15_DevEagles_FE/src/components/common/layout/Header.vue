<template>
  <header class="bg-[var(--color-gray-800)] h-14 w-full flex items-center px-3 shadow-drop z-10">
    <div class="flex items-center justify-between w-full">
      <div class="w-30 h-10">
        <img
          src="/assets/image/logo-goody-with-text.png"
          alt="Goody Logo"
          class="w-full h-full object-contain"
        />
      </div>

      <div class="flex items-center space-x-4">
        <!-- 검색 -->
        <div class="relative">
          <input
            type="text"
            placeholder="검색 넣으면 여기다"
            class="bg-[var(--color-primary-400)] text-white placeholder-[var(--color-gray-300)] rounded-md px-3 py-1.5 pl-9 font-small outline-none focus:ring-1 focus:ring-white w-48"
          />
          <svg
            xmlns="http://www.w3.org/2000/svg"
            class="h-4 w-4 text-[var(--color-gray-300)] absolute left-3 top-1/2 transform -translate-y-1/2"
            fill="none"
            viewBox="0 0 24 24"
            stroke="currentColor"
          >
            <path
              stroke-linecap="round"
              stroke-linejoin="round"
              stroke-width="2"
              d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"
            />
          </svg>
        </div>

        <!-- 프로필 -->
        <div class="relative group">
          <button
            class="flex items-center space-x-2 text-white hover:opacity-80 transition-opacity"
          >
            <div
              class="rounded-full bg-white h-8 w-8 overflow-hidden flex items-center justify-center border-2 border-white"
            >
              <img
                v-if="user.userThumbnail"
                :src="user.userThumbnail"
                :alt="user.name"
                class="w-full h-full object-cover"
              />
              <div
                v-else
                class="w-full h-full flex items-center justify-center bg-[var(--color-primary-300)] text-white font-one-liner-semibold"
              >
                {{ user.name ? user.name.charAt(0) : '?' }}
              </div>
            </div>
            <span class="font-one-liner-semibold">{{ user.name }}</span>
          </button>

          <!-- 드롭다운 메뉴 -->
          <div
            class="absolute right-0 top-full mt-2 bg-white rounded-md shadow-drop overflow-hidden invisible opacity-0 group-hover:visible group-hover:opacity-100 transition-all duration-200 w-48 z-50"
          >
            <div class="py-2">
              <a
                href="#"
                class="block px-4 py-2 text-[var(--color-gray-700)] hover:bg-[var(--color-gray-100)] font-one-liner"
                @click.prevent="handleMyPage"
                >나의 정보</a
              >

              <div class="border-t border-[var(--color-gray-200)] my-1"></div>
              <a
                href="#"
                class="block px-4 py-2 text-[var(--color-error-300)] hover:bg-[var(--color-gray-100)] font-one-liner"
                @click.prevent="handleLogout"
                >로그아웃</a
              >
            </div>
          </div>
        </div>
      </div>
    </div>
  </header>
</template>

<script setup>
  import { ref } from 'vue';
  import { logout } from '@/features/user/api/user.js';
  import { useRouter } from 'vue-router';
  import { useAuthStore } from '@/store/auth.js';

  const router = useRouter();
  const authStore = useAuthStore();

  const user = ref({
    name: authStore.name, // 예시 이름
    userThumbnail: authStore.userThumbnailUrl, // 예시 (null이면 첫 글자 표시)
  });

  const handleLogout = async () => {
    try {
      await logout();
      authStore.clearAuth();
      router.push('/login');
    } catch (error) {
      console.error('로그아웃 실패:', error);
    }
  };

  const handleMyPage = () => {
    router.push('/mypage');
  };
</script>
