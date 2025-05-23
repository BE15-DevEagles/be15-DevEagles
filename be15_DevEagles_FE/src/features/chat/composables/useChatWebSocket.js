import { ref, onUnmounted } from 'vue';
import {
  subscribeToChatRoom,
  subscribeToReadStatus,
  sendReadMessage,
  unsubscribe,
  isWebSocketConnected,
} from '@/features/chat/api/webSocketService.js';

export function useChatWebSocket() {
  const currentSubscription = ref(null);
  const isConnected = ref(false);

  // WebSocket 연결 상태 업데이트
  const updateConnectionStatus = () => {
    isConnected.value = isWebSocketConnected();
  };

  // 채팅방 구독
  const subscribeToChat = (chatRoomId, messageHandler, readStatusHandler) => {
    if (!chatRoomId) {
      console.warn('[useChatWebSocket] 채팅방 ID가 없습니다.');
      return;
    }

    if (currentSubscription.value === chatRoomId) {
      console.log('[useChatWebSocket] 이미 같은 채팅방을 구독 중입니다:', chatRoomId);
      return;
    }

    // 기존 구독 완전히 해제
    unsubscribeFromChat();

    console.log('[useChatWebSocket] 채팅방 구독 시작:', chatRoomId);

    try {
      // 메시지 구독
      subscribeToChatRoom(chatRoomId, message => {
        console.log('[useChatWebSocket] 메시지 수신:', {
          id: message.id,
          chatroomId: message.chatroomId,
          content: message.content?.substring(0, 20),
        });

        // 채팅방 ID 검증 후 핸들러 호출
        if (messageHandler && message.chatroomId === chatRoomId) {
          messageHandler(message);
        } else {
          console.warn('[useChatWebSocket] 메시지 채팅방 ID 불일치:', {
            expected: chatRoomId,
            received: message.chatroomId,
          });
        }
      });

      // 읽음 상태 구독
      subscribeToReadStatus(chatRoomId, readStatus => {
        console.log('[useChatWebSocket] 읽음 상태 수신:', readStatus);
        if (readStatusHandler && readStatus.chatroomId === chatRoomId) {
          readStatusHandler(readStatus);
        }
      });

      currentSubscription.value = chatRoomId;
      updateConnectionStatus();

      console.log('[useChatWebSocket] 채팅방 구독 완료:', chatRoomId);
    } catch (error) {
      console.error('[useChatWebSocket] 구독 중 오류 발생:', error);
      currentSubscription.value = null;
    }
  };

  // 구독 해제
  const unsubscribeFromChat = () => {
    if (currentSubscription.value) {
      console.log('[useChatWebSocket] 구독 해제 시작:', currentSubscription.value);

      try {
        unsubscribe(`/topic/chatroom.${currentSubscription.value}`);
        unsubscribe(`/topic/chatroom.${currentSubscription.value}.read`);
        console.log('[useChatWebSocket] 구독 해제 완료');
      } catch (error) {
        console.error('[useChatWebSocket] 구독 해제 중 오류:', error);
      }

      currentSubscription.value = null;
    }
    updateConnectionStatus();
  };

  // 읽음 상태 전송
  const markMessageAsRead = (chatRoomId, messageId) => {
    if (!chatRoomId || !messageId) {
      console.warn('[useChatWebSocket] 읽음 처리 실패 - 필수 정보 부족:', {
        chatRoomId,
        messageId,
      });
      return false;
    }

    try {
      const success = sendReadMessage(chatRoomId, messageId);
      if (success) {
        console.log('[useChatWebSocket] 읽음 상태 전송 성공:', { chatRoomId, messageId });
      }
      return success;
    } catch (error) {
      console.error('[useChatWebSocket] 읽음 상태 전송 실패:', error);
      return false;
    }
  };

  // 자동 읽음 처리 (채팅방 입장 시)
  const markLatestMessageAsRead = (chatRoomId, messages) => {
    if (!chatRoomId || !messages || messages.length === 0) return;

    const latestMessage = messages[messages.length - 1];
    if (latestMessage && !latestMessage.isMe) {
      markMessageAsRead(chatRoomId, latestMessage.id);
    }
  };

  // 컴포넌트 정리
  onUnmounted(() => {
    unsubscribeFromChat();
  });

  return {
    currentSubscription,
    isConnected,
    subscribeToChat,
    unsubscribeFromChat,
    markMessageAsRead,
    markLatestMessageAsRead,
    updateConnectionStatus,
  };
}
