<script setup>
  import { ref, onMounted, watch } from 'vue';
  import { useRouter } from 'vue-router';
  import WorkLog from '@/features/worklog/components/WorkLog.vue';
  import Pagination from '@/components/common/components/Pagaination.vue';
  import { searchWorklogs, fetchMyWorklogs } from '@/features/worklog/api/worklog.js';
  import BaseButton from '@/components/common/components/BaseButton.vue';
  import { useAuthStore } from '@/store/auth.js';
  import { useTeamStore } from '@/store/team.js';

  const teamStore = useTeamStore();
  const worklogs = ref([]);
  const searchType = ref('all');
  const searchInput = ref('');
  const sortType = ref('latest');
  const startDate = ref('');
  const endDate = ref('');
  const showDatePicker = ref(false);
  const currentPage = ref(1);
  const totalPages = ref(0);
  const worklogScope = ref('mine');
  const pageSize = 10;
  const teamId = teamStore.currentTeamId;
  const router = useRouter();
  const authStore = useAuthStore();

  function goToCreatePage() {
    router.push({
      name: 'WorklogCreate',
      query: {
        username: authStore.name,
        teamId: teamStore.currentTeamId,
      },
    });
  }

  function formatDateTime(date) {
    return date ? date + ' 00:00:00' : null;
  }

  function isSearchMode() {
    return (
      searchInput.value.trim() !== '' ||
      (startDate.value && endDate.value) ||
      searchType.value !== 'all'
    );
  }

  function clearDates() {
    startDate.value = '';
    endDate.value = '';
  }

  function switchScope(scope) {
    if (worklogScope.value !== scope) {
      worklogScope.value = scope;
      currentPage.value = 1;
      fetchWorklogs();
    }
  }

  function triggerSearch() {
    currentPage.value = 1;
    fetchWorklogs();
  }

  function onPageChange(page) {
    if (page !== currentPage.value) {
      currentPage.value = page;
      fetchWorklogs();
    }
  }

  function goToDetail(log) {
    router.push({ name: 'WorklogDetail', params: { id: log.worklogId } });
  }

  async function fetchWorklogs() {
    try {
      const commonParams = {
        teamId,
        page: currentPage.value,
        size: pageSize,
        sort: sortType.value,
      };

      if (isSearchMode()) {
        const request = {
          ...commonParams,
          searchType: searchType.value.toUpperCase(),
          keyword: searchInput.value,
          startDate: formatDateTime(startDate.value),
          endDate: formatDateTime(endDate.value),
        };
        const response = await searchWorklogs(request);
        const { content, pagination } = response.data.data;
        worklogs.value = content;
        totalPages.value = Math.ceil(pagination.totalItems / pageSize);
      } else {
        const url = worklogScope.value === 'mine' ? '/myworklog' : '/team';
        const response = await fetchMyWorklogs('/worklog' + url, commonParams);
        const { content, pagination } = response.data.data;
        worklogs.value = content;
        totalPages.value = Math.ceil(pagination.totalItems / pageSize);
      }
    } catch (error) {
      console.error('업무일지 로드 실패:', error);
    }
  }

  onMounted(fetchWorklogs);

  watch(sortType, () => {
    currentPage.value = 1;
    fetchWorklogs();
  });
</script>

