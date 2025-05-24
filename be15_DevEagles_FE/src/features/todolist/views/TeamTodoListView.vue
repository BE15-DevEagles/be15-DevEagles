<template>
  <div class="todo-list-container">
    <h1 class="title">TodoList</h1>

    <!-- 필터 탭 -->
    <div class="filter-tabs">
      <BaseButton
        v-for="type in ['전체', '미완료', '완료']"
        :key="type"
        :type="selectedStatus === mapTabToStatus(type) ? 'primary' : 'secondary'"
        :outline="selectedStatus !== mapTabToStatus(type)"
        @click="selectStatus(type)"
      >
        {{ type }}
      </BaseButton>
    </div>

    <!-- 팀 멤버 필터 (오른쪽 정렬) -->
    <div class="member-filter px-2">
      <div
        v-for="member in teamMembers"
        :key="member.userId"
        class="member-avatar"
        :class="{ selected: selectedUserId === Number(member.userId) }"
        @click="toggleUser(Number(member.userId))"
      >
        <template v-if="member.userThumbnailUrl">
          <div
            class="w-full h-full bg-cover bg-center rounded-full"
            :style="{ backgroundImage: `url(${member.userThumbnailUrl})` }"
          ></div>
        </template>
        <template v-else>
          <div
            class="w-full h-full bg-gray-400 text-white flex items-center justify-center rounded-full font-bold"
            style="background: var(--color-primary-main)"
          >
            {{ member.userName?.charAt(0) || '?' }}
          </div>
        </template>
      </div>
    </div>

    <!-- 목록 테이블 -->
    <table class="todo-table">
      <colgroup>
        <col style="width: 15%" />
        <col style="width: 70%" />
        <col style="width: 15%" />
      </colgroup>
      <thead>
        <tr>
          <th>작성자</th>
          <th>내용</th>
          <th>디데이</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="todo in todos" :key="todo.todoId">
          <td>{{ todo.userName }}</td>
          <td>{{ todo.content }}</td>
          <td>{{ getDday(todo.dueDate) }}</td>
        </tr>
      </tbody>
    </table>
  </div>

  <BasePagination
    :current-page="currentPage"
    :total-pages="totalPages"
    @update:current-page="onPageChange"
  />
</template>

<script setup>
  import { computed, ref, watch } from 'vue';
  import { useRoute } from 'vue-router';
  import { storeToRefs } from 'pinia';
  import { fetchTeamTodos } from '@/features/todolist/api/api.js';
  import { useTeamStore } from '@/store/team';
  import BasePagination from '@/components/common/components/Pagaination.vue';
  import BaseButton from '@/components/common/components/BaseButton.vue';

  const route = useRoute();
  const teamStore = useTeamStore();
  const { currentTeamId } = storeToRefs(teamStore);

  const todos = ref([]);
  const selectedUserId = ref([]);

  const teamMembers = computed(() => teamStore.teamMembers);
  const validStatuses = ['all', 'completed', 'incomplete'];
  const selectedStatus = ref(
    validStatuses.includes(route.query.status) ? route.query.status : 'all'
  );

  const currentPage = ref(1);
  const totalPages = ref(1);

  const onPageChange = page => {
    currentPage.value = page;
    fetchTodos();
  };

  const fetchTodos = async () => {
    if (!currentTeamId.value) return;
    const { data } = await fetchTeamTodos({
      teamId: currentTeamId.value,
      userId: selectedUserId.value,
      status: selectedStatus.value,
      page: currentPage.value,
      size: 10,
    });
    todos.value = data.data.content;
    totalPages.value = data.data.pagination.totalPages;
  };

  const getDday = dueDateStr => {
    const today = new Date();
    const due = new Date(dueDateStr);
    const diff = Math.ceil((due - today) / (1000 * 60 * 60 * 24));
    if (diff === 0) return 'D-DAY';
    return diff > 0 ? `D - ${diff}` : `D + ${Math.abs(diff)}`;
  };

  const toggleUser = userId => {
    selectedUserId.value = selectedUserId.value === userId ? null : userId;
    fetchTodos();
  };

  const selectStatus = label => {
    selectedStatus.value = mapTabToStatus(label);
    fetchTodos();
  };

  const mapTabToStatus = label => {
    if (label === '전체') return 'all';
    if (label === '완료') return 'completed';
    if (label === '미완료') return 'incomplete';
    return 'all';
  };
  watch(teamMembers, val => {
    console.log('[팀 멤버 변경 감지됨]', val);
  });
  watch(
    currentTeamId,
    async newId => {
      if (newId) {
        await fetchTodos();
      }
    },
    { immediate: true }
  );
</script>

<style scoped>
  .pagination {
    display: flex;
    justify-content: center;
    list-style: none;
    padding: 1rem 0;
    gap: 0.5rem;
  }

  .todo-list-container {
    margin: 2rem;
    padding: 0;
    background: white;
    border-radius: 8px;
    overflow-y: auto;
    box-shadow: 0 0 4px rgba(0, 0, 0, 0.05);
  }

  .title {
    font-size: 1.5rem;
    font-weight: bold;
    margin-bottom: 1.5rem;
  }

  .filter-tabs {
    display: flex;
    gap: 1rem;
    margin-bottom: 1.5rem;
  }

  .member-filter {
    display: flex;
    justify-content: flex-end;
    gap: 0.75rem;
    margin-bottom: 1.5rem;
    padding-right: 1rem;
  }

  .member-avatar {
    width: 40px;
    height: 40px;
    border-radius: 50%;
    cursor: pointer;
    overflow: hidden;
  }

  .member-avatar.selected {
    outline: 2px solid var(--color-primary-main);
    outline-offset: 2px;
  }

  .todo-table {
    width: 100%;
    table-layout: fixed;
    border-collapse: collapse;
    font-size: 0.95rem;
  }

  .todo-table th,
  .todo-table td {
    padding: 0.75rem 0.5rem;
    border-top: 1px solid #e5e7eb;
    text-align: center;
  }
</style>
