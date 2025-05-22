<template>
  <aside class="bg-[var(--color-gray-800)] h-full w-16 flex flex-col items-center py-4">
    <!-- 팀 썸네일 목록 -->
    <div class="flex flex-col items-center space-y-4 overflow-y-auto flex-grow">
      <div
        v-for="team in teamStore.teams"
        :key="team.teamId"
        class="w-10 h-10 rounded-md flex items-center justify-center cursor-pointer relative overflow-hidden group transition-all duration-200 shadow-drop"
        :class="[
          team.teamId === teamStore.currentTeamId
            ? 'bg-[var(--color-primary-300)]'
            : 'bg-[var(--color-gray-600)] hover:bg-[var(--color-gray-500)]',
        ]"
        @click="switchTeam(team.teamId)"
      >
        <!-- 썸네일 이미지 or 팀 이름 첫 글자 -->
        <img
          v-if="team.teamThumbnailUrl"
          :src="team.teamThumbnailUrl"
          :alt="team.teamName"
          class="w-full h-full object-cover"
        />
        <span v-else class="text-white font-xs-semibold">
          {{ team.teamName?.charAt(0) || '?' }}
        </span>

        <!-- 활성 팀 표시기 -->
        <div
          v-if="team.teamId === teamStore.currentTeamId"
          class="absolute -left-1 top-1/2 transform -translate-y-1/2 w-1 h-6 bg-[var(--color-info-500)] rounded-r-full"
        ></div>

        <!-- 호버 툴팁 -->
        <div
          class="absolute left-14 z-10 invisible opacity-0 group-hover:visible group-hover:opacity-100 transition-opacity duration-200 bg-[var(--color-gray-800)] text-white px-3 py-2 rounded font-small shadow-drop"
        >
          {{ team.teamName || '팀 이름 없음' }}
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
        New Team
      </div>
    </div>
  </aside>

  <!-- 팀 생성 모달 -->
  <CreateTeamModal v-model="showModal" @submit="handleTeamCreate" />
</template>

<script setup>
  import { ref, onMounted, watch } from 'vue';
  import { useTeamStore } from '@/store/team';
  import { useChatStore } from '@/store/chat';
  import CreateTeamModal from '@/features/team/components/CreateTeamModal.vue';

  const teamStore = useTeamStore();
  const chatStore = useChatStore();

  const showModal = ref(false);

  // 팀 데이터 로드
  onMounted(async () => {
    await teamStore.fetchTeams();
  });

  // 팀 전환 처리
  function switchTeam(teamId) {
    teamStore.setCurrentTeam(teamId);
  }

  // 팀 변경 감지 및 관련 데이터 갱신
  watch(
    () => teamStore.currentTeamId,
    newTeamId => {
      if (newTeamId) {
        try {
          chatStore.fetchTeamChats();
          console.log(`팀 변경: ${newTeamId}, 팀원 수: ${teamStore.teamMembers.length}`);
        } catch (err) {
          console.error('팀 관련 데이터 갱신 실패:', err);
        }
      }
    },
    { immediate: true }
  );

  // 팀 생성 모달 제출
  const handleTeamCreate = async ({ teamName, description }) => {
    try {
      await teamStore.createTeam({ teamName, description });
      await teamStore.fetchTeams();
    } catch (e) {
      console.error('팀 생성 실패:', e);
    }
  };

  const createTeam = () => {
    showModal.value = true;
  };
</script>
