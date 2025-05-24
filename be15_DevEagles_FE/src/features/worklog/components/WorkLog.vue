<template>
  <tr class="cursor-pointer hover:bg-gray-50" @click="$emit('click', log)">
    <td class="text-dark font-one-liner-semibold" style="width: 20%">{{ log.userName }}</td>
    <td class="text-gray" style="width: 60%">{{ log.summary }}</td>
    <td class="text-gray" style="width: 20%">{{ formattedDate }}</td>
  </tr>
</template>

<script setup>
  import { computed } from 'vue';

  const props = defineProps({
    log: {
      type: Object,
      required: true,
    },
  });

  const formattedDate = computed(() => {
    return props.log.writtenAt?.slice(0, 10) ?? '';
  });
</script>
