import axios from 'axios';
import { useAuthStore } from '@/store/auth.js';

const api = axios.create({
  baseURL: `http://localhost:8080/api/v1`,
  withCredentials: true,
});

const exceptToken = ['/auth/login'];

api.interceptors.request.use(
  config => {
    const authStore = useAuthStore();
    const token = authStore.accessToken;

    const shouldSkipToken = exceptToken.some(url => config.url?.includes(url));

    if (token && !shouldSkipToken) {
      config.headers.Authorization = `Bearer ${token}`;
    }

    return config;
  },
  error => {
    return Promise.reject(error);
  }
);

export const login = params => api.post(`/auth/login`, params);

export const validUserStatus = () => api.post(`/auth/valid`);
