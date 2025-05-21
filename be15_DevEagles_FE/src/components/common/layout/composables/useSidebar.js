import { ref, watch } from 'vue';

export function useSidebar() {
  // 사이드바 상태
  const isCollapsed = ref(false);
  const shouldRenderContent = ref(!isCollapsed.value); // 콘텐츠 렌더링 제어 상태
  const currentMode = ref('team'); // 'team' 또는 'chat'
  const selectedChat = ref(null);
  let contentRenderTimer = null; // setTimeout 타이머 ID

  // isCollapsed 상태 변경 감지
  watch(isCollapsed, newVal => {
    if (contentRenderTimer) clearTimeout(contentRenderTimer);

    if (newVal) {
      // true가 됨, 접힘
      shouldRenderContent.value = false; // 내용 즉시 숨김
      if (selectedChat.value) {
        // 사이드바 접힐 때 열려있는 채팅창 닫기
        selectedChat.value = null;
      }
    } else {
      // false가 됨, 펼쳐짐
      // 펼쳐지는 애니메이션 동안 내용 숨김
      shouldRenderContent.value = false;
      contentRenderTimer = setTimeout(() => {
        shouldRenderContent.value = true; // 애니메이션 후 내용 표시
      }, 200); // transition duration (200ms)과 일치
    }
  });

  // 사이드바 접기/펼치기 토글
  const toggleCollapse = () => {
    isCollapsed.value = !isCollapsed.value;
  };

  // 모드 전환 (팀원/채팅)
  const toggleMode = mode => {
    currentMode.value = mode;

    // 모드 전환 시 채팅창 닫기
    if (!isCollapsed.value) {
      selectedChat.value = null;
    }
  };

  // 컴포넌트 언마운트 시 타이머 정리
  const cleanup = () => {
    if (contentRenderTimer) {
      clearTimeout(contentRenderTimer);
    }
  };

  return {
    isCollapsed,
    shouldRenderContent,
    currentMode,
    selectedChat,
    toggleCollapse,
    toggleMode,
    cleanup,
  };
}
