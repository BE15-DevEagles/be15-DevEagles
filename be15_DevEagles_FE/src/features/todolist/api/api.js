import api from '@/api/axios.js';

/* 🗓️ 1. 내 캘린더 일정 조회 */
export function fetchMyCalendarEvents() {
  return api.get('/todos/calendar/my');
}

/* 2. 할 일 상세 조회 */
export function fetchTodoDetail(todoId) {
  return api.get(`/todos/${todoId}`);
}

/* 3. 팀 캘린더 일정 조회 */
export function fetchTeamCalendarEvents(teamId) {
  return api.get(`/todos/calendar/team/${teamId}`);
}

/* 4. 할 일 상세 조회 */
export function fetchTeamTodoDetail(todoId) {
  return api.get(`/todos/team/detail/${todoId}`);
}

/* 5. 팀 Todo 목록 조회 */
export function fetchTeamTodos({ teamId, userId = [], status = 'all', page = 1, size = 10 }) {
  return api.get(`/todos/team/${teamId}`, {
    params: { userId, status, page, size },
  });
}
