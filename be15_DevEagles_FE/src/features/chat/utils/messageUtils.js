import { normalizeTimestamp, compareTimestamps } from './timeUtils';

/**
 * 메시지 관련 유틸리티 함수들
 */

export function generateTempMessageId() {
  return `temp-${Date.now()}-${Math.floor(Math.random() * 1000)}`;
}

export function isTempMessage(messageId) {
  return messageId?.toString().startsWith('temp-');
}

export function createTempMessage(content, senderId, senderName) {
  return {
    id: generateTempMessageId(),
    content,
    senderId,
    senderName,
    timestamp: new Date().toISOString(),
    isMe: true,
    messageType: 'TEXT',
  };
}

export function normalizeMessage(message, currentUserId) {
  // timestamp 우선, 없으면 createdAt 사용
  let messageTimestamp = message.timestamp || message.createdAt;

  // 둘 다 없으면 현재 한국 시간으로 설정
  if (!messageTimestamp) {
    const now = new Date();
    // 한국 시간대로 ISO 문자열 생성
    const koreaTime = new Date(now.toLocaleString('en-US', { timeZone: 'Asia/Seoul' }));
    messageTimestamp = koreaTime.toISOString();

    console.warn('[messageUtils] 메시지에 timestamp/createdAt이 없어 현재 시간 사용:', message.id);
  }

  return {
    ...message,
    timestamp: normalizeTimestamp(messageTimestamp),
    isMe: message.senderId === currentUserId,
  };
}

export function isDuplicateMessage(messages, newMessage) {
  if (!newMessage?.id) return false;
  return messages.some(msg => msg.id === newMessage.id);
}

export function findTempMessageIndex(messages, realMessage) {
  const realTimestamp = new Date(realMessage.timestamp || realMessage.createdAt).getTime();

  return messages.findIndex(msg => {
    if (!isTempMessage(msg.id)) return false;
    if (msg.senderId !== realMessage.senderId) return false;
    if (msg.content !== realMessage.content) return false;

    const tempTimestamp = new Date(msg.timestamp).getTime();
    const timeDiff = Math.abs(realTimestamp - tempTimestamp);

    // 시간 차이가 30초 이내인 경우 매칭으로 간주
    return timeDiff < 30000;
  });
}

export function sortMessagesByTime(messages) {
  return [...messages].sort((a, b) => compareTimestamps(a.timestamp, b.timestamp));
}

export function groupMessagesByDate(messages) {
  if (!messages || messages.length === 0) return [];

  const sortedMessages = sortMessagesByTime(messages);
  const groups = [];
  let currentGroup = null;
  let lastDate = null;

  sortedMessages.forEach((message, index) => {
    const messageDate = new Date(message.timestamp);
    const messageDateString = messageDate.toDateString();

    if (lastDate && lastDate !== messageDateString) {
      groups.push({ type: 'date', date: message.timestamp });
      currentGroup = null;
    }
    lastDate = messageDateString;

    const prevMessage = index > 0 ? sortedMessages[index - 1] : null;
    const shouldStartNewGroup =
      !currentGroup ||
      !prevMessage ||
      prevMessage.senderId !== message.senderId ||
      new Date(prevMessage.timestamp).toDateString() !== messageDateString ||
      messageDate.getTime() - new Date(prevMessage.timestamp).getTime() > 300000;

    if (shouldStartNewGroup) {
      currentGroup = { type: 'messages', messages: [message] };
      groups.push(currentGroup);
    } else {
      currentGroup.messages.push(message);
    }
  });

  return groups;
}
