import { ref, computed } from 'vue';
import { useToast } from 'vue-toastification';
import {
  getChatNotificationSetting,
  toggleChatNotification,
  getAllNotificationSettings,
} from '@/features/chat/api/chatService';

const notificationSettings = ref(new Map());
const isLoading = ref(false);

export function useNotifications() {
  const toast = useToast();

  const isNotificationEnabled = chatId => {
    return notificationSettings.value.get(chatId) !== false;
  };

  const toggleNotification = async chatId => {
    try {
      isLoading.value = true;
      const result = await toggleChatNotification(chatId);

      // 백엔드에서 반환된 설정값으로 업데이트
      const notificationEnabled = result.notificationEnabled;
      notificationSettings.value.set(chatId, notificationEnabled);

      return notificationEnabled;
    } catch (error) {
      console.error('알림 설정 변경 실패:', error);
      toast.error('알림 설정 변경에 실패했습니다.');
      return notificationSettings.value.get(chatId) !== false;
    } finally {
      isLoading.value = false;
    }
  };

  const setNotificationEnabled = (chatId, enabled) => {
    notificationSettings.value.set(chatId, enabled);
  };

  // 채팅방 데이터에서 알림 설정 초기화
  const initializeFromChatData = chats => {
    chats.forEach(chat => {
      if (chat.id && chat.notificationEnabled !== undefined) {
        notificationSettings.value.set(chat.id, chat.notificationEnabled);
      }
    });
  };

  // 백엔드에서 모든 알림 설정 로드
  const loadNotificationSettings = async () => {
    try {
      isLoading.value = true;
      const settings = await getAllNotificationSettings();

      // 설정을 Map으로 변환
      settings.forEach(setting => {
        notificationSettings.value.set(setting.chatRoomId, setting.notificationEnabled);
      });
    } catch (error) {
      console.error('알림 설정 로드 실패:', error);
    } finally {
      isLoading.value = false;
    }
  };

  // 특정 채팅방 알림 설정 로드
  const loadChatNotificationSetting = async chatId => {
    try {
      const setting = await getChatNotificationSetting(chatId);
      notificationSettings.value.set(chatId, setting.notificationEnabled);
      return setting.notificationEnabled;
    } catch (error) {
      console.error('채팅방 알림 설정 로드 실패:', error);
      // 실패 시 기본값(true) 반환
      notificationSettings.value.set(chatId, true);
      return true;
    }
  };

  const showChatNotification = message => {
    const { chatroomId, senderName, content } = message;

    if (!isNotificationEnabled(chatroomId)) {
      return;
    }

    if ('Notification' in window && Notification.permission === 'granted') {
      new Notification(`${senderName}님이 메시지를 보냈습니다`, {
        body: content,
        icon: '/favicon.ico',
        tag: `chat-${chatroomId}`,
      });
    }

    toast.info(`${senderName}: ${content}`, {
      timeout: 3000,
      onClick: () => {
        window.focus();
      },
    });
  };

  const requestNotificationPermission = async () => {
    if ('Notification' in window) {
      if (Notification.permission === 'default') {
        const permission = await Notification.requestPermission();
        return permission === 'granted';
      }
      return Notification.permission === 'granted';
    }
    return false;
  };

  const hasNotificationSupport = computed(() => {
    return 'Notification' in window;
  });

  const notificationPermission = computed(() => {
    return 'Notification' in window ? Notification.permission : 'denied';
  });

  return {
    isNotificationEnabled,
    toggleNotification,
    setNotificationEnabled,
    initializeFromChatData,
    loadNotificationSettings,
    loadChatNotificationSetting,
    showChatNotification,
    requestNotificationPermission,
    hasNotificationSupport,
    notificationPermission,
    isLoading,
  };
}
