import axios from 'axios';
import { useAuthStore } from '@/store/auth.js';

const api = axios.create({
  baseURL: `http://localhost:8080/api/v1`,
  withCredentials: true,
});

const exceptToken = [
  { method: 'post', url: '/auth/login' },
  { method: 'post', url: '/users$' },
  { method: 'post', url: '/users/duplcheck' },
  { method: 'post', url: '/auth/sendauth' },
  { method: 'post', url: '/auth/verify' },
];

api.interceptors.request.use(
  config => {
    const authStore = useAuthStore();
    const token = authStore.accessToken;

    const shouldSkipToken = exceptToken.some(
      pattern => pattern.method === config.method && new RegExp(pattern.url).test(config.url)
    );

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

export const logout = () => api.post(`/auth/logout`);

export const validUserStatus = () => api.post(`/auth/valid`);

export const findUserId = params => api.post(`/auth/findid`, params);

export const sendAuth = params => api.post(`/auth/sendauth`, params);

export const verifyEmailCode = params => api.post(`/auth/verify`, params);

export const signUp = formData =>
  api.post(`/users`, formData, {
    headers: {
      'Content-Type': 'multipart/form-data',
    },
  });

export const checkDuplicate = params => api.post(`/users/duplcheck`, params);

export const mypage = () => api.get(`/users/me`);

export const updateUserInfo = formData =>
  api.patch(`/users/mod`, formData, {
    headers: {
      'Content-Type': 'multipart/form-data',
    },
  });

export const verifyPassword = password => api.post(`/users/valid`, password);

export const editPassword = password =>
  api.patch(
    '/users/mod/pwd',
    { password },
    {
      headers: {
        'Content-Type': 'application/json',
      },
    }
  );

export const withdrawUser = () => api.delete(`/users`);
