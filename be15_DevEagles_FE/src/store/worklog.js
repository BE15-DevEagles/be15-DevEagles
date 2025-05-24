import { defineStore } from 'pinia';

export const useWorklogStore = defineStore('worklog', {
  state: () => ({
    preview: null, // type: PreviewType | null
    loading: false,
  }),
  actions: {
    setWorklogPreview(data) {
      this.preview = data;
    },
    setLoading(flag) {
      this.loading = flag;
    },
    clear() {
      this.preview = null;
      this.loading = false;
    },
  },
  getters: {
    hasPreview: state => !!state.preview,
  },
  persist: {
    paths: ['preview'], // 'loading'은 유지할 필요 없음
  },
});
