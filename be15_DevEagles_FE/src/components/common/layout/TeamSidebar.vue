<template>
  <aside class="bg-[var(--color-gray-800)] h-full w-16 flex flex-col items-center py-4">
    <!-- 로고 영역 -->
    <div class="w-10 h-10 mb-6">
      <img
        src="/assets/image/logo-goody.png"
        alt="Goody Logo"
        class="w-full h-full object-contain"
      />
    </div>

    <!-- 팀 썸네일 목록 -->
    <div class="flex flex-col items-center space-y-4 overflow-y-auto flex-grow">
      <div
        v-for="(team, index) in teams"
        :key="index"
        class="w-10 h-10 rounded-md flex items-center justify-center cursor-pointer relative overflow-hidden group transition-all duration-200 shadow-drop"
        :class="[
          team.active
            ? 'bg-[var(--color-primary-300)]'
            : 'bg-[var(--color-gray-600)] hover:bg-[var(--color-gray-500)]',
        ]"
        @click="selectTeam(team.id)"
      >
        <img
          v-if="team.thumbnail"
          :src="team.thumbnail"
          :alt="team.name"
          class="w-full h-full object-cover"
        />
        <span v-else class="text-white font-xs-semibold">{{ team.name.charAt(0) }}</span>

        <!-- 활성 표시기 -->
        <div
          v-if="team.active"
          class="absolute -left-1 top-1/2 transform -translate-y-1/2 w-1 h-6 bg-[var(--color-info-500)] rounded-r-full"
        ></div>

        <!-- 호버 툴팁 -->
        <div
          class="absolute left-14 z-10 invisible opacity-0 group-hover:visible group-hover:opacity-100 transition-opacity duration-200 bg-[var(--color-gray-800)] text-white px-3 py-2 rounded font-small shadow-drop"
        >
          {{ team.name }}
        </div>
      </div>
    </div>

    <!-- 구분선 -->
    <div class="w-8 h-px bg-[var(--color-gray-600)] my-4"></div>

    <!-- 팀 생성 버튼 -->
    <div
      class="w-10 h-10 rounded-md bg-[var(--color-gray-600)] flex items-center justify-center cursor-pointer hover:bg-[var(--color-primary-300)] group relative transition-all duration-200 shadow-drop"
      @click="createTeam"
    >
      <svg
        xmlns="http://www.w3.org/2000/svg"
        class="h-5 w-5 text-white"
        fill="none"
        viewBox="0 0 24 24"
        stroke="currentColor"
      >
        <path
          stroke-linecap="round"
          stroke-linejoin="round"
          stroke-width="2"
          d="M12 6v6m0 0v6m0-6h6m-6 0H6"
        />
      </svg>

      <!-- 호버 툴팁 -->
      <div
        class="absolute left-14 z-10 invisible opacity-0 group-hover:visible group-hover:opacity-100 transition-opacity duration-200 bg-[var(--color-gray-800)] text-white px-3 py-2 rounded font-small shadow-drop"
      >
        팀 생성하기
      </div>
    </div>
  </aside>
</template>

<script setup>
  import { ref, defineEmits } from 'vue';

  const emit = defineEmits(['selectTeam', 'createTeam']);

  // Mock 데이터 나중에 교체하세요요
  const teams = ref([
    { id: 1, name: '홀', thumbnail: null, active: true },
    { id: 2, name: '리', thumbnail: null, active: false },
    { id: 3, name: '쉬', thumbnail: null, active: false },
    { id: 4, name: '잇', thumbnail: null, active: false },
  ]);

  // 팀 선택 함수
  const selectTeam = teamId => {
    teams.value = teams.value.map(team => ({
      ...team,
      active: team.id === teamId,
    }));

    // 부모 컴포넌트에 선택된 팀 ID 전달
    emit('selectTeam', teamId);
  };

  // 팀 생성 함수
  const createTeam = () => {
    // 팀 생성 모달이나 페이지로 이동하는 로직 구현
    emit('createTeam');
  };
</script>
