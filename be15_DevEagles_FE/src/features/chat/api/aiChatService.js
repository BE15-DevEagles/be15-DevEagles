import api from '@/api/axios';

/**
 * AI 채팅방 생성 또는 가져오기
 */
export async function createOrGetAiChatRoom(userId, aiName = '수리AI') {
  try {
    const response = await api.post('/ai-chat', null, {
      params: { userId, aiName },
    });
    return response.data.data;
  } catch (error) {
    console.error('AI 채팅방 생성 실패:', error);
    throw error;
  }
}

/**
 * 사용자의 AI 채팅방 목록 조회
 */
export async function getUserAiChatRooms(userId) {
  try {
    const response = await api.get('/ai-chat/me', {
      params: { userId },
    });
    return response.data.data;
  } catch (error) {
    console.error('AI 채팅방 목록 조회 실패:', error);
    throw error;
  }
}

/**
 * AI 채팅방 메시지 조회
 */
export async function getAiChatMessages(chatroomId, page = 0, size = 20) {
  try {
    const response = await api.get(`/ai-chat/${chatroomId}/messages`, {
      params: { page, size },
    });
    return response.data.data;
  } catch (error) {
    console.error('AI 채팅 메시지 조회 실패:', error);
    throw error;
  }
}

/**
 * 기분 질문 생성 요청
 */
export async function generateMoodInquiry(userId) {
  try {
    const response = await api.post('/ai-chat/mood-inquiry', null, {
      params: { userId },
    });
    return response.data.data;
  } catch (error) {
    if (error.response?.status === 400) {
      // 이미 오늘 기분 질문이 전송된 경우
      return null;
    }
    console.error('기분 질문 생성 실패:', error);
    throw error;
  }
}

/**
 * 기분 답변 저장
 */
export async function saveMoodAnswer(inquiryId, answer) {
  try {
    const response = await api.post('/ai-chat/mood-answer', {
      inquiryId,
      answer,
    });
    return response.data.data;
  } catch (error) {
    console.error('기분 답변 저장 실패:', error);
    throw error;
  }
}

/**
 * 사용자 기분 히스토리 조회
 */
export async function getUserMoodHistory(userId) {
  try {
    const response = await api.get('/ai-chat/mood-history', {
      params: { userId },
    });
    return response.data.data;
  } catch (error) {
    console.error('기분 히스토리 조회 실패:', error);
    throw error;
  }
}
