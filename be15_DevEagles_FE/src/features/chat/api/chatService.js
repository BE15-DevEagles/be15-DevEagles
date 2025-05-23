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

export async function getChatHistory(chatRoomId, page = 0, size = 20) {
  try {
    const response = await api.get(`/chatrooms/${chatRoomId}/messages`, {
      params: { page, size },
    });
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
