<template>
  <div
    class="border-b border-[var(--color-gray-200)] flex-shrink-0 flex-grow-0 overflow-hidden h-full"
  >
    <div class="p-3 border-b border-[var(--color-gray-200)] flex-shrink-0">
      <h2 class="font-section-inner">팀원</h2>
    </div>

    <div class="overflow-y-auto h-full" style="height: calc(100% - 49px)">
      <div
        v-for="(member, idx) in teamMembers"
        :key="member.userId || idx"
        class="p-3 border-b border-[var(--color-gray-200)] hover:bg-[var(--color-gray-100)] transition-colors"
      >
        <div class="flex items-center">
          <div class="relative mr-3 flex-shrink-0">
            <div
              class="w-10 h-10 rounded-md overflow-hidden bg-[var(--color-primary-300)] flex items-center justify-center text-white font-one-liner-semibold"
            >
              {{ member.userName?.charAt(0) || '?' }}
            </div>
            <div
              class="absolute -bottom-1 -right-1 w-3 h-3 rounded-full border-2 border-white"
              :class="
                member.isOnline ? 'bg-[var(--color-success-300)]' : 'bg-[var(--color-gray-400)]'
              "
            ></div>
          </div>

          <div class="flex-grow mr-2">
            <h3 class="font-one-liner-semibold">{{ member.userName || '이름 없음' }}</h3>
          </div>

          <!-- 버튼들 동일 -->
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
  import { defineProps, defineEmits, watch } from 'vue';

  /**
   * Props:
   * teamMembers - 팀원 목록 배열
   *
   * 예시:
   * [
   *   {
   *     id: 1,
   *     name: '김경록',
   *     isOnline: true,
   *     userThumbnail: null,
   *     workLog: ['오늘 할 일 완료', '새로운 기능 구현', '회의 참석'],
   *   },
   *   ...
   * ]
   */
  const props = defineProps({
    teamMembers: {
      type: Array,
      required: true,
    },
  });

  // 팀원 목록 변경 감지
  watch(
    () => props.teamMembers,
    newMembers => {
      console.log('팀원 목록 변경됨:', newMembers);
    },
    { deep: true }
  );

  /**
   * Emits:
   * view-worklog - 일지보기 버튼 클릭 시 발생, 인자로 팀원 객체 전달
   * start-chat - 대화하기 버튼 클릭 시 발생, 인자로 팀원 객체 전달
   *
   * 예시:
   * this.$emit('view-worklog', { id: 1, name: '김경록', ... })
   * this.$emit('start-chat', { id: 1, name: '김경록', ... })
   */
  defineEmits(['view-worklog', 'start-chat']);
</script>