<template>
  <section class="p-4">
    <!-- 탭 및 작성 버튼 -->
    <div class="d-flex gap-2 mb-3 justify-content-between align-items-center">
      <BaseButton class="btn btn-accent" @click="goToCreatePage"> 업무일지 작성 </BaseButton>
      <div class="d-flex gap-2">
        <BaseButton
          class="btn tab-toggle"
          :class="{ selected: worklogScope === 'team' }"
          @click="switchScope('team')"
        >
          팀별 업무일지
        </BaseButton>
        <BaseButton
          class="btn tab-toggle"
          :class="{ selected: worklogScope === 'mine' }"
          @click="switchScope('mine')"
        >
          내 업무일지
        </BaseButton>
      </div>
    </div>

    <!-- 검색 필터 -->
    <div class="d-flex flex-wrap align-items-end justify-content-center gap-3 mb-3">
      <div
        class="d-flex align-items-center"
        style="
          position: relative;
          border: 1px solid var(--color-gray-300);
          border-radius: 0.5rem;
          overflow: hidden;
          height: 42px;
          min-width: 400px;
          flex-grow: 1;
        "
      >
        <select
          v-model="searchType"
          class="input"
          style="
            border: none;
            border-right: 1px solid var(--color-gray-300);
            border-radius: 0;
            height: 100%;
            max-width: 7rem;
          "
        >
          <option value="all">전체</option>
          <option value="author">작성자</option>
          <option value="keyword">키워드</option>
        </select>
        <div class="position-relative w-100" style="height: 100%">
          <input
            v-model.trim="searchInput"
            type="text"
            class="input w-100"
            placeholder="검색어 입력"
            style="border: none; border-radius: 0; height: 100%; padding-right: 2.5rem"
            @keyup.enter="triggerSearch"
          />
          <span class="search-icon" style="cursor: pointer" @click="triggerSearch">
            <svg
              xmlns="http://www.w3.org/2000/svg"
              fill="none"
              viewBox="0 0 24 24"
              stroke="currentColor"
              style="width: 16px; height: 16px"
            >
              <path
                stroke-linecap="round"
                stroke-linejoin="round"
                stroke-width="2"
                d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"
              />
            </svg>
          </span>
        </div>
      </div>

      <!-- 정렬 + 날짜 선택 -->
      <div class="d-flex gap-2 align-items-center" style="height: 42px">
        <select v-model="sortType" class="input" style="max-width: 10rem; height: 100%">
          <option value="latest">최신순</option>
          <option value="created">등록순</option>
        </select>

        <BaseButton
          class="btn btn-outline btn-primary"
          style="height: 100%; white-space: nowrap"
          @click="showDatePicker = !showDatePicker"
        >
          📅 날짜 선택
        </BaseButton>
      </div>
    </div>

    <!-- 날짜 필터 -->
    <div v-if="showDatePicker" class="d-flex gap-3 mb-4 justify-content-center align-items-center">
      <input v-model="startDate" type="date" class="input" />
      <span class="font-one-liner-semibold align-self-center">~</span>
      <input v-model="endDate" type="date" class="input" />
      <BaseButton class="btn btn-sm btn-gray" @click="clearDates">초기화</BaseButton>
    </div>

    <!-- 테이블 -->
    <div class="table-responsive">
      <table class="table table-striped text-center w-100" style="table-layout: fixed">
        <thead>
          <tr>
            <th style="width: 20%; text-align: center">작성자</th>
            <th style="width: 60%; text-align: center">제목</th>
            <th style="width: 20%; text-align: center">작성일자</th>
          </tr>
        </thead>
        <tbody>
          <WorkLog
            v-for="log in worklogs"
            :key="log.worklogId"
            :log="log"
            @click="goToDetail(log)"
          />
          <tr v-if="worklogs.length === 0">
            <td colspan="3" class="text-center text-gray">조회된 업무일지가 없습니다.</td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- 페이지네이션 -->
    <div class="mt-5 d-flex justify-content-center">
      <Pagination
        :key="`${totalPages}-${currentPage}`"
        :current-page="currentPage"
        :total-pages="totalPages"
        @update:current-page="onPageChange"
      />
    </div>
  </section>
</template>

<style scoped>
  .tab-toggle {
    border: 1px solid var(--color-primary-main);
    background: transparent;
    color: var(--color-primary-main);
  }
  .tab-toggle.selected {
    background: var(--color-primary-main);
    color: var(--color-neutral-white);
  }
  .search-icon {
    position: absolute;
    right: 1rem;
    top: 50%;
    transform: translateY(-50%);
    color: var(--color-gray-400);
    pointer-events: none;
    display: flex;
    align-items: center;
    justify-content: center;
  }
  .btn-accent {
    background-color: var(--color-primary-500) !important;
    color: var(--color-neutral-white) !important;
    border: none;
  }
  .btn-accent:hover {
    background-color: var(--color-primary-500) !important;
    color: var(--color-neutral-white) !important;
  }
</style>
