import { ref, computed } from 'vue';
import {
  createOrGetAiChatRoom,
  generateMoodInquiry,
  saveMoodAnswer,
  getUserMoodHistory,
} from '@/features/chat/api/aiChatService';
import {
  isAiChatRoom,
  isAiMessage,
  isMoodInquiryMessage,
  findPendingMoodInquiry,
  isWaitingForMoodResponse,
  getAiMessageType,
  AI_CONSTANTS,
} from '@/features/chat/utils/aiMessageUtils';

/**
 * AI 채팅 기능 관리 컴포저블
 */
export function useAiChat() {
  const isAiThinking = ref(false);
  const isProcessingMood = ref(false);
  const error = ref(null);
  const moodHistory = ref([]);

  /**
   * AI 채팅방 생성 또는 가져오기
   */
  const initializeAiChat = async (userId, aiName = '수리AI') => {
    try {
      const chatRoom = await createOrGetAiChatRoom(userId, aiName);
      return chatRoom;
    } catch (err) {
      error.value = 'AI 채팅방 초기화 실패';
      console.error('[useAiChat] 초기화 실패:', err);
      throw err;
    }
  };

  /**
   * 기분 질문 생성 요청
   */
  const requestMoodInquiry = async userId => {
    try {
      isProcessingMood.value = true;
      error.value = null;

      const inquiry = await generateMoodInquiry(userId);

      if (inquiry) {
        console.log('[useAiChat] 기분 질문 생성됨:', inquiry);
        return inquiry;
      } else {
        console.log('[useAiChat] 오늘 이미 기분 질문 완료');
        return null;
      }
    } catch (err) {
      error.value = '기분 질문을 생성할 수 없습니다. 잠시 후 다시 시도해 주세요.';
      console.error('[useAiChat] 기분 질문 실패:', err);
      setAiThinking(false); // AI 상태도 해제
      throw err;
    } finally {
      isProcessingMood.value = false;
    }
  };

  /**
   * 기분 답변 처리
   */
  const processMoodAnswer = async (inquiryId, answer) => {
    try {
      isProcessingMood.value = true;
      error.value = null;

      const result = await saveMoodAnswer(inquiryId, answer);
      console.log('[useAiChat] 기분 답변 저장됨:', result);

      return result;
    } catch (err) {
      error.value = '답변을 저장할 수 없습니다. 네트워크를 확인해 주세요.';
      console.error('[useAiChat] 기분 답변 실패:', err);
      setAiThinking(false); // AI 상태도 해제
      throw err;
    } finally {
      isProcessingMood.value = false;
    }
  };

  /**
   * 사용자 기분 히스토리 로드
   */
  const loadMoodHistory = async userId => {
    try {
      const history = await getUserMoodHistory(userId);
      moodHistory.value = history;
      return history;
    } catch (err) {
      error.value = '기분 히스토리 로드 실패';
      console.error('[useAiChat] 히스토리 로드 실패:', err);
      throw err;
    }
  };

  /**
   * AI 응답 대기 상태 설정
   */
  const setAiThinking = thinking => {
    isAiThinking.value = thinking;
  };

  /**
   * 메시지가 기분 질문에 대한 답변인지 확인하고 처리
   */
  const handleUserMessageForMood = async (message, messages) => {
    if (!message || !messages) return false;

    // 답변 대상 질문 찾기
    const pendingInquiry = findPendingMoodInquiry(messages);

    if (pendingInquiry) {
      try {
        const inquiryId = pendingInquiry.metadata?.inquiryId;
        if (inquiryId) {
          console.log('[useAiChat] 기분 질문에 대한 답변 감지:', {
            inquiryId,
            answer: message.content,
          });

          await processMoodAnswer(inquiryId, message.content);
          return true;
        }
      } catch (err) {
        console.error('[useAiChat] 기분 답변 처리 오류:', err);
        error.value = 'AI 응답을 처리할 수 없습니다. 잠시 후 다시 시도해 주세요.';
        setAiThinking(false); // 실패시 AI 상태 해제
        return false;
      }
    }

    return false;
  };

  /**
   * AI 메시지 처리 (수신 시)
   */
  const handleAiMessage = message => {
    if (!isAiMessage(message)) return;

    const messageType = getAiMessageType(message);

    switch (messageType) {
      case AI_CONSTANTS.MESSAGE_TYPES.MOOD_INQUIRY:
        console.log('[useAiChat] 기분 질문 수신:', message.content);
        break;

      case AI_CONSTANTS.MESSAGE_TYPES.MOOD_FEEDBACK:
        console.log('[useAiChat] 기분 피드백 수신:', message.content);
        break;

      case AI_CONSTANTS.MESSAGE_TYPES.GENERAL_RESPONSE:
        console.log('[useAiChat] 일반 AI 응답 수신:', message.content);
        break;
    }

    // AI 응답이 오면 thinking 상태 해제
    setAiThinking(false);
  };

  /**
   * 에러 클리어
   */
  const clearError = () => {
    error.value = null;
  };

  // Computed properties
  const hasError = computed(() => !!error.value);
  const isProcessing = computed(() => isProcessingMood.value || isAiThinking.value);

  return {
    // State
    isAiThinking,
    isProcessingMood,
    error,
    moodHistory,

    // Computed
    hasError,
    isProcessing,

    // Actions
    initializeAiChat,
    requestMoodInquiry,
    processMoodAnswer,
    loadMoodHistory,
    setAiThinking,
    handleUserMessageForMood,
    handleAiMessage,
    clearError,

    // Utils (re-exported for convenience)
    isAiChatRoom,
    isAiMessage,
    isMoodInquiryMessage,
    isWaitingForMoodResponse,
    findPendingMoodInquiry,
  };
}
