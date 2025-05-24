import { ref, nextTick } from 'vue';
import { useAuthStore } from '@/store/auth';

export function useChatWindow() {
  const authStore = useAuthStore();

  const initializeChat = async (
    chatId,
    { loadChatHistory, setMessages, subscribeToChat, markChatAsRead, clearError, onReady }
  ) => {
    if (!chatId) {
      console.warn('[useChatWindow] 채팅방 ID가 없습니다.');
      return;
    }

    console.log('[useChatWindow] 채팅 초기화 시작:', chatId);

    try {
      clearError();

      // 초기에는 최신 메시지 15개만 로드
      const historyMessages = await loadChatHistory(chatId, null, 15);

      if (historyMessages && historyMessages.length > 0) {
        console.log('[useChatWindow] 초기 메시지 로드 완료:', historyMessages.length);
        setMessages(historyMessages, authStore.userId);
      } else {
        console.log('[useChatWindow] 초기 메시지 없음');
      }

      // WebSocket 구독
      subscribeToChat(chatId);

      // 채팅방 읽음 처리
      await markChatAsRead(chatId);

      // 초기화 완료 콜백
      if (onReady) {
        onReady();
      }

      console.log('[useChatWindow] 채팅 초기화 완료');
    } catch (err) {
      console.error('[useChatWindow] 채팅 초기화 실패:', err);
      throw err;
    }
  };

  const handleIncomingMessage = (
    message,
    { currentChatId, addMessage, updateChatInfo, markMessageAsRead, scrollToBottom }
  ) => {
    console.log('[useChatWindow] 새 메시지 수신:', {
      id: message.id,
      chatroomId: message.chatroomId,
      content: message.content?.substring(0, 20),
    });

    // 현재 채팅방의 메시지인지 확인
    if (message.chatroomId !== currentChatId) {
      console.log('[useChatWindow] 다른 채팅방 메시지 무시');
      return;
    }

    // 타임스탬프 검증
    if (!message.timestamp && !message.createdAt) {
      message.timestamp = new Date().toISOString();
      console.warn('[useChatWindow] 메시지에 타임스탬프 없어 현재 시간 설정:', message.id);
    }

    // 메시지 추가
    const messageWithUserId = {
      ...message,
      currentUserId: authStore.userId,
    };

    const success = addMessage(messageWithUserId);

    if (success) {
      console.log('[useChatWindow] 메시지 추가 성공');

      // 자동 스크롤
      scrollToBottom();

      // 다른 사용자의 메시지라면 읽음 처리
      if (message.senderId !== authStore.userId && message.id) {
        markMessageAsRead(currentChatId, message.id);
      }

      // 채팅방 정보 업데이트
      updateChatInfo(currentChatId, message);
    }
  };

  const loadMoreMessages = async (
    chatId,
    { getOldestMessage, loadChatHistory, setMessages, messages, maintainScrollPosition }
  ) => {
    if (!chatId) return { hasMore: false };

    const oldestMessage = getOldestMessage();
    const beforeId = oldestMessage?.id;

    console.log('[useChatWindow] 이전 메시지 로드 시작:', {
      beforeId,
      currentCount: messages.value.length,
    });

    return await maintainScrollPosition(async () => {
      const olderMessages = await loadChatHistory(chatId, beforeId, 15);

      if (olderMessages && olderMessages.length > 0) {
        // 기존 메시지 앞에 추가
        setMessages([...olderMessages, ...messages.value], authStore.userId);
        console.log('[useChatWindow] 이전 메시지 추가 완료:', olderMessages.length);
        return { hasMore: olderMessages.length >= 15 };
      }

      return { hasMore: false };
    });
  };

  return {
    initializeChat,
    handleIncomingMessage,
    loadMoreMessages,
  };
}
