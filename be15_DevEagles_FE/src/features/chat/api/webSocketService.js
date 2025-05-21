import SockJS from 'sockjs-client';
import { Stomp } from '@stomp/stompjs';
import { useAuthStore } from '@/store/auth.js';

let stompClient = null;
let subscriptions = {};
let reconnectAttempts = 0;
const MAX_RECONNECT_ATTEMPTS = 5;

export function initializeWebSocket(store = null) {
  if (stompClient !== null && stompClient.connected) {
    console.log('웹소켓이 이미 연결되어 있습니다.');
    return;
  }

  reconnectAttempts = 0;
  connectWebSocket(store);
}

function connectWebSocket(providedStore = null) {
  if (reconnectAttempts >= MAX_RECONNECT_ATTEMPTS) {
    console.error('최대 재연결 시도 횟수를 초과했습니다.');
    return;
  }

  // 스토어 접근을 함수 내부로 이동
  let authStore = providedStore;
  try {
    if (!authStore) {
      authStore = useAuthStore();
    }
  } catch (error) {
    console.error('Auth 스토어 접근 실패. 인증 정보 없이 연결합니다.', error);
    authStore = { accessToken: null };
  }

  const socket = new SockJS(`${import.meta.env.VITE_API_BASE_URL}/ws`);
  stompClient = Stomp.over(socket);

  stompClient.debug = true;

  const headers = {};
  if (authStore.accessToken) {
    headers['Authorization'] = `Bearer ${authStore.accessToken}`;
  }

  stompClient.connect(
    headers,
    () => {
      console.log('웹소켓 연결 성공');
      reconnectAttempts = 0;
      // 기존 구독 복원
      const currentSubscriptions = { ...subscriptions };
      subscriptions = {};
      Object.keys(currentSubscriptions).forEach(destination => {
        const callback = currentSubscriptions[destination].callback;
        subscribeToChatRoom(destination.split('.')[1], callback, authStore);
      });
    },
    error => {
      console.error('웹소켓 연결 실패:', error?.message || '알 수 없는 오류');
      reconnectAttempts++;
      const delay = Math.min(1000 * (reconnectAttempts + 1), 10000); // 최대 10초까지 지수적 대기
      setTimeout(() => {
        connectWebSocket(authStore);
      }, delay);
    }
  );

  socket.onclose = () => {
    console.log('웹소켓 연결이 종료되었습니다. 재연결 시도...');
    if (reconnectAttempts < MAX_RECONNECT_ATTEMPTS) {
      const delay = Math.min(1000 * (reconnectAttempts + 1), 10000);
      setTimeout(() => {
        connectWebSocket(authStore);
      }, delay);
    }
  };
}

export function subscribeToChatRoom(chatRoomId, callback, providedStore = null) {
  if (!stompClient || !stompClient.connected) {
    console.log('웹소켓 연결이 없습니다. 연결을 시도합니다.');
    initializeWebSocket(providedStore);
    // 웹소켓 연결이 완료될 때까지 구독 정보 저장
    subscriptions[`chatroom.${chatRoomId}`] = { callback };
    setTimeout(() => subscribeToChatRoom(chatRoomId, callback, providedStore), 1000);
    return;
  }

  // 백엔드 경로 형식에 맞춰 변경 (/topic/chatroom.{chatRoomId})
  const destination = `/topic/chatroom.${chatRoomId}`;

  if (subscriptions[destination]) {
    unsubscribe(destination);
  }

  subscriptions[destination] = {
    subscription: stompClient.subscribe(destination, message => {
      try {
        const receivedMessage = JSON.parse(message.body);
        callback(receivedMessage);
      } catch (error) {
        console.error('메시지 처리 중 오류 발생:', error);
      }
    }),
    callback,
  };

  console.log(`채팅방 ${chatRoomId} 구독 완료`);

  // 읽음 상태 이벤트도 구독
  const readStatusDestination = `/topic/chatroom.${chatRoomId}.read`;
  subscriptions[readStatusDestination] = {
    subscription: stompClient.subscribe(readStatusDestination, message => {
      try {
        const readStatus = JSON.parse(message.body);
        console.log(`읽음 상태 업데이트:`, readStatus);
        // 필요한 경우 읽음 상태 이벤트 처리
      } catch (error) {
        console.error('읽음 상태 이벤트 처리 중 오류 발생:', error);
      }
    }),
    callback: null,
  };
}

export function sendWebSocketMessage(chatRoomId, message, providedStore = null) {
  if (!stompClient || !stompClient.connected) {
    console.error('웹소켓 연결이 없습니다. 메시지를 보낼 수 없습니다.');
    initializeWebSocket(providedStore); // 연결 재시도
    return false;
  }

  // 스토어 접근을 함수 내부로 이동
  let authStore = providedStore;
  try {
    if (!authStore) {
      authStore = useAuthStore();
    }
  } catch (error) {
    console.error('Auth 스토어 접근 실패. 인증 정보 없이 메시지를 보낼 수 없습니다.', error);
    return false;
  }

  try {
    stompClient.send(
      `/app/chat.send`,
      {},
      JSON.stringify({
        chatroomId: chatRoomId,
        content: message,
        senderId: authStore.userId,
        senderName: authStore.userName,
        messageType: 'TEXT',
      })
    );
    return true;
  } catch (error) {
    console.error('메시지 전송 중 오류 발생:', error);
    return false;
  }
}

export function unsubscribe(destination) {
  if (subscriptions[destination] && subscriptions[destination].subscription) {
    subscriptions[destination].subscription.unsubscribe();
    delete subscriptions[destination];
    console.log(`${destination} 구독 해제 완료`);
  }
}

export function disconnectWebSocket() {
  if (stompClient && stompClient.connected) {
    Object.keys(subscriptions).forEach(destination => {
      if (subscriptions[destination].subscription) {
        subscriptions[destination].subscription.unsubscribe();
      }
    });
    subscriptions = {};

    stompClient.disconnect();
    stompClient = null;
    console.log('웹소켓 연결 종료');
  }
}
