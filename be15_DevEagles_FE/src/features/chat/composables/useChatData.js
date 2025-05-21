import { ref, computed, inject } from 'vue';

export function useChatData() {
  // 채팅 데이터
  const chats = ref([
    {
      id: 1,
      name: '김경록',
      isOnline: true,
      userThumbnail: null,
      lastMessage: '안녕하세요! 오늘 회의 자료 확인했습니다.',
      lastMessageTime: '14:12 전',
      unreadCount: 0,
      messages: [
        { text: '안녕하세요! 오늘 회의 자료 확인했습니다.', time: '14:12', isMe: false },
        { text: '네! 확인 감사합니다.', time: '14:15', isMe: true },
        { text: '추가 자료는 언제쯤 받을 수 있을까요?', time: '14:20', isMe: false },
        { text: '내일 오전까지 보내드리겠습니다.', time: '14:22', isMe: true },
      ],
    },
    {
      id: 2,
      name: '박준석',
      isOnline: true,
      userThumbnail: null,
      lastMessage: '일정입니다!',
      lastMessageTime: '어제',
      unreadCount: 2,
      messages: [
        { text: '안녕하세요, 다음 주 일정 공유드립니다.', time: '어제 15:30', isMe: false },
        { text: '감사합니다. 일정 확인했습니다.', time: '어제 16:45', isMe: true },
        { text: '미팅 시간이 30분 당겨질 수 있을까요?', time: '어제 17:20', isMe: false },
        { text: '네, 가능합니다!', time: '어제 17:22', isMe: false },
      ],
    },
    {
      id: 3,
      name: '류현진',
      isOnline: false,
      userThumbnail: null,
      lastMessage: '음이에용 뭐 먹을까요...',
      lastMessageTime: '2일 전',
      unreadCount: 0,
      messages: [
        { text: '점심 뭐 먹을까요?', time: '2일 전 12:30', isMe: false },
        { text: '저는 김치찌개 어떨까요?', time: '2일 전 12:31', isMe: true },
        { text: '좋아요! 김치찌개 먹어요.', time: '2일 전 12:32', isMe: false },
      ],
    },
    {
      id: 4,
      name: '코디 폰스',
      isOnline: true,
      userThumbnail: null,
      lastMessage: 'how are you bro ...',
      lastMessageTime: '5일 전',
      unreadCount: 0,
      messages: [
        { text: 'Hello there!', time: '5일 전 09:15', isMe: false },
        { text: 'Hey, how are you?', time: '5일 전 09:17', isMe: true },
        { text: "I'm good, thanks! how are you bro?", time: '5일 전 09:20', isMe: false },
      ],
    },
  ]);

  // 읽지 않은 메시지 수 계산
  const unreadCount = computed(() => {
    return chats.value.reduce((total, chat) => total + (chat.unreadCount || 0), 0);
  });

  // 팀원과 채팅 시작
  function startChat(member) {
    // 해당 팀원과의 채팅이 이미 있는지 확인
    let chat = chats.value.find(c => c.id === member.id);

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

  // 메시지 전송
  function sendMessage(chatId, messageText) {
    if (!messageText.trim()) {
      return null;
    }

    // 현재 시간 생성
    const now = new Date();
    const hours = now.getHours().toString().padStart(2, '0');
    const minutes = now.getMinutes().toString().padStart(2, '0');
    const timeString = `${hours}:${minutes}`;

    // 새 메시지 객체 생성
    const message = {
      text: messageText,
      time: timeString,
      isMe: true,
    };

    // 채팅 메시지에 추가
    const chatIndex = chats.value.findIndex(c => c.id === chatId);
    if (chatIndex !== -1) {
      chats.value[chatIndex].messages.push(message);
      chats.value[chatIndex].lastMessage = messageText;
      chats.value[chatIndex].lastMessageTime = '방금';

      // 해당 채팅을 목록의 맨 위로 이동
      const chat = chats.value.splice(chatIndex, 1)[0];
      chats.value.unshift(chat);

      return chat;
    }

    return null;
  }

  // 자동 응답 생성
  function generateAutoReply(chatId, messageText) {
    const now = new Date();
    const hours = now.getHours().toString().padStart(2, '0');
    const minutes = (now.getMinutes() + 1).toString().padStart(2, '0');

    const autoReply = {
      text: `[자동응답] "${messageText}"에 대한 답변은 조금 후에 드리겠습니다.`,
      time: `${hours}:${minutes}`,
      isMe: false,
    };

    const chatIndex = chats.value.findIndex(c => c.id === chatId);
    if (chatIndex !== -1) {
      chats.value[chatIndex].messages.push(autoReply);
      chats.value[chatIndex].lastMessage = autoReply.text;
      return chats.value[chatIndex];
    }

    return null;
  }

  // 채팅 열기 시 읽음 표시
  function markChatAsRead(chatId) {
    const chatIndex = chats.value.findIndex(c => c.id === chatId);
    if (chatIndex !== -1) {
      chats.value[chatIndex].unreadCount = 0;
    }
  }

  return {
    chats,
    unreadCount,
    startChat,
    sendMessage,
    generateAutoReply,
    markChatAsRead,
  };
}
