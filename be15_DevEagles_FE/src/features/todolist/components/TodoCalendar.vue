<!-- file: TodoCalendar.vue -->
<script setup>
  import { inject, onMounted, ref, watch } from 'vue';
  import { Calendar } from '@fullcalendar/core';
  import dayGridPlugin from '@fullcalendar/daygrid';
  import timeGridPlugin from '@fullcalendar/timegrid';
  import interactionPlugin from '@fullcalendar/interaction';
  import tippy from 'tippy.js';
  import 'tippy.js/dist/tippy.css';

  import { fetchTodoDetail, fetchTeamTodoDetail } from '@/features/todolist/api/api.js';

  const currentUserId = inject('currentUserId');

  // 유저별 색상 맵
  const userColorMap = {};
  const userColors = [
    '#DA5D77',
    '#E89056',
    '#AF7AC5',
    '#F3C146',
    '#FFAA5C',
    '#B9A26D',
    '#5C87C5',
    '#6AA6AC',
    '#8F8FBF',
    '#62B292',
  ];

  function getColorForUser(userName) {
    if (!userColorMap[userName]) {
      const nextColor = userColors[Object.keys(userColorMap).length % userColors.length];
      userColorMap[userName] = nextColor;
    }
    return userColorMap[userName];
  }

  // 팀별 색상 맵
  const teamColorMap = {
    1: { bg: '#DA5D77', fg: '#000' },
    2: { bg: '#E89056', fg: '#000' },
    3: { bg: '#AF7AC5', fg: '#000' },
    4: { bg: '#F3C146', fg: '#000' },
    5: { bg: '#FFAA5C', fg: '#000' },
    6: { bg: '#B9A26D', fg: '#000' },
    7: { bg: '#5C87C5', fg: '#000' },
    8: { bg: '#6AA6AC', fg: '#000' },
    9: { bg: '#8F8FBF', fg: '#000' },
    10: { bg: '#62B292', fg: '#000' },
  };

  const props = defineProps({
    events: Array,
    type: {
      type: String,
      default: 'my',
      validator: value => ['my', 'team'].includes(value),
    },
  });

  const calendarRef = ref(null);
  let calendarInstance = null;

  watch(
    () => props.events,
    newEvents => {
      if (calendarInstance) {
        calendarInstance.removeAllEvents();
        calendarInstance.addEventSource(newEvents);
      }
    },
    { immediate: true }
  );

  onMounted(() => {
    calendarInstance = new Calendar(calendarRef.value, {
      plugins: [dayGridPlugin, timeGridPlugin, interactionPlugin],
      initialView: 'dayGridMonth',
      selectable: true,
      editable: false,
      eventStartEditable: false,
      eventDurationEditable: false,
      height: 'auto',
      headerToolbar: {
        left: 'prev,next today',
        center: 'title',
        right: 'dayGridMonth,timeGridWeek,timeGridDay',
      },
      eventDidMount: async info => {
        const todoId = Number(info.event.id);
        const fetchFn = props.type === 'team' ? fetchTeamTodoDetail : fetchTodoDetail;

        try {
          const res = await fetchFn(todoId);
          const todo = props.type === 'team' ? res.data.data : res.data;

          if (!todo) {
            console.warn('❗ 상세조회 실패 또는 응답 없음:', res);
            return;
          }

          // ✅ 배경 색상 처리
          if (props.type === 'team') {
            // 팀 캘린더 → 유저별 색상
            const userColor = getColorForUser(todo.userName);
            info.el.style.backgroundColor = userColor;
            info.el.style.color = '#fff';
          } else {
            // 내 캘린더 → 팀별 색상
            const teamColor = teamColorMap[todo.teamId];
            if (teamColor) {
              info.el.style.backgroundColor = teamColor.bg;
              info.el.style.color = teamColor.fg;
            } else {
              info.el.style.backgroundColor = '#257180';
              info.el.style.color = '#fff';
            }
          }

          // ✅ 툴팁 렌더링
          const content = `
          <div style="font-size: 13px; line-height: 1.5;">
            <div><strong>${todo.isCompleted ? '✅ 완료' : '🕓 진행 중'}</strong></div>
            <div>${todo.startDate.slice(0, 10)} ~ ${todo.dueDate.slice(0, 10)}</div>
            <div>작성자: ${todo.userName}</div>
            <div>팀명: ${todo.teamName}</div>
            <div style="margin-top: 4px;">${todo.content}</div>
          </div>
        `;

          tippy(info.el, {
            content,
            allowHTML: true,
            theme: 'light-border',
            placement: 'top',
            offset: [0, 10],
            delay: [100, 0],
          });
        } catch (err) {
          console.error('❌ 툴팁 상세조회 실패:', err);
        }
      },
    });

    calendarInstance.render();
  });
</script>

<template>
  <div>
    <div ref="calendarRef"></div>
  </div>
</template>

<style>
  .tippy-box[data-theme~='light-border'] {
    background-color: var(--color-info-50);
    color: var(--color-neutral-dark);
    border: 1px solid var(--color-info-300);
    font-size: 13px;
    border-radius: 0.5rem;
    padding: 0.75rem;
    box-shadow: 0 8px 20px -4px rgba(0, 0, 0, 0.08);
  }
  .tippy-box[data-theme~='light-border'][data-placement^='top'] > .tippy-arrow::before {
    border-top-color: var(--color-info-300);
  }
  .tippy-box[data-theme~='light-border'][data-placement^='bottom'] > .tippy-arrow::before {
    border-bottom-color: var(--color-info-300);
  }
  .tippy-box[data-theme~='light-border'][data-placement^='left'] > .tippy-arrow::before {
    border-left-color: var(--color-info-300);
  }
  .tippy-box[data-theme~='light-border'][data-placement^='right'] > .tippy-arrow::before {
    border-right-color: var(--color-info-300);
  }
</style>
