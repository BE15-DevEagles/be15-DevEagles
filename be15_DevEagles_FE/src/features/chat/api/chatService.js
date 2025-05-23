import api from '@/api/axios';
import { sendWebSocketMessage, initializeWebSocket } from './webSocketService';
import { useAuthStore } from '@/store/auth.js';

export async function getChatRooms() {
  try {
    const response = await api.get('/chatrooms');
    return response.data.data.chatrooms;
  } catch (error) {
    console.error('채팅방 목록 조회 실패:', error);
    throw error;
  }
}

export async function getChatHistory(chatRoomId, before = null, limit = 20) {
  try {
    console.log('[chatService] 채팅 히스토리 요청:', {
      chatRoomId,
      before,
      limit,
    });

    const params = {};
    if (before) {
      params.before = before;
    }
    params.limit = limit;

    const response = await api.get(`/chatrooms/${chatRoomId}/messages`, {
      params,
    });

    // 메시지 데이터 검증
    if (response.data.data && response.data.data.messages) {
      const messages = response.data.data.messages;
      console.log(`[chatService] 히스토리 로드: ${messages.length}개 메시지`);

      // 타임스탬프가 없는 메시지 체크
      const noTimestampCount = messages.filter(msg => !msg.timestamp && !msg.createdAt).length;
      if (noTimestampCount > 0) {
        console.warn(`[chatService] 타임스탬프 없는 메시지: ${noTimestampCount}개`);
      }

      // 메시지 정규화 - timestamp가 없으면 createdAt 사용
      const normalizedMessages = messages.map(msg => {
        if (!msg.timestamp && msg.createdAt) {
          msg.timestamp = msg.createdAt;
        }
        return msg;
      });

      return {
        ...response.data.data,
        messages: normalizedMessages,
      };
    }

    return response.data.data;
  } catch (error) {
    console.error(`채팅 이력 조회 실패:`, error);
    throw error;
  }
}

export function sendMessage(chatRoomId, message) {
  let authStore;
  try {
    authStore = useAuthStore();
  } catch (error) {
    console.error('Auth 스토어 접근 실패:', error);
  }

  return sendWebSocketMessage(chatRoomId, message, authStore);
}

export async function markAsRead(chatRoomId) {
  try {
    await api.put(`/chatrooms/${chatRoomId}/read`);
    return true;
  } catch (error) {
    console.error('읽음 표시 실패:', error);
    return false;
  }
}

export async function getMessageReadStatus(chatroomId, messageId) {
  try {
    const response = await api.get(`/chatrooms/${chatroomId}/messages/${messageId}/read-status`);
    return response.data.data;
  } catch (error) {
    console.error('메시지 읽음 상태 조회 실패:', error);
    throw error;
  }
}

// 채팅방 알림 설정 조회
export async function getChatNotificationSetting(chatRoomId) {
  try {
    const response = await api.get(`/chatrooms/${chatRoomId}/notification`);
    return response.data.data;
  } catch (error) {
    console.error('알림 설정 조회 실패:', error);
    throw error;
  }
}

// 채팅방 알림 설정 토글
export async function toggleChatNotification(chatRoomId) {
  try {
    const response = await api.put(`/chatrooms/${chatRoomId}/notification/toggle`);
    return response.data.data;
  } catch (error) {
    console.error('알림 설정 변경 실패:', error);
    throw error;
  }
}

// 모든 채팅방의 알림 설정 조회
export async function getAllNotificationSettings() {
  try {
    const response = await api.get('/chatrooms/notifications');
    return response.data.data;
  } catch (error) {
    console.error('전체 알림 설정 조회 실패:', error);
    throw error;
  }
}

export function initializeChat() {
  let authStore;
  try {
    authStore = useAuthStore();
    initializeWebSocket(authStore);
  } catch (error) {
    console.error('채팅 초기화 실패:', error);
  }
}
