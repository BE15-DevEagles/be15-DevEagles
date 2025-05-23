import { defineStore } from 'pinia';
import { useAuthStore } from './auth';
import { getChatRooms, getChatHistory, markAsRead } from '@/features/chat/api/chatService';
import {
  initializeWebSocket,
  subscribeToChatRoom,
  sendWebSocketMessage,
  disconnectWebSocket,
  isWebSocketConnected,
  getWebSocketStatus,
} from '@/features/chat/api/webSocketService';

export const useChatStore = defineStore('chat', {
  state: () => ({
    chats: [],
    isLoading: false,
    error: null,
    isWebSocketConnected: false,
    currentChatId: null,
  }),

  getters: {
    unreadCount: state => {
      return state.chats.reduce((total, chat) => total + (chat.unreadCount || 0), 0);
    },

    getCurrentChat: state => {
      return state.chats.find(chat => chat.id === state.currentChatId);
    },

    getChatById: state => {
      return chatId => state.chats.find(chat => chat.id === chatId);
    },
  },

  actions: {
    // 웹소켓 초기화
    async initializeWebSocketConnection() {
      if (!isWebSocketConnected()) {
        initializeWebSocket();

        // 연결 상태 모니터링
        const checkConnection = () => {
          this.isWebSocketConnected = isWebSocketConnected();
          if (!this.isWebSocketConnected) {
            setTimeout(checkConnection, 1000);
          }
        };

        setTimeout(checkConnection, 1000);
      } else {
        this.isWebSocketConnected = true;
      }
    },

    // 채팅방 목록 로드
    async loadChatRooms() {
      try {
        this.isLoading = true;
        this.error = null;

        const chatRooms = await getChatRooms();

        if (chatRooms && Array.isArray(chatRooms)) {
          this.chats = chatRooms.map(room => this.transformChatRoom(room));
        } else {
          this.chats = [];
        }
      } catch (err) {
        console.error('채팅방 목록 로딩 실패:', err);
        this.error = '채팅방 목록을 불러오는데 실패했습니다.';
        this.chats = [];
      } finally {
        this.isLoading = false;
      }
    },

    // 백엔드 채팅방 데이터를 프론트엔드 형식으로 변환
    transformChatRoom(room) {
      const authStore = useAuthStore();
      const otherParticipants = room.participants?.filter(p => p.userId !== authStore.userId) || [];
      const otherParticipant = otherParticipants[0];

      return {
        id: room.id,
        name: room.name || this.getDisplayName(room, otherParticipant),
        type: room.type,
        isOnline: otherParticipant?.isOnline || false,
        thumbnail: otherParticipant?.userThumbnail || null,
        lastMessage: room.lastMessage?.content || '',
        lastMessageTime: this.formatLastMessageTime(room.lastMessage?.timestamp),
        lastMessageTimestamp: room.lastMessage?.timestamp,
        unreadCount: room.unreadCount || 0,
        participants: room.participants || [],
        createdAt: room.createdAt,
        updatedAt: room.updatedAt,
      };
    },

    // 채팅방 표시명 결정
    getDisplayName(room, otherParticipant) {
      if (room.type === 'AI') {
        return '🤖 AI 어시스턴트';
      }

      if (room.type === 'GROUP') {
        return room.name || '그룹 채팅';
      }

      return otherParticipant?.userName || '알 수 없는 사용자';
    },

    // 시간 포맷팅
    formatLastMessageTime(timestamp) {
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

      return msgTime.toLocaleDateString('ko-KR', {
        month: 'short',
        day: 'numeric',
      });
    },

    // 채팅방 선택 및 구독
    async selectChat(chatId) {
      this.currentChatId = chatId;
      await this.markChatAsRead(chatId);

      // 웹소켓 구독
      if (isWebSocketConnected()) {
        subscribeToChatRoom(chatId, message => {
          this.handleIncomingMessage(message);
        });
      }
    },

    // 채팅방 읽음 처리
    async markChatAsRead(chatId) {
      const chatIndex = this.chats.findIndex(c => c.id === chatId);

      if (chatIndex > -1) {
        this.chats[chatIndex].unreadCount = 0;

        try {
          await markAsRead(chatId);
        } catch (error) {
          console.error('읽음 표시 실패:', error);
        }
      }
    },

    // 메시지 전송
    async sendMessage(chatId, content) {
      const authStore = useAuthStore();

      if (!isWebSocketConnected()) {
        console.error('웹소켓이 연결되지 않았습니다.');
        return false;
      }

      const success = sendWebSocketMessage(chatId, content, authStore.userId, authStore.userName);

      if (success) {
        this.updateChatAfterSending(chatId, content);
      }

      return success;
    },

    // 메시지 전송 후 UI 업데이트
    updateChatAfterSending(chatId, content) {
      const chatIndex = this.chats.findIndex(c => c.id === chatId);

      if (chatIndex > -1) {
        const chat = this.chats[chatIndex];
        chat.lastMessage = content;
        chat.lastMessageTime = '방금';
        chat.lastMessageTimestamp = new Date().toISOString();

        // 해당 채팅을 목록 맨 위로 이동
        if (chatIndex > 0) {
          this.chats.splice(chatIndex, 1);
          this.chats.unshift(chat);
        }
      }
    },

    // 웹소켓 메시지 수신 처리
    handleIncomingMessage(message) {
      const authStore = useAuthStore();
      const chatIndex = this.chats.findIndex(c => c.id === message.chatroomId);

      if (chatIndex > -1) {
        const chat = this.chats[chatIndex];

        // 메시지가 내가 보낸 것이 아니면 읽지 않은 메시지 증가
        if (message.senderId !== authStore.userId && this.currentChatId !== message.chatroomId) {
          chat.unreadCount = (chat.unreadCount || 0) + 1;
        }

        // 마지막 메시지 업데이트
        chat.lastMessage = message.content;
        chat.lastMessageTime = '방금';
        chat.lastMessageTimestamp = message.timestamp;

        // 해당 채팅을 목록 맨 위로 이동
        if (chatIndex > 0) {
          this.chats.splice(chatIndex, 1);
          this.chats.unshift(chat);
        }
      }
    },

    // 1:1 채팅 시작 또는 찾기
    async startDirectChat(userId, userName, userThumbnail) {
      const authStore = useAuthStore();

      try {
        // 기존 1:1 채팅이 있는지 확인
        let existingChat = this.chats.find(
          chat => chat.type === 'DIRECT' && chat.participants?.some(p => p.userId === userId)
        );

        if (existingChat) {
          return existingChat;
        }

        // 새로운 1:1 채팅방 생성 (백엔드 API 호출)
        const apiBaseUrl = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080';
        const response = await fetch(`${apiBaseUrl}/api/v1/chatrooms`, {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
            Authorization: `Bearer ${authStore.accessToken}`,
          },
          body: JSON.stringify({
            type: 'DIRECT',
            participantIds: [userId],
          }),
        });

        if (response.ok) {
          const result = await response.json();
          const newRoom = result.data || result;
          const transformedRoom = this.transformChatRoom(newRoom);
          this.chats.unshift(transformedRoom);
          return transformedRoom;
        } else {
          throw new Error('채팅방 생성 실패');
        }
      } catch (error) {
        console.error('1:1 채팅 시작 실패:', error);

        // 실패 시 임시 채팅방 객체 반환 (로컬에서만 사용)
        const tempChat = {
          id: 'temp-' + Date.now(),
          name: userName,
          type: 'DIRECT',
          isOnline: false,
          thumbnail: userThumbnail,
          lastMessage: '',
          lastMessageTime: '방금',
          unreadCount: 0,
          participants: [
            { userId: authStore.userId, userName: authStore.userName },
            { userId, userName, userThumbnail },
          ],
          isTemp: true,
        };

        this.chats.unshift(tempChat);
        return tempChat;
      }
    },

    // 초기화
    async initialize() {
      await this.initializeWebSocketConnection();
      await this.loadChatRooms();
    },

    // 정리
    cleanup() {
      disconnectWebSocket();
      this.isWebSocketConnected = false;
      this.currentChatId = null;
    },
  },
});
