<script setup>
  import { onMounted, ref, watch } from 'vue';
  import { Calendar } from '@fullcalendar/core';
  import dayGridPlugin from '@fullcalendar/daygrid';
  import timeGridPlugin from '@fullcalendar/timegrid';
  import interactionPlugin from '@fullcalendar/interaction';
  import tippy from 'tippy.js';
  import 'tippy.js/dist/tippy.css';

  import { fetchTodoDetail } from '@/features/todolist/api/api.js';

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
      editable: true,
      height: 'auto',
      headerToolbar: {
        left: 'prev,next today',
        center: 'title',
        right: 'dayGridMonth,timeGridWeek,timeGridDay',
      },

      eventDidMount: async info => {
        const todoId = info.event.id;
        try {
          const res = await fetchTodoDetail(todoId);
          const todo = res.data;

          const content = `
          <div style="font-size: 13px; line-height: 1.5;">
            <div><strong>${todo.isCompleted ? '✅ 완료' : '🕓 진행 중'}</strong></div>
            <div>${todo.startDate.slice(0, 10)} ~ ${todo.dueDate.slice(0, 10)}</div>
            <div>작성자: ${todo.userId}</div>
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
<style scoped></style>
