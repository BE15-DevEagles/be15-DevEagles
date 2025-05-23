<script setup>
  import { onMounted, ref, watch } from 'vue';
  import { Calendar } from '@fullcalendar/core';
  import dayGridPlugin from '@fullcalendar/daygrid';
  import timeGridPlugin from '@fullcalendar/timegrid';
  import interactionPlugin from '@fullcalendar/interaction';
  import tippy from 'tippy.js';
  import 'tippy.js/dist/tippy.css';

  import { fetchTodoDetail } from '@/features/todolist/api/api.js';

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
        const todoId = info.event.id;
        const teamId = info.event.extendedProps.teamId;
        const color = teamColorMap[teamId];

        if (color) {
          info.el.style.backgroundColor = color.bg;
          info.el.style.color = color.fg;
        }

        try {
          const res = await fetchTodoDetail(todoId);
          const todo = res.data;

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
  /* light-border 테마의 tippy 툴팁을 info 스타일로 오버라이드 */
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
