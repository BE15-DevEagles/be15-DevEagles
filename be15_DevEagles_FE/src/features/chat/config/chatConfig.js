import { useChatStore } from '@/store/chat';
import { useAuthStore } from '@/store/auth';

let isSetupComplete = false;

export function setupChat() {
  if (isSetupComplete) {
    console.log('채팅이 이미 설정되어 있습니다.');
    return;
  }

  console.log('채팅 시스템 설정 시작...');

  try {
    const authStore = useAuthStore();

    // 인증된 사용자만 채팅 초기화
    if (!authStore.isAuthenticated || !authStore.accessToken) {
      console.log('사용자가 인증되지 않아 채팅 초기화를 건너뜁니다.');
      return;
    }

    const chatStore = useChatStore();

    // Chat Store를 통해 초기화
    chatStore
      .initialize()
      .then(() => {
        isSetupComplete = true;
        console.log('채팅 시스템 설정 완료!');
      })
      .catch(error => {
        console.error('채팅 시스템 초기화 실패:', error);
      });
  } catch (error) {
    console.error('채팅 설정 중 오류가 발생했습니다:', error);
  }
}

export function teardownChat() {
  try {
    console.log('채팅 시스템 종료...');
    const chatStore = useChatStore();
    chatStore.cleanup();
    isSetupComplete = false;
    console.log('채팅 시스템 종료 완료');
  } catch (error) {
    console.error('채팅 종료 중 오류 발생:', error);
  }
}

export function getChatSetupStatus() {
  const chatStore = useChatStore();
  return {
    isSetupComplete,
    isWebSocketConnected: chatStore.isWebSocketConnected,
  };
}

// 페이지 언로드 시 정리
if (typeof window !== 'undefined') {
  window.addEventListener('beforeunload', teardownChat);
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
