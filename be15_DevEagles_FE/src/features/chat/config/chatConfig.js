import { initializeWebSocket } from '../api/webSocketService';
import { useAuthStore } from '@/store/auth.js';
import { initializeChat } from '../api/chatService';

export function setupChat() {
  try {
    const authStore = useAuthStore();
    initializeWebSocket(authStore);
  } catch (error) {
    console.error('채팅 설정 중 오류가 발생했습니다:', error);
    // 나중에 재시도하기 위해 지연 처리
    setTimeout(() => {
      try {
        const authStore = useAuthStore();
        if (authStore.isAuthenticated) {
          initializeChat();
        }
      } catch (retryError) {
        console.error('채팅 재시도 중 오류가 발생했습니다:', retryError);
      }
    }, 3000);
  }
}

export const CHAT_CONSTANTS = {
  MAX_MESSAGE_LENGTH: 1000,
  MAX_FILE_SIZE: 5 * 1024 * 1024, // 5MB
  // TODO : 상황 보기
  //   MESSAGE_TYPES: {
  //     TEXT: 'TEXT',
  //     IMAGE: 'IMAGE',
  //     FILE: 'FILE',
  // 	VIDEO: 'VIDEO',
  // 	AUDIO: 'AUDIO',
  //   },
};
