<template>
  <div class="p-3 border-b border-[var(--color-gray-200)] flex items-center justify-between">
    <button
      class="text-[var(--color-gray-600)] hover:text-[var(--color-primary-300)] transition-colors mr-2"
      @click="$emit('toggle-collapse')"
    >
      <svg
        xmlns="http://www.w3.org/2000/svg"
        class="h-5 w-5"
        fill="none"
        viewBox="0 0 24 24"
        stroke="currentColor"
      >
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7" />
      </svg>
    </button>
  </div>
</template>

<script setup>
  /**
   * Emits:
   * toggle-collapse - 사이드바 접기/펼치기 버튼 클릭 시 발생
   *
   * 예시:
   * this.$emit('toggle-collapse')
   */
  defineEmits(['toggle-collapse']);
</script>
