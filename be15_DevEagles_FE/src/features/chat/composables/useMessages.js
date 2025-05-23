import { ref, computed } from 'vue';
import { formatMessageTime, formatDate } from '../utils/timeUtils';
import {
  createTempMessage,
  normalizeMessage,
  isDuplicateMessage,
  findTempMessageIndex,
  groupMessagesByDate,
  isTempMessage,
} from '../utils/messageUtils';

export function useMessages() {
  const messages = ref([]);

  const addMessage = message => {
    if (!message) return false;

    if (isDuplicateMessage(messages.value, message)) {
      console.log('[useMessages] 중복 메시지 무시:', message.id);
      return false;
    }

    if (!isTempMessage(message.id)) {
      const tempIndex = findTempMessageIndex(messages.value, message);
      if (tempIndex !== -1) {
        console.log('[useMessages] 임시 메시지를 실제 메시지로 교체:', {
          tempId: messages.value[tempIndex].id,
          realId: message.id,
        });

        const normalizedMessage = normalizeMessage(message, message.currentUserId);
        messages.value[tempIndex] = normalizedMessage;
        return true;
      }
    }

    const formattedMessage = normalizeMessage(message, message.currentUserId);
    messages.value.push(formattedMessage);

    console.log('[useMessages] 메시지 추가:', message.id);
    return true;
  };

  const addTemporaryMessage = (content, senderId, senderName) => {
    const tempMessage = createTempMessage(content, senderId, senderName);
    messages.value.push(tempMessage);

    console.log('[useMessages] 임시 메시지 추가:', tempMessage.id);
    return tempMessage;
  };

  const groupedMessages = computed(() => groupMessagesByDate(messages.value));

  const clearMessages = () => {
    console.log('[useMessages] 메시지 초기화');
    messages.value = [];
  };

  const setMessages = (newMessages, currentUserId) => {
    console.log('[useMessages] 메시지 목록 설정, 개수:', newMessages?.length || 0);

    if (!newMessages || !Array.isArray(newMessages)) {
      console.warn('[useMessages] 유효하지 않은 메시지 배열:', newMessages);
      return;
    }

    // 기존 메시지 클리어하지 않고 새 메시지들만 정규화해서 설정
    const normalizedMessages = newMessages.map(msg => {
      const formattedMessage = {
        ...normalizeMessage(msg, currentUserId),
        currentUserId,
      };
      return formattedMessage;
    });

    // 중복 제거하고 시간순 정렬
    const uniqueMessages = [];
    const seenIds = new Set();

    normalizedMessages.forEach(msg => {
      if (!seenIds.has(msg.id)) {
        seenIds.add(msg.id);
        uniqueMessages.push(msg);
      }
    });

    // 시간순 정렬
    uniqueMessages.sort((a, b) => {
      const timeA = new Date(a.timestamp).getTime();
      const timeB = new Date(b.timestamp).getTime();
      return timeA - timeB;
    });

    messages.value = uniqueMessages;

    console.log('[useMessages] 최종 메시지 수:', messages.value.length);
  };

  const getMessageCount = () => messages.value.length;

  const getLastMessage = () => {
    if (messages.value.length === 0) return null;
    const sorted = [...messages.value].sort(
      (a, b) => new Date(b.timestamp).getTime() - new Date(a.timestamp).getTime()
    );
    return sorted[0];
  };

  const getOldestMessage = () => {
    if (messages.value.length === 0) return null;
    const sorted = [...messages.value].sort(
      (a, b) => new Date(a.timestamp).getTime() - new Date(b.timestamp).getTime()
    );
    return sorted[0];
  };

  const removeTempMessage = tempId => {
    const index = messages.value.findIndex(msg => msg.id === tempId);
    if (index !== -1) {
      messages.value.splice(index, 1);
      console.log('[useMessages] 임시 메시지 제거:', tempId);
      return true;
    }
    return false;
  };

  const updateMessage = (messageId, updates) => {
    const index = messages.value.findIndex(msg => msg.id === messageId);
    if (index !== -1) {
      messages.value[index] = { ...messages.value[index], ...updates };
      console.log('[useMessages] 메시지 업데이트:', messageId);
      return true;
    }
    return false;
  };

  return {
    messages,
    groupedMessages,
    addMessage,
    addTemporaryMessage,
    clearMessages,
    setMessages,
    formatMessageTime,
    formatDate,
    getMessageCount,
    getLastMessage,
    getOldestMessage,
    removeTempMessage,
    updateMessage,
  };
}
