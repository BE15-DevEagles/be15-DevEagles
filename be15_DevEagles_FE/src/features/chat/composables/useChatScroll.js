import { ref, nextTick } from 'vue';

export function useChatScroll() {
  const shouldScrollToBottom = ref(true);
  const lastScrollTop = ref(0);
  let scrollContainer = null;

  const setScrollContainer = element => {
    scrollContainer = element;
    console.log('[useChatScroll] 스크롤 컨테이너 설정됨');
  };

  const scrollToBottom = (force = false) => {
    if (!scrollContainer) return;

    if (force || shouldScrollToBottom.value) {
      nextTick(() => {
        if (scrollContainer) {
          scrollContainer.scrollTop = scrollContainer.scrollHeight;
          shouldScrollToBottom.value = true;
          console.log('[useChatScroll] 하단으로 스크롤됨');
        }
      });
    }
  };

  const maintainScrollPosition = async callback => {
    if (!scrollContainer) return;

    // 스크롤 위치 저장
    const currentScrollTop = scrollContainer.scrollTop;
    const currentScrollHeight = scrollContainer.scrollHeight;

    // 콜백 실행 및 반환값 저장
    const result = await callback();

    // 스크롤 위치 복원
    nextTick(() => {
      if (scrollContainer) {
        const newScrollHeight = scrollContainer.scrollHeight;
        const heightDifference = newScrollHeight - currentScrollHeight;
        scrollContainer.scrollTop = currentScrollTop + heightDifference;

        console.log('[useChatScroll] 스크롤 위치 유지됨:', {
          이전높이: currentScrollHeight,
          새높이: newScrollHeight,
          높이차이: heightDifference,
          조정된스크롤: scrollContainer.scrollTop,
        });
      }
    });

    // 콜백의 반환값 반환
    return result;
  };

  const handleScroll = (event, onScrollToTop) => {
    const { scrollTop, scrollHeight, clientHeight } = event.target;

    // 상단 스크롤 감지 (무한 스크롤용)
    if (scrollTop <= 30 && onScrollToTop) {
      onScrollToTop(scrollTop);
    }

    // 하단 근처인지 감지
    const isNearBottom = scrollHeight - scrollTop - clientHeight < 100;

    // 사용자 의도 파악
    if (scrollTop < lastScrollTop.value - 20) {
      // 위로 스크롤 - 자동 스크롤 비활성화
      shouldScrollToBottom.value = false;
    } else if (isNearBottom) {
      // 하단 근처 - 자동 스크롤 활성화
      shouldScrollToBottom.value = true;
    }

    lastScrollTop.value = scrollTop;
  };

  const reset = () => {
    shouldScrollToBottom.value = true;
    lastScrollTop.value = 0;
    console.log('[useChatScroll] 상태 초기화됨');
  };

  return {
    shouldScrollToBottom,
    setScrollContainer,
    scrollToBottom,
    maintainScrollPosition,
    handleScroll,
    reset,
  };
}
