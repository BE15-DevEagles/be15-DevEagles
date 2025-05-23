import { defineStore } from 'pinia';
import { useAuthStore } from './auth';
import { getChatRooms, markAsRead } from '@/features/chat/api/chatService';
import {
  initializeWebSocket,
  subscribeToChatRoom,
  sendWebSocketMessage,
  disconnectWebSocket,
  isWebSocketConnected,
} from '@/features/chat/api/webSocketService';
import { useNotifications } from '@/features/chat/composables/useNotifications';
import { transformChatRoom } from '@/features/chat/utils/chatUtils';
import { formatLastMessageTime } from '@/features/chat/utils/timeUtils';

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
          const authStore = useAuthStore();
          this.chats = chatRooms.map(room => transformChatRoom(room, authStore.userId));

          // 알림 설정 초기화
          const { initializeFromChatData } = useNotifications();
          initializeFromChatData(this.chats);
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
      const { showChatNotification } = useNotifications();
      const chatIndex = this.chats.findIndex(c => c.id === message.chatroomId);

      if (chatIndex > -1) {
        const chat = this.chats[chatIndex];

        // 메시지가 내가 보낸 것이 아니면 처리
        if (message.senderId !== authStore.userId) {
          // 현재 채팅방이 아닌 경우 읽지 않은 메시지 증가
          if (this.currentChatId !== message.chatroomId) {
            chat.unreadCount = (chat.unreadCount || 0) + 1;
          }

          // 알림 표시 (현재 활성화된 채팅방이 아닌 경우에만)
          if (this.currentChatId !== message.chatroomId) {
            showChatNotification({
              chatroomId: message.chatroomId,
              senderName: message.senderName || '알 수 없는 사용자',
              content: message.content,
            });
          }
        }

        // 마지막 메시지 업데이트
        chat.lastMessage = message.content;
        chat.lastMessageTime = formatLastMessageTime(message.timestamp);
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
          const transformedRoom = transformChatRoom(newRoom, authStore.userId);
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
