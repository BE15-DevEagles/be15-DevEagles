<script setup>
  import { ref, watch } from 'vue';
  import { DatePicker } from 'v-calendar';
  import BaseModal from '@/components/common/components/BaseModal.vue';
  import BaseButton from '@/components/common/components/BaseButton.vue';
  import { deleteTodo, fetchTodoDetail, updateTodo } from '@/features/todolist/api/api';
  import dayjs from 'dayjs';
  import { useToast } from 'vue-toastification';
  import { nextTick } from 'vue';

  const toast = useToast();
  const props = defineProps({
    modelValue: Boolean,
    todoId: Number,
  });
  const emit = defineEmits(['update:modelValue', 'edit', 'delete']);

  const visible = ref(props.modelValue);
  watch(
    () => props.modelValue,
    val => (visible.value = val)
  );
  watch(visible, val => emit('update:modelValue', val));

  const todo = ref(null);
  const editedContent = ref('');
  const startDateRaw = ref(null);
  const dueDateRaw = ref(null);
  const editedStartDate = ref('');
  const editedDueDate = ref('');
  const isModified = ref(false);
  const originalStartDate = ref(null);
  const originalDueDate = ref(null);
  const showConfirmEdit = ref(false);
  const showConfirmDelete = ref(false);

  const handleEditConfirm = async () => {
    try {
      await updateTodo(todo.value.todoId, {
        content: editedContent.value,
        startDate: new Date(startDateRaw.value),
        dueDate: new Date(dueDateRaw.value),
      });

      toast.success('수정되었습니다.', {
        toastClassName: 'Vue-Toastification__toast--info',
      });

      emit('edit', todo.value.todoId);
      showConfirmEdit.value = false;
      visible.value = false;
      location.reload();
    } catch (err) {
      toast.error('수정에 실패했습니다.');
      console.error('❌ 수정 실패:', err);
    }
  };

  const handleDeleteConfirm = async () => {
    try {
      await deleteTodo(todo.value.todoId);

      toast.success('삭제되었습니다.', {
        toastClassName: 'Vue-Toastification__toast--info',
      });

      emit('delete', todo.value.todoId);
      showConfirmDelete.value = false;
      visible.value = false;
      location.reload();
    } catch (err) {
      toast.error('삭제에 실패했습니다.');
      console.error('❌ 삭제 실패:', err);
    }
  };

  const formatDate = date => dayjs(date).format('YYYY-MM-DD HH:mm');

  const checkIfModified = () => {
    const trimmedContent = editedContent.value.trim();
    isModified.value =
      trimmedContent.length > 0 &&
      (trimmedContent !== todo.value?.content.trim() ||
        editedStartDate.value !== formatDate(todo.value?.startDate) ||
        editedDueDate.value !== formatDate(todo.value?.dueDate));
  };

  const onStartDateChange = async newVal => {
    if (newVal > dueDateRaw.value) {
      toast.warning('시작 날짜는 마감 날짜보다 빠를 수 없습니다.', {
        toastClassName: 'Vue-Toastification__toast--info',
      });
      await nextTick();
      startDateRaw.value = originalStartDate.value;
      return;
    }
    originalStartDate.value = new Date(newVal);
    editedStartDate.value = formatDate(newVal);
    checkIfModified();
  };

  const onDueDateChange = async newVal => {
    if (newVal < startDateRaw.value) {
      toast.warning('마감 날짜는 시작 날짜보다 늦어야 합니다.', {
        toastClassName: 'Vue-Toastification__toast--info',
      });
      await nextTick();
      dueDateRaw.value = originalDueDate.value;
      return;
    }
    originalDueDate.value = new Date(newVal);
    editedDueDate.value = formatDate(newVal);
    checkIfModified();
  };

  const submitEdit = () => {
    if (todo.value) {
      emit('edit', {
        todoId: todo.value.todoId,
        newContent: editedContent.value,
        newStartDate: new Date(startDateRaw.value),
        newDueDate: new Date(dueDateRaw.value),
      });
    }
  };
  watch(
    () => [props.todoId, visible.value],
    async ([id, isVisible]) => {
      if (id && isVisible) {
        try {
          const res = await fetchTodoDetail(id);
          todo.value = res.data;

          startDateRaw.value = new Date(res.data.startDate);
          dueDateRaw.value = new Date(res.data.dueDate);

          originalStartDate.value = new Date(res.data.startDate);
          originalDueDate.value = new Date(res.data.dueDate);

          editedStartDate.value = formatDate(startDateRaw.value);
          editedDueDate.value = formatDate(dueDateRaw.value);
          editedContent.value = res.data.content;

          isModified.value = false;
        } catch (err) {
          console.error('❌ 상세 조회 실패:', err);
        }
      }
    },
    { immediate: true }
  );
