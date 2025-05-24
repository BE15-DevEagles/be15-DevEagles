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

/* 6. 내 미완료 Todo 리스트 (D-Day 포함) */
export function fetchMyDdayTodos({ page = 1, size = 10 }) {
  return api.get('/todos/dday/my', {
    params: { page, size },
  });
}

/* 7. 업무일지 작성 여부 조회 */
export function fetchWorklogWrittenStatus(teamId) {
  return api.get('/todos/worklog/written', {
    params: { teamId },
  });
}

// 8. 특정 팀의 내 미완료 Todo 리스트 (D-day 포함)
export function fetchMyTeamDdayTodos({ teamId, page = 1, size = 10 }) {
  return api.get(`/todos/dday/team/${teamId}`, {
    params: { page, size },
  });
}

/* 9. 할 일 수정 */
export function updateTodo(todoId, payload) {
  return api.put(`/todos/${todoId}`, payload);
}

/* 10. 할 일 삭제 */
export function deleteTodo(todoId) {
  return api.delete(`/todos/${todoId}`);
}

/* 11. 할 일 완료 처리 */
export function completeTodo(todoId) {
  return api.put(`/todos/${todoId}/complete`);
}
