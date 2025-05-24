<template>
  <div
    class="w-[515px] max-h-[calc(100vh-100px)] rounded-xl border border-[var(--color-gray-200)] bg-white shadow-sm flex flex-col"
  >
    <div class="flex-1 overflow-y-auto p-6">
      <!-- 썸네일 + 팀명 + 버튼 -->
      <div class="flex items-center justify-between mb-6">
        <div class="flex items-center space-x-4">
          <div
            class="w-[80px] h-[80px] rounded-full overflow-hidden bg-gray-200 flex items-center justify-center"
            :class="{ 'cursor-pointer': isTeamLeader }"
            @click="openThumbnailModal"
          >
            <img
              v-if="teamThumbnailUrl"
              :src="teamThumbnailUrl"
              :alt="teamName"
              class="w-full h-full object-cover"
            />
            <span v-else class="text-white text-xl font-bold">
              {{ teamName?.charAt(0) || '?' }}
            </span>
          </div>
          <h2 class="text-xl font-bold">{{ teamName || '팀 이름 없음' }}</h2>
        </div>
        <div class="flex gap-3">
          <BaseButton v-if="isTeamLeader" type="" class="text-black hover:text-[#dc2626]" size="sm"
            >팀 삭제</BaseButton
          >
          <BaseButton type="" class="text-black hover:text-[#dc2626]" size="sm">팀 탈퇴</BaseButton>
        </div>
      </div>

      <!-- 팀 소개 -->
      <div
        class="w-full min-h-[96px] p-3 border border-gray-300 rounded-md bg-gray-50 text-gray-700 whitespace-pre-wrap"
      >
        {{ teamIntroduction || '팀 설명이 아직 없습니다.' }}
      </div>

      <!-- 팀원 목록 -->
      <div class="mt-6">
        <div class="flex justify-between items-center mb-2">
          <span class="text-sm text-gray-500">팀원</span>
          <button
            v-if="isTeamLeader"
            class="text-sm text-gray-500 hover:text-[#064e3b]"
            @click="isInviteModalOpen = true"
          >
            + 팀원 초대
          </button>
        </div>
        <div class="max-h-[200px] overflow-y-auto pr-1 border rounded-md">
          <ul class="space-y-2 p-2">
            <li
              v-for="member in members"
              :key="member.userId"
              class="flex justify-between items-center border-b border-gray-200 pb-2"
            >
              <div class="flex items-center gap-6">
                <span class="text-base font-semibold text-gray-800 w-24">{{
                  member.userName
                }}</span>
                <span class="text-sm text-gray-500">{{ member.email }}</span>
              </div>
              <input
                v-if="isTeamLeader"
                v-model="selectedUserId"
                type="radio"
                :value="member.userId"
                class="accent-[#257180]"
              />
            </li>
          </ul>
        </div>
      </div>

      <!-- 하단 버튼 -->
      <div class="p-4 border-t border-gray-100">
        <div v-if="isTeamLeader" class="flex gap-2 justify-end mb-3">
          <BaseButton type="secondary" size="sm">추방</BaseButton>
          <BaseButton type="secondary" size="sm">팀장 양도</BaseButton>
        </div>
      </div>
    </div>
  </div>

  <!-- 모달들 -->
  <TeamMemberInviteModal v-model="isInviteModalOpen" @submit="handleInvite" />
  <UpdateTeamThumbnailModal
    v-model="isThumbnailModalOpen"
    :current-url="teamThumbnail"
    @submit="handleThumbnailSubmit"
  />
</template>

<script setup>
  import { computed, onMounted, ref } from 'vue';
  import { useRoute } from 'vue-router';
  import { useAuthStore } from '@/store/auth';
  import api from '@/api/axios';
  import BaseButton from '@/components/common/components/BaseButton.vue';
  import TeamMemberInviteModal from '@/features/team/components/TeamMemberInviteModal.vue';
  import UpdateTeamThumbnailModal from '@/features/team/components/UpdateTeamThumbnailModal.vue';
  import { getTeamMembers } from '@/features/team/api/team';

  const route = useRoute();
  const teamId = computed(() => Number(route.params.teamId));
  const authStore = useAuthStore();

  const currentUserId = ref(null);
  const teamOwnerId = ref(null);
  const teamName = ref('');
  const teamIntroduction = ref('');
  const teamThumbnailUrl = ref(null);

  const isInviteModalOpen = ref(false);
  const isThumbnailModalOpen = ref(false);
  const isUploading = ref(false);

  const isTeamLeader = computed(() => Number(teamOwnerId.value) === Number(currentUserId.value));

  function openThumbnailModal() {
    if (isTeamLeader.value) isThumbnailModalOpen.value = true;
  }

  async function fetchTeamInfo(id) {
    try {
      const res = await api.get(`/teams/teams/${id}`);
      const data = res.data.data;
      teamName.value = data.teamName || '';
      teamIntroduction.value = data.introduction || '';
      teamOwnerId.value = data.userId;
      teamThumbnailUrl.value = data.url || null;
    } catch (err) {
      console.error('팀 정보 불러오기 실패:', err);
    }
  }

  async function updateThumbnail(file) {
    try {
      isUploading.value = true;
      const formData = new FormData();
      formData.append('file', file);
      await api.post(`/teams/${teamId.value}/thumbnail`, formData, {
        headers: { 'Content-Type': 'multipart/form-data' },
      });
      await fetchTeamInfo(teamId.value);
    } catch (err) {
      console.error('썸네일 업로드 실패:', err);
    } finally {
      isUploading.value = false;
    }
  }

  function handleInvite(email) {
    console.log('✅ 초대 이메일:', email);
  }

  const selectedUserId = ref(null);
  const members = ref([]);

  async function fetchMembers() {
    try {
      members.value = await getTeamMembers(teamId.value);
    } catch (e) {
      console.error('팀원 불러오기 실패:', e);
    }
  }

  onMounted(() => {
    authStore.initAuth();
    currentUserId.value = authStore.userId;
    if (!isNaN(teamId.value) && teamId.value > 0) {
      fetchTeamInfo(teamId.value);
      fetchMembers();
    }
  });
</script>
