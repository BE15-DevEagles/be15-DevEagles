import axios from 'axios';

const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL,
});

/* 1. 채팅방 생성 */
export function createChatRoom(payload) {
  return api.post('/chatrooms', payload);
}

/* 2. 기본 채팅방 생성 */
export function createDefaultChatRoom(teamId, name) {
  return api.post('/chatrooms/default', null, {
    params: {
      teamId,
      name,
    },
  });
}

/* 3. AI 채팅방 생성 또는 조회 */
export function createOrGetPersonalAiChatRoom(teamId, userId, name) {
  return api.post('/chatrooms/ai', null, {
    params: {
      teamId,
      userId,
      name,
    },
  });
}

/* 4. 채팅방 삭제 */
export function deleteChatRoom(chatroomId) {
  return api.delete(`/chatrooms/${chatroomId}`);
}

/* 5. 채팅방에 참가자 추가 */
export function addParticipantToChatRoom(chatroomId, userId) {
  return api.post(`/chatrooms/${chatroomId}/participants`, null, {
    params: {
      userId,
    },
  });
}

/* 6. 채팅방에서 참가자 제거 */
export function removeParticipantFromChatRoom(chatroomId, userId) {
  return api.delete(`/chatrooms/${chatroomId}/participants/${userId}`);
}

/* 7. 채팅방 참가자 알림 설정 토글 */
export function toggleParticipantNotification(chatroomId, userId) {
  return api.put(`/chatrooms/${chatroomId}/participants/${userId}/notification`);
}
