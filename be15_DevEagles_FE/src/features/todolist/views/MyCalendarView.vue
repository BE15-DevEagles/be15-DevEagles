<script setup>
  import { ref, onMounted, watch } from 'vue';
  import TodoCalendar from '@/features/todolist/components/TodoCalendar.vue';
  import {
    fetchMyCalendarEvents,
    fetchMyDdayTodos,
    fetchWorklogWrittenStatus,
  } from '@/features/todolist/api/api.js';
  import dayjs from 'dayjs';
  import BasePagination from '@/components/common/components/Pagaination.vue';
  import { useTeamStore } from '@/store/team.js';
  import { useRouter } from 'vue-router';
  import BaseButton from '@/components/common/components/BaseButton.vue';

  const myEvents = ref([]);
  const ddayTodoList = ref([]);
  const currentPage = ref(1);
  const totalPages = ref(1);
  const pageSize = 10;

  const worklogWritten = ref(null); // null | true | false

  const props = defineProps({
    isSidebarCollapsed: Boolean,
  });

  const router = useRouter();
  const teamStore = useTeamStore();

  function formatDday(dday) {
    if (dday > 0) return `D - ${dday}`;
    if (dday === 0) return 'D - DAY';
    return `D + ${Math.abs(dday)}`;
  }

  const fetchDdayTodos = async () => {
    try {
      const res = await fetchMyDdayTodos({ page: currentPage.value, size: pageSize });
      ddayTodoList.value = res.data.data.content;
      totalPages.value = res.data.data.pagination.totalPages;
    } catch (err) {
      console.error('❌ D-day 목록 불러오기 실패:', err);
    }
  };
  console.log('📌 현재 팀 ID:', teamStore.currentTeamId);
  const fetchWorklogStatus = async () => {
    try {
      const teamId = teamStore.currentTeamId;
      if (!teamId) return;
      const res = await fetchWorklogWrittenStatus(teamId);
      worklogWritten.value = res.data.data.written;
      console.log('📘 작성 여부:', res.data.data.written);
    } catch (err) {
      console.error('❌ 워크로그 상태 조회 실패:', err);
    }
  };

  onMounted(async () => {
    const calendarRes = await fetchMyCalendarEvents();
    myEvents.value = calendarRes.data.data.map(todo => ({
      id: todo.todoId,
      title: todo.content,
      start: todo.startDate,
      end: dayjs(todo.dueDate).add(1, 'day').format('YYYY-MM-DD'),
      extendedProps: { teamId: todo.teamId },
    }));

    await fetchDdayTodos();
    await fetchWorklogStatus();
  });

  watch(currentPage, fetchDdayTodos);
</script>

