<script setup>
  import { onMounted, ref, watch } from 'vue';
  import { Calendar } from '@fullcalendar/core';
  import dayGridPlugin from '@fullcalendar/daygrid';
  import timeGridPlugin from '@fullcalendar/timegrid';
  import interactionPlugin from '@fullcalendar/interaction';

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