</script>

<template>
  <BaseModal v-model="visible" title="TODO 상세">
    <div v-if="todo" class="info-grid">
      <!-- 작성자 / 팀명 -->
      <div class="form-group">
        <label class="form-label">작성자</label>
        <input type="text" class="input" :value="todo.userName" readonly />
      </div>
      <div class="form-group">
        <label class="form-label">팀명</label>
        <input type="text" class="input" :value="todo.teamName" readonly />
      </div>

      <!-- 시작 날짜 -->
      <div class="form-group">
        <label class="form-label">시작 날짜</label>
        <DatePicker
          v-model="startDateRaw"
          mode="dateTime"
          is24hr
          :masks="{ input: 'YYYY-MM-DD HH:mm' }"
          :popover="{ placement: 'bottom-start' }"
          color="primary"
          @update:model-value="onStartDateChange"
        >
          <template #default="{ inputValue, inputEvents }">
            <input class="input dp-sm" :value="inputValue" readonly v-on="inputEvents" />
          </template>
        </DatePicker>
      </div>

      <!-- 마감 날짜 -->
      <div class="form-group">
        <label class="form-label">마감 날짜</label>
        <DatePicker
          v-model="dueDateRaw"
          mode="dateTime"
          is24hr
          :masks="{ input: 'YYYY-MM-DD HH:mm' }"
          :popover="{ placement: 'bottom-start' }"
          color="primary"
          @update:model-value="onDueDateChange"
        >
          <template #default="{ inputValue, inputEvents }">
            <input class="input dp-sm" :value="inputValue" readonly v-on="inputEvents" />
          </template>
        </DatePicker>
      </div>

      <!-- 할 일 -->
      <div class="form-group full">
        <label class="form-label">할 일</label>
        <textarea v-model="editedContent" class="input" @input="checkIfModified" />
      </div>
    </div>
    <div v-else class="text-center p-3">불러오는 중...</div>

    <template #footer>
      <BaseButton
        type="secondary"
        :disabled="!isModified"
        class="!cursor-default"
        @click="showConfirmEdit = true"
      >
        수정
      </BaseButton>
      <BaseButton type="error" @click="showConfirmDelete = true"> 삭제 </BaseButton>
      <BaseButton type="primary" @click="visible = false">닫기</BaseButton>
    </template>
  </BaseModal>
  <!-- 수정 확인 모달 -->
  <BaseModal v-model="showConfirmEdit" title="수정 확인">
    <div class="p-4">정말 수정하시겠습니까?</div>
    <template #footer>
      <BaseButton type="error" @click="showConfirmEdit = false">아니오</BaseButton>
      <BaseButton type="primary" @click="handleEditConfirm">예</BaseButton>
    </template>
  </BaseModal>

  <!-- 삭제 확인 모달 -->
  <BaseModal v-model="showConfirmDelete" title="삭제 확인">
    <div class="p-4 text-red-600">정말 삭제하시겠습니까?</div>
    <template #footer>
      <BaseButton type="error" @click="showConfirmDelete = false">아니오</BaseButton>
      <BaseButton type="primary" @click="handleDeleteConfirm">예</BaseButton>
    </template>
  </BaseModal>
</template>

<style>
  button[disabled] {
    background-color: #ccc !important;
    color: #666 !important;
    cursor: not-allowed !important;
  }
  .info-grid {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 12px;
  }
  .form-group.full {
    grid-column: span 2;
  }
  .dp-sm {
    font-size: 12px;
    padding: 6px 10px;
    height: 34px;
  }

  /* v-calendar 달력 내부 스타일 */
  :root {
    --vc-accent-500: var(--color-primary-main);
    --vc-accent-600: var(--color-primary-400);
    --vc-white: var(--color-neutral-white);
  }

  .vc-container {
    font-size: 12px !important;
  }

  .vc-nav-title,
  .vc-nav-arrow,
  .vc-day {
    font-size: 12px !important;
  }
</style>
