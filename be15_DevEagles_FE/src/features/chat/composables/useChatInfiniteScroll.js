import { ref } from 'vue';

export function useChatInfiniteScroll(loadMoreCallback) {
  const isLoading = ref(false);
  const hasMore = ref(true);
  const error = ref(null);
  let targetElement = null;

  const loadMore = async () => {
    if (isLoading.value || !hasMore.value) {
      console.log('[useChatInfiniteScroll] 로딩 중이거나 더 이상 로드할 메시지 없음');
      return { hasMore: false };
    }

    try {
      isLoading.value = true;
      error.value = null;

      console.log('[useChatInfiniteScroll] 이전 메시지 로딩 시작');
      const result = await loadMoreCallback();

      if (typeof result === 'object' && result !== null) {
        if ('hasMore' in result) {
          hasMore.value = result.hasMore;
          console.log('[useChatInfiniteScroll] hasMore 업데이트:', result.hasMore);
        }
      }

      return result;
    } catch (err) {
      error.value = err.message || '이전 메시지 로딩 중 오류가 발생했습니다.';
      console.error('[useChatInfiniteScroll] 오류:', err);
      return { hasMore: false };
    } finally {
      isLoading.value = false;
    }
  };

  const checkAndLoadMore = (scrollTop, threshold = 30) => {
    if (scrollTop <= threshold && hasMore.value && !isLoading.value) {
      console.log('[useChatInfiniteScroll] 상단 임계값 도달, 로딩 트리거:', {
        scrollTop,
        threshold,
      });
      return loadMore();
    }
    return null;
  };

  const setTargetElement = element => {
    targetElement = element;
    console.log('[useChatInfiniteScroll] 타겟 엘리먼트 설정됨');
  };

  const reset = () => {
    hasMore.value = true;
    error.value = null;
    isLoading.value = false;
    console.log('[useChatInfiniteScroll] 상태 초기화됨');
  };

  return {
    isLoading,
    hasMore,
    error,
    setTargetElement,
    reset,
    loadMore,
    checkAndLoadMore,
  };
}
