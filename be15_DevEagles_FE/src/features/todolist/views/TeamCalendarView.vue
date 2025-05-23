<script setup>
  import { ref, onMounted, watch } from 'vue';
  import TodoCalendar from '@/features/todolist/components/TodoCalendar.vue';

  const myEvents = ref([]);

  const props = defineProps({
    isSidebarCollapsed: Boolean,
  });

  watch(
    () => props.isSidebarCollapsed,
    newVal => {
      console.log('[Sidebar 상태] isSidebarCollapsed:', newVal);
    },
    { immediate: true }
  );

  onMounted(() => {
    // 더미 일정 데이터
    myEvents.value = [
      {
        id: 1,
        title: '팀 회의',
        start: '2025-05-20',
        end: '2025-05-23',
      },
      {
        id: 2,
        title: '기획서 제출 마감',
        start: '2025-05-21',
        end: '2025-05-27',
      },
      {
        id: 3,
        title: '디자인 리뷰',
        start: '2025-04-15',
        end: '2025-04-18',
      },
      {
        id: 4,
        title: '디자인 리뷰',
        start: '2025-05-21',
        end: '2025-05-22',
      },
    ];
  });
</script>

<template>
  <div class="page">
    <div class="calendar-page">
      <div :class="['calendar-section', props.isSidebarCollapsed ? 'wide' : 'narrow']">
        <div class="box">
          <TodoCalendar :events="myEvents" />
        </div>
      </div>

      <div :class="['todolist-section', props.isSidebarCollapsed ? 'compact' : 'expanded']">
        <div class="box">
          <p class="todo-title">todoList</p>
          <p class="todo-subtitle">업무일지 미완료</p>

          <div class="todolist-header-row">
            <span>완료</span>
            <span>내용</span>
            <span>D-day</span>
          </div>

          <ul class="todolist-list">
            <li class="todolist-item">
              <input type="checkbox" />
              <span>요구사항...</span>
              <span>D - 1</span>
            </li>
            <li class="todolist-item">
              <input type="checkbox" />
              <span>도메인...</span>
              <span>D - 1</span>
            </li>
            <li class="todolist-item">
              <input type="checkbox" />
              <span>스타일...</span>
              <span>D - 2</span>
            </li>
          </ul>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
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
    grid-template-columns: 1.5fr 1.8fr 1.8fr;
    text-align: center;
    font-weight: bold;
    margin-bottom: 0.5rem;
    border-bottom: 1px solid var(--color-gray-300);
    padding-bottom: 4px;
  }

  .todolist-list {
    display: flex;
    flex-direction: column;
    font-size: 1rem;
    gap: 0.5rem;
  }

  .todolist-item {
    display: grid;
    grid-template-columns: 1.5fr 1.8fr 1.8fr;
    align-items: center;
    text-align: center;
    font-size: 14px;
  }

  /* ✅ 소제목 (업무일지 미완료) 강조 */
  .todo-subtitle {
    text-align: center;
    font-size: 1rem; /* 기존보다 큼 */
    font-weight: 600;
    color: red;
    margin-bottom: 1.5rem;
  }

  .todo-title {
    font-size: 1.5rem;
    margin-bottom: 1.5rem;
    font-weight: 600;
  }

  .todolist-item {
    display: grid;
    grid-template-columns: 1fr 2fr 1fr;
    align-items: center;
    text-align: center;
    font-size: 13px;
    padding: 10px 0;
    min-height: 44px;
    border-radius: 6px;
    transition: background-color 0.2s ease;
  }

  .todolist-item:hover {
    background-color: var(--color-gray-100);
  }

  /* FullCalendar 내부 스타일 */
  ::v-deep(.fc-event) {
    background-color: #afdee8 !important;
    color: white !important;
    border-radius: 4px;
    padding: 2px 6px;
    font-size: 13px;
  }

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
