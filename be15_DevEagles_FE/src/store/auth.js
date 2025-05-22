import { defineStore } from 'pinia';
import { computed, ref } from 'vue';

function decodeJwtPayload(token) {
  const payload = token.split('.')[1];
  const decoded = atob(payload);
  const utf8Payload = new TextDecoder().decode(Uint8Array.from(decoded, c => c.charCodeAt(0)));
  return JSON.parse(utf8Payload);
}

export const useAuthStore = defineStore('auth', () => {
  const accessToken = ref(null);
  const userId = ref(null);
  const expirationTime = ref(null);

  // ✅ 추가 상태
  const name = ref(null);
  const userThumbnailUrl = ref(null);

  const isAuthenticated = computed(
    () => !!accessToken.value && Date.now() < (expirationTime.value || 0)
  );

  function setAuth(at) {
    accessToken.value = at;
    try {
      const payload = decodeJwtPayload(at);
      console.log('payload', payload);
      userId.value = payload.userId;
      name.value = payload.name || null;
      userThumbnailUrl.value = payload.userThumbnailUrl || null;
      expirationTime.value = payload.exp * 1000;

      localStorage.setItem('accessToken', at);
    } catch (e) {
      clearAuth();
    }
  }

  function clearAuth() {
    accessToken.value = null;
    userId.value = null;
    name.value = null;
    userThumbnailUrl.value = null;
    expirationTime.value = null;

    localStorage.removeItem('accessToken');
    localStorage.removeItem('refreshToken');
  }

  function initAuth() {
    const token = localStorage.getItem('accessToken');
    if (token) {
      setAuth(token);
    }
  }

  return {
    accessToken,
    userId,
    name,
    userThumbnailUrl,
    expirationTime,
    isAuthenticated,
    setAuth,
    clearAuth,
    initAuth,
  };
});
