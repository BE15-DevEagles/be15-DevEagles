import { ref, computed, onMounted } from 'vue';
import { useAuthStore } from '@/store/auth.js';
import { getChatRooms, markAsRead } from '../api/chatService';

export function useChatData() {
  const authStore = useAuthStore();
  // 채팅 데이터
  const chats = ref([]);
  const isLoading = ref(false);

  // 읽지 않은 메시지 수 계산
  const unreadCount = computed(() => {
    return chats.value.reduce((total, chat) => total + (chat.unreadCount || 0), 0);
  });

  // 백엔드에서 채팅방 목록 가져오기
  async function loadChatRooms() {
    try {
      isLoading.value = true;
      const chatRooms = await getChatRooms();

      if (chatRooms && Array.isArray(chatRooms)) {
        chats.value = chatRooms.map(room => ({
          id: room.id,
          name: room.name || getParticipantName(room.participants),
          isOnline: isParticipantOnline(room.participants),
          userThumbnail: getParticipantThumbnail(room.participants),
          lastMessage: room.lastMessage?.content || '메시지가 없습니다.',
          lastMessageTime: formatLastMessageTime(room.lastMessage?.timestamp),
          unreadCount: room.unreadCount || 0,
          messages: [],
        }));
      }
    } catch (error) {
      console.error('채팅방 목록 로딩 실패:', error);
    } finally {
      isLoading.value = false;
    }
  }

  function getParticipantName(participants) {
    if (!participants || !Array.isArray(participants)) return '알 수 없는 대화';

    const otherParticipant = participants.find(p => p.id !== authStore.userId);
    return otherParticipant ? otherParticipant.name : '대화방';
  }

  function isParticipantOnline(participants) {
    if (!participants || !Array.isArray(participants)) return false;

    const otherParticipant = participants.find(p => p.id !== authStore.userId);
    return otherParticipant ? otherParticipant.isOnline : false;
  }

  function getParticipantThumbnail(participants) {
    if (!participants || !Array.isArray(participants)) return null;

    const otherParticipant = participants.find(p => p.id !== authStore.userId);
    return otherParticipant ? otherParticipant.thumbnail : null;
  }

  function formatLastMessageTime(timestamp) {
    if (!timestamp) return '';

    const now = new Date();
    const msgTime = new Date(timestamp);
    const diffMs = now - msgTime;
    const diffMins = Math.floor(diffMs / 60000);
    const diffHours = Math.floor(diffMins / 60);
    const diffDays = Math.floor(diffHours / 24);

    if (diffMins < 1) return '방금';
    if (diffMins < 60) return `${diffMins}분 전`;
    if (diffHours < 24) return `${diffHours}시간 전`;
    if (diffDays < 7) return `${diffDays}일 전`;

    return `${msgTime.getMonth() + 1}월 ${msgTime.getDate()}일`;
  }

  // 팀원과 채팅 시작
  function startChat(member) {
    // 해당 팀원과의 채팅이 이미 있는지 확인
    let chat = chats.value.find(c => c.name === member.name);

    // 채팅이 없으면 새로 생성
    if (!chat) {
      chat = {
        id: member.id,
        name: member.name,
        isOnline: member.isOnline,
        userThumbnail: member.userThumbnail,
        lastMessage: '',
        lastMessageTime: '방금',
        unreadCount: 0,
        messages: [],
      };
      chats.value.unshift(chat);
    }

    return chat;
  }

  // 메시지 전송 (로컬 UI 업데이트용)
  function sendMessage(chatId, message) {
    const chatIndex = chats.value.findIndex(c => c.id === chatId);
    if (chatIndex === -1) return null;

    chats.value[chatIndex].lastMessage = message.text;
    chats.value[chatIndex].lastMessageTime = '방금';

    // 해당 채팅을 목록의 맨 위로 이동
    const chat = chats.value.splice(chatIndex, 1)[0];
    chats.value.unshift(chat);

    return chat;
  }

  // 메시지 수신 (웹소켓으로부터 수신한 메시지 처리)
  function receiveMessage(chatId, message) {
    const chatIndex = chats.value.findIndex(c => c.id === chatId);
    if (chatIndex === -1) return null;

    // 읽지 않은 메시지 카운트 증가 (내가 보낸 메시지가 아닌 경우만)
    if (!message.isMe) {
      chats.value[chatIndex].unreadCount += 1;
    }

    chats.value[chatIndex].lastMessage = message.text;
    chats.value[chatIndex].lastMessageTime = '방금';

    // 해당 채팅을 목록의 맨 위로 이동
    if (chatIndex > 0) {
      const chat = chats.value.splice(chatIndex, 1)[0];
      chats.value.unshift(chat);
    }

    return chats.value[chatIndex];
  }

  // 채팅 메시지 업데이트 (채팅 이력 로드 시)
  function updateChatMessages(chatId, messages) {
    const chatIndex = chats.value.findIndex(c => c.id === chatId);
    if (chatIndex === -1) return null;

    chats.value[chatIndex].messages = messages;
    return chats.value[chatIndex];
  }

  // 채팅 열기 시 읽음 표시
  function markChatAsRead(chatId) {
    const chatIndex = chats.value.findIndex(c => c.id === chatId);
    if (chatIndex !== -1) {
      chats.value[chatIndex].unreadCount = 0;
      markAsRead(chatId); // 서버에 읽음 표시 업데이트
    }
  }

  // 컴포넌트 마운트 시 채팅방 목록 로드
  onMounted(loadChatRooms);

  return {
    chats,
    isLoading,
    unreadCount,
    startChat,
    sendMessage,
    receiveMessage,
    updateChatMessages,
    markChatAsRead,
    loadChatRooms,
  };
}