<template>
  <div class="page">
    <div class="calendar-page">
      <div :class="['calendar-section', props.isSidebarCollapsed ? 'wide' : 'narrow']">
        <div class="box">
          <TodoCalendar :events="myEvents" type="my" />
        </div>
      </div>

      <div :class="['todolist-section', props.isSidebarCollapsed ? 'compact' : 'expanded']">
        <div class="box">
          <p class="todo-title">TodoList</p>
          <p class="todo-subtitle" :class="{ unwritten: worklogWritten === false }">
            {{ worklogWritten === false ? '업무일지 미작성' : '업무일지 작성 완료' }}
          </p>
          <div class="todolist-header-row">
            <span>완료</span>
            <span>할 일</span>
            <span>디데이</span>
          </div>

          <ul class="todolist-list">
            <li v-for="todo in ddayTodoList" :key="todo.todoId" class="todolist-item">
              <span><input type="checkbox" /></span>
              <span>{{ todo.content }}</span>
              <span>{{ formatDday(todo.dday) }}</span>
            </li>
          </ul>
          <div class="pagination-wrapper">
            <BasePagination
              :current-page="currentPage"
              :total-pages="totalPages"
              @update:current-page="page => (currentPage = page)"
            />
          </div>
          <div v-if="worklogWritten === false" class="write-worklog-wrapper">
            <BaseButton type="info" size="sm" @click="router.push('/worklog/write')">
              업무일지 작성하러 가기
            </BaseButton>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
  .pagination-wrapper {
    display: flex;
    justify-content: center;
    margin-top: 1rem;
  }

  .todolist-section.expanded {
    flex: 1;
    min-width: 220px;
    max-width: 400px;
  }

  .todolist-section.compact {
    flex: 0 0 150px;
  }

  .box {
    border: 1px solid var(--color-gray-200);
    border-radius: 1rem;
    padding: 0;
    background: var(--color-neutral-white);
    box-shadow: 0 4px 20px rgba(0, 0, 0, 0.05);
  }

  .calendar-wrapper {
    padding: 1.5rem;
    height: 100%;
  }

  .page {
    display: flex;
    justify-content: center;
    width: 100%;
  }

  .box {
    flex: 1;
    height: 100%; /* 부모 높이 채움 */
    border: 1px solid var(--color-gray-200);
    border-radius: 1rem;
    padding: 1.5rem;
    background: var(--color-neutral-white);
    box-shadow: 0 4px 20px rgba(0, 0, 0, 0.05);
  }

  .calendar-page {
    align-items: stretch;
    display: flex;
    width: 100%;
    max-width: 100%;
    box-sizing: border-box;
    justify-content: center;
    gap: 1rem;
    padding: 1rem;
  }

  .calendar-section,
  .todolist-section {
    display: flex;
    flex-direction: column;
  }

  .calendar-box {
    height: 100%;
  }
  .calendar-section {
    width: 600px;
    min-width: 600px;
    max-width: 600px;
    flex-shrink: 0;
  }

  .todolist-section {
    background: white;
    padding: 0;
    border-radius: 8px;
    box-shadow: 0 0 4px rgba(0, 0, 0, 0.05);
    overflow-y: auto;
    transition: all 0.3s ease;
  }

  .todolist-header-row {
    display: grid;
    grid-template-columns: 1fr 2fr 1.1fr;
    font-weight: bold;
    margin-bottom: 0.5rem;
    border-bottom: 1px solid var(--color-gray-300);
    padding-bottom: 4px;
    align-items: center;
    text-align: center;
  }
  .todolist-header-row span {
    display: flex;
    justify-content: center;
    align-items: center;
  }
  .todolist-item span:nth-child(2) {
    overflow: hidden;
    white-space: nowrap;
    text-overflow: ellipsis;
    display: block;
    padding: 0 4px;
  }

  .todolist-list {
    display: flex;
    flex-direction: column;
    font-size: 1rem;
    gap: 0.5rem;
  }

  .todolist-item {
    composes: todo-grid;
    font-size: 13px;
    padding: 10px 0;
    min-height: 44px;
    border-radius: 6px;
    transition: background-color 0.2s ease;
  }

  /* ✅ 소제목 (업무일지 미완료) 강조 */

  .todo-subtitle {
    text-align: center;
    font-size: 1rem;
    font-weight: 600;
    margin-bottom: 1.5rem;
    color: var(--color-success-400);
  }

  /* 업무일지 미작성일 때 */
  .todo-subtitle.unwritten {
    color: var(--color-error-300);
    font-weight: 700;
    font-size: 16px;
    text-align: center;
    line-height: 20.8px;
  }

  .write-worklog-wrapper {
    display: flex;
    justify-content: flex-start;
    margin-top: 1rem;
  }

  .todo-title {
    font-size: 1.5rem;
    margin-bottom: 1.5rem;
    font-weight: 600;
  }

  .todolist-item {
    display: grid;
    grid-template-columns: 1fr 2fr 1.1fr;
    align-items: center;
    text-align: center;
    font-size: 13px;
    padding: 10px 0;
    min-height: 44px;
    border-radius: 6px;
    transition: background-color 0.2s ease;
  }

  .todo-grid {
    display: grid;
    grid-template-columns: 1fr 2fr 1fr;
    align-items: center;
    text-align: center;
  }

  .todolist-item:hover {
    background-color: var(--color-gray-100);
  }

  /* FullCalendar 내부 스타일 */
  ::v-deep(.fc-day-today) {
    background-color: #fff3cd !important;
    font-weight: bold;
  }

  ::v-deep(.fc .fc-button) {
    background: var(--color-primary-main) !important;
    color: var(--color-neutral-white) !important;
  }

  ::v-deep(.fc .fc-button:hover) {
    background: var(--color-primary-400) !important;
  }

  ::v-deep(.fc .fc-button:active) {
    background: var(--color-primary-500) !important;
  }

  ::v-deep(.fc .fc-button:disabled) {
    background: var(--color-gray-200) !important;
    color: var(--color-gray-500) !important;
    cursor: not-allowed;
  }
</style>
