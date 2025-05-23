/**
 * 채팅 관련 타입 정의들
 */

/**
 * @typedef {Object} ChatMessage
 * @property {string} id - 메시지 ID
 * @property {string} content - 메시지 내용
 * @property {string} senderId - 발신자 ID
 * @property {string} senderName - 발신자 이름
 * @property {string} timestamp - 전송 시간 (ISO 형식)
 * @property {string} chatroomId - 채팅방 ID
 * @property {boolean} isMe - 내가 보낸 메시지 여부
 * @property {'TEXT'|'IMAGE'|'FILE'} messageType - 메시지 타입
 */

/**
 * @typedef {Object} ChatRoom
 * @property {string} id - 채팅방 ID
 * @property {string} name - 채팅방 이름
 * @property {'DIRECT'|'GROUP'|'AI'} type - 채팅방 타입
 * @property {boolean} isOnline - 상대방 온라인 상태 (DIRECT만)
 * @property {string|null} thumbnail - 썸네일 이미지 URL
 * @property {string} lastMessage - 마지막 메시지
 * @property {string} lastMessageTime - 마지막 메시지 시간 (상대 시간)
 * @property {string} lastMessageTimestamp - 마지막 메시지 시간 (ISO 형식)
 * @property {number} unreadCount - 읽지 않은 메시지 수
 * @property {ChatParticipant[]} participants - 참가자 목록
 * @property {boolean} notificationEnabled - 알림 설정 여부
 * @property {string} createdAt - 생성 시간
 * @property {string} updatedAt - 수정 시간
 */

/**
 * @typedef {Object} ChatParticipant
 * @property {string} userId - 사용자 ID
 * @property {string} userName - 사용자 이름
 * @property {string|null} userThumbnail - 사용자 썸네일
 * @property {boolean} isOnline - 온라인 상태
 * @property {boolean} notificationEnabled - 알림 설정 여부
 */

/**
 * @typedef {Object} MessageGroup
 * @property {'messages'|'date'} type - 그룹 타입
 * @property {ChatMessage[]} [messages] - 메시지 목록 (type이 'messages'일 때)
 * @property {string} [date] - 날짜 (type이 'date'일 때)
 */

/**
 * @typedef {Object} WebSocketMessage
 * @property {string} id - 메시지 ID
 * @property {string} content - 메시지 내용
 * @property {string} senderId - 발신자 ID
 * @property {string} senderName - 발신자 이름
 * @property {string} chatroomId - 채팅방 ID
 * @property {string} timestamp - 전송 시간
 * @property {'TEXT'|'IMAGE'|'FILE'} messageType - 메시지 타입
 */

export {};
