import { defineStore } from 'pinia';
import { useTeamStore } from './team';
import api from '@/api/axios';

export const useChatStore = defineStore('chat', {
  state: () => ({
    chats: [],
    loading: false,
    error: null,
  }),

  getters: {
    unreadCount: state => {
      return state.chats.reduce((count, chat) => count + (chat.unreadCount || 0), 0);
    },
  },

  actions: {
    // 팀의 채팅방 로드
    async fetchTeamChats() {
      const teamStore = useTeamStore();
      const teamId = teamStore.currentTeamId;

      if (!teamId) return;

      this.loading = true;
      try {
        // 실제 API 구현 전까지는 테스트 데이터 사용
        // const response = await api.get(`/api/v1/teams/${teamId}/chatrooms`);
        // this.chats = response.data.data;

        // 테스트 데이터 - 팀별로 다른 채팅 목록
        let testChats = [];

        if (teamId === 'team1') {
          testChats = [
            {
              id: 'chat1',
              name: '김코딩',
              isOnline: true,
              userThumbnail: null,
              lastMessage: '안녕하세요! 오늘 회의 자료 확인했습니다.',
              lastMessageTime: '14:12 전',
              unreadCount: 2,
              messages: [
                { text: '안녕하세요!', time: '14:10', isMe: false },
                { text: '오늘 회의 자료 확인했습니다.', time: '14:12', isMe: false },
              ],
            },
            {
              id: 'chat2',
              name: '팀 공지',
              isOnline: undefined,
              userThumbnail: null,
              lastMessage: '이번 주 금요일 발표 준비 관련 회의 있습니다.',
              lastMessageTime: '어제',
              unreadCount: 0,
              messages: [
                { text: '이번 주 금요일 발표 준비 관련 회의 있습니다.', time: '어제', isMe: false },
              ],
            },
          ];
        } else if (teamId === 'team2') {
          testChats = [
            {
              id: 'chat3',
              name: '이해커',
              isOnline: false,
              userThumbnail: null,
              lastMessage: '코드 리뷰 부탁드립니다.',
              lastMessageTime: '3시간 전',
              unreadCount: 1,
              messages: [{ text: '코드 리뷰 부탁드립니다.', time: '3시간 전', isMe: false }],
            },
            {
              id: 'chat4',
              name: '코드봉인 채널',
              isOnline: undefined,
              userThumbnail: null,
              lastMessage: '다음 스프린트 일정 공유합니다.',
              lastMessageTime: '2일 전',
              unreadCount: 0,
              messages: [{ text: '다음 스프린트 일정 공유합니다.', time: '2일 전', isMe: false }],
            },
          ];
        } else {
          testChats = [
            {
              id: 'chat5',
              name: '박알고',
              isOnline: true,
              userThumbnail: null,
              lastMessage: '스터디 자료 공유 드립니다.',
              lastMessageTime: '방금 전',
              unreadCount: 3,
              messages: [{ text: '스터디 자료 공유 드립니다.', time: '방금 전', isMe: false }],
            },
          ];
        }

        this.chats = testChats;
      } catch (err) {
        this.error = err.message;
        console.error('채팅 목록 로드 실패:', err);
      } finally {
        this.loading = false;
      }
    },

    // 채팅방 선택
    markChatAsRead(chatId) {
      const chat = this.chats.find(c => c.id === chatId);
      if (chat) {
        chat.unreadCount = 0;
      }
    },

    // 메시지 전송 (테스트용)
    sendMessage(chatId, text) {
      const chat = this.chats.find(c => c.id === chatId);
      if (!chat) return null;

      const now = new Date();
      const hours = now.getHours().toString().padStart(2, '0');
      const minutes = now.getMinutes().toString().padStart(2, '0');
      const timeString = `${hours}:${minutes}`;

      // 메시지 추가
      const newMessage = {
        text,
        time: timeString,
        isMe: true,
      };

      chat.messages.push(newMessage);
      chat.lastMessage = text;
      chat.lastMessageTime = '방금 전';

      // 자동 응답 (테스트용)
      setTimeout(() => {
        this.receiveMessage(chatId);
      }, 1000);

      return chat;
    },

    // 메시지 수신 (테스트용)
    receiveMessage(chatId) {
      const chat = this.chats.find(c => c.id === chatId);
      if (!chat) return null;

      const responses = [
        '네, 확인했습니다.',
        '알겠습니다. 바로 처리하겠습니다.',
        '감사합니다!',
        '좋은 아이디어네요.',
        '미팅은 언제로 잡을까요?',
      ];

      const randomResponse = responses[Math.floor(Math.random() * responses.length)];
      const now = new Date();
      const hours = now.getHours().toString().padStart(2, '0');
      const minutes = now.getMinutes().toString().padStart(2, '0');
      const timeString = `${hours}:${minutes}`;

      // 메시지 추가
      const newMessage = {
        text: randomResponse,
        time: timeString,
        isMe: false,
      };

      chat.messages.push(newMessage);
      chat.lastMessage = randomResponse;
      chat.lastMessageTime = '방금 전';

      // 읽지 않은 메시지 수 증가 (현재 선택되어 있지 않으면)
      // 실제로는 현재 선택된 채팅방인지 체크하는 로직이 필요
      chat.unreadCount = (chat.unreadCount || 0) + 1;

      return chat;
    },
  },
});
