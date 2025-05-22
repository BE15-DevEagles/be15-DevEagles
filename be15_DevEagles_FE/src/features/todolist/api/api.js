import axios from 'axios';

const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL,
});

/* 🗓️ 1. 내 캘린더 일정 조회 */
export function fetchMyCalendarEvents() {
  return api.get('/todos/calendar/my');
}
