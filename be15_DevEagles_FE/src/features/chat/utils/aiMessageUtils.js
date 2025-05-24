/**
 * AI 시스템 상수
 */
export const AI_CONSTANTS = {
  AI_USER_ID: 'ai-assistant',
  AI_NAME: '수리 AI',
  MESSAGE_TYPES: {
    MOOD_INQUIRY: 'MOOD_INQUIRY',
    MOOD_FEEDBACK: 'MOOD_FEEDBACK',
    GENERAL_RESPONSE: 'GENERAL_RESPONSE',
  },
};

/**
 * AI 메시지인지 확인
 */
export function isAiMessage(message) {
  return message?.senderId === AI_CONSTANTS.AI_USER_ID;
}

/**
 * 기분 질문 메시지인지 확인
 */
export function isMoodInquiryMessage(message) {
  return isAiMessage(message) && message.metadata?.inquiryId && !message.metadata?.isResponse;
}

/**
 * 기분 피드백 메시지인지 확인
 */
export function isMoodFeedbackMessage(message) {
  return isAiMessage(message) && message.metadata?.isResponse === true;
}

/**
 * 메시지에서 inquiry ID 추출
 */
export function extractInquiryId(message) {
  return message?.metadata?.inquiryId || null;
}

/**
 * AI 메시지 타입 결정
 */
export function getAiMessageType(message) {
  if (!isAiMessage(message)) {
    return null;
  }

  if (isMoodInquiryMessage(message)) {
    return AI_CONSTANTS.MESSAGE_TYPES.MOOD_INQUIRY;
  }

  if (isMoodFeedbackMessage(message)) {
    return AI_CONSTANTS.MESSAGE_TYPES.MOOD_FEEDBACK;
  }

  return AI_CONSTANTS.MESSAGE_TYPES.GENERAL_RESPONSE;
}

/**
 * AI 응답 대기 중인지 확인 (마지막 메시지가 기분 질문인 경우)
 */
export function isWaitingForMoodResponse(messages) {
  if (!messages?.length) return false;

  const lastMessage = messages[messages.length - 1];
  return isMoodInquiryMessage(lastMessage);
}

/**
 * 사용자의 답변 대상 질문 찾기
 */
export function findPendingMoodInquiry(messages) {
  if (!messages?.length) return null;

  // 뒤에서부터 찾아서 가장 최근의 답변되지 않은 기분 질문 찾기
  for (let i = messages.length - 1; i >= 0; i--) {
    const message = messages[i];

    if (isMoodInquiryMessage(message)) {
      // 이 질문 이후에 사용자의 답변이 있는지 확인
      const hasUserResponse = messages.slice(i + 1).some(m => !isAiMessage(m));

      if (!hasUserResponse) {
        return message;
      }
    }
  }

  return null;
}

/**
 * AI 채팅방인지 확인
 */
export function isAiChatRoom(chat) {
  return chat?.type === 'AI' || chat?.name?.includes('AI') || chat?.isAiChat === true;
}
