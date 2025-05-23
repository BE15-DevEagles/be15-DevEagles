import { ref, watch } from 'vue';

export function useDebounce(fn, delay = 300) {
  const isLoading = ref(false);
  let timeoutId = null;

  const debouncedFn = (...args) => {
    isLoading.value = true;

    if (timeoutId) {
      clearTimeout(timeoutId);
    }

    timeoutId = setTimeout(() => {
      fn(...args);
      isLoading.value = false;
    }, delay);
  };

  const cancel = () => {
    if (timeoutId) {
      clearTimeout(timeoutId);
      timeoutId = null;
      isLoading.value = false;
    }
  };

  const flush = (...args) => {
    cancel();
    fn(...args);
    isLoading.value = false;
  };

  return {
    debouncedFn,
    isLoading,
    cancel,
    flush,
  };
}

export function useDebouncedWatch(source, callback, delay = 300) {
  const isLoading = ref(false);
  let timeoutId = null;

  const stopWatcher = watch(
    source,
    (newValue, oldValue) => {
      isLoading.value = true;

      if (timeoutId) {
        clearTimeout(timeoutId);
      }

      timeoutId = setTimeout(() => {
        callback(newValue, oldValue);
        isLoading.value = false;
      }, delay);
    },
    { immediate: false }
  );

  const cancel = () => {
    if (timeoutId) {
      clearTimeout(timeoutId);
      timeoutId = null;
      isLoading.value = false;
    }
  };

  const flush = () => {
    if (timeoutId) {
      clearTimeout(timeoutId);
      callback(source.value);
      isLoading.value = false;
    }
  };

  return {
    isLoading,
    cancel,
    flush,
    stop: () => {
      cancel();
      stopWatcher();
    },
  };
}
