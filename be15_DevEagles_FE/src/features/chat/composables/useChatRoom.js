import { ref } from 'vue';
import { getChatHistory, markAsRead } from '@/features/chat/api/chatService.js';
import { sendWebSocketMessage } from '@/features/chat/api/webSocketService.js';
import { useAuthStore } from '@/store/auth.js';
import { useChatStore } from '@/store/chat.js';
import { createTempMessage } from '@/features/chat/utils/messageUtils.js';
import { normalizeTimestamp } from '@/features/chat/utils/timeUtils.js';

export function useChatRoom() {
  const isLoading = ref(false);
  const isSending = ref(false);
  const error = ref(null);

  const authStore = useAuthStore();
  const chatStore = useChatStore();

  // 채팅 히스토리 로드 (무한 스크롤 지원)
  const loadChatHistory = async (chatRoomId, before = null, limit = 30) => {
    if (!chatRoomId) {
      console.warn('[useChatRoom] 채팅방 ID가 없습니다.');
      return [];
    }

    try {
      isLoading.value = true;
      error.value = null;

      console.log('[useChatRoom] 채팅 히스토리 로드 시작:', {
        chatRoomId,
        before,
        limit,
      });

      const response = await getChatHistory(chatRoomId, before, limit);

      let messages = [];

      // 응답 구조에 따라 처리
      if (response && Array.isArray(response.messages)) {
        messages = response.messages;
      } else if (response && Array.isArray(response)) {
        messages = response;
      } else if (response && response.data) {
        if (Array.isArray(response.data.messages)) {
          messages = response.data.messages;
        } else if (Array.isArray(response.data)) {
          messages = response.data;
        }
      }

      // 메시지 타임스탬프 검증 및 정규화 (timeUtils 사용)
      messages = messages.map(msg => {
        if (!msg.timestamp && !msg.createdAt) {
          console.warn('[useChatRoom] 메시지에 타임스탬프 없음:', msg.id);
        }
        // timestamp 우선, 없으면 createdAt 사용, 둘 다 없으면 현재 시간
        msg.timestamp = normalizeTimestamp(msg.timestamp || msg.createdAt);
        return msg;
      });

      // 백엔드에서 DESC 순으로 받아온 메시지를 ASC 순으로 뒤집기 (오래된 메시지가 먼저)
      messages.reverse();

      console.log('[useChatRoom] 로드된 메시지 수:', messages.length);
      return messages;
    } catch (err) {
      console.error('[useChatRoom] 채팅 히스토리 로드 실패:', err);
      error.value = '채팅 이력을 불러오는데 실패했습니다.';
      return [];
    } finally {
      isLoading.value = false;
    }
  };

  // 메시지 전송
  const sendMessage = async (chatRoomId, content) => {
    if (!chatRoomId || !content?.trim()) {
      console.warn('[useChatRoom] 메시지 전송 실패 - 필수 정보 부족');
      return null;
    }

    if (isSending.value) {
      console.warn('[useChatRoom] 이미 메시지 전송 중입니다.');
      return null;
    }

    try {
      isSending.value = true;
      error.value = null;

      console.log('[useChatRoom] 메시지 전송 시작:', { chatRoomId, content });

      // WebSocket으로 메시지 전송
      const success = sendWebSocketMessage(
        chatRoomId,
        content.trim(),
        authStore.userId,
        authStore.userName || authStore.nickname
      );

      if (success) {
        console.log('[useChatRoom] 메시지 전송 성공');

        const tempMessage = {
          ...createTempMessage(
            content.trim(),
            authStore.userId,
            authStore.userName || authStore.nickname
          ),
          chatroomId: chatRoomId,
        };

        return tempMessage;
      } else {
        throw new Error('WebSocket 메시지 전송 실패');
      }
    } catch (err) {
      console.error('[useChatRoom] 메시지 전송 실패:', err);
      error.value = '메시지 전송에 실패했습니다.';
      return null;
    } finally {
      isSending.value = false;
    }
  };

  // 채팅방 읽음 처리 (REST API)
  const markChatAsRead = async chatRoomId => {
    if (!chatRoomId) {
      console.warn('[useChatRoom] 읽음 처리 실패 - 채팅방 ID 없음');
      return false;
    }

    try {
      console.log('[useChatRoom] 채팅방 읽음 처리:', chatRoomId);
      await markAsRead(chatRoomId);

      // 스토어의 읽지 않은 메시지 수 업데이트
      if (chatStore.markChatAsRead) {
        chatStore.markChatAsRead(chatRoomId);
      }

      console.log('[useChatRoom] 읽음 처리 완료');
      return true;
    } catch (err) {
      console.error('[useChatRoom] 읽음 처리 실패:', err);
      return false;
    }
  };

  // 채팅방 정보 업데이트 (마지막 메시지 등)
  const updateChatInfo = (chatRoomId, lastMessage) => {
    if (!chatRoomId || !lastMessage) return;

    try {
      // 스토어에 마지막 메시지 정보 업데이트
      if (chatStore.updateChatAfterSending) {
        chatStore.updateChatAfterSending(chatRoomId, lastMessage.content);
      }
    } catch (err) {
      console.error('[useChatRoom] 채팅방 정보 업데이트 실패:', err);
    }
  };

  // 에러 초기화
  const clearError = () => {
    error.value = null;
  };

  return {
    isLoading,
    isSending,
    error,
    loadChatHistory,
    sendMessage,
    markChatAsRead,
    updateChatInfo,
    clearError,
  };
}
