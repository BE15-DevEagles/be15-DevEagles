<template>
  <tr>
    <td class="text-dark font-one-liner-semibold" style="width: 20%">{{ log.userName }}</td>
    <td class="text-gray" style="width: 60%">{{ log.summary }}</td>
    <td class="text-gray" style="width: 20%">{{ formattedDate }}</td>
  </tr>
</template>

<script setup>
  import { computed } from 'vue';
  const { log } = defineProps({
    log: {
      type: Object,
      required: true,
    },
  });
  const formattedDate = computed(() => {
    // ISO 문자열 → YYYY-MM-DD 포맷으로 변환
    return log.writtenAt?.slice(0, 10) ?? '';
  });
</script>
