<script setup>
import { ref } from 'vue'
import { useBeforeUnload } from '@/features/worklog/composables/useBeforeUnload.js'

// 기본 정보
const author = '홍길동'
const team = '프론트엔드팀'

// 반응형 데이터
const title = ref('')
const workLog = ref('')
const issues = ref('')
const nextPlan = ref('')
const isLoading = ref(false)
const hasUnsavedChanges = ref(false)

// 작성 감지
const markUnsaved = () => { hasUnsavedChanges.value = true }

// 페이지 이탈 경고
useBeforeUnload(() => hasUnsavedChanges.value)

// 취소
const onCancel = () => {
  title.value = ''
  workLog.value = ''
  issues.value = ''
  nextPlan.value = ''
  hasUnsavedChanges.value = false
  alert('작성이 초기화되었습니다.')
}

// 맞춤법 검사
const onSpellCheck = async () => {
  isLoading.value = true
  try {
    // 실제 맞춤법 검사 로직 연결 예정
    await new Promise(resolve => setTimeout(resolve, 1000))
    alert('맞춤법 검사 완료! (예시)')
  } catch (err) {
    alert('맞춤법 검사 실패')
  } finally {
    isLoading.value = false
  }
}

// AI 요약
const onAISummary = async () => {
  isLoading.value = true
  try {
    // 실제 AI 요약 로직 호출 예정
    await new Promise(resolve => setTimeout(resolve, 1000))
    title.value = '오늘 업무 요약 자동 생성됨'
    markUnsaved()
    alert('AI 요약으로 제목이 자동 생성되었습니다.')
  } catch (err) {
    alert('AI 요약 실패')
  } finally {
    isLoading.value = false
  }
}

// 제출
const onSubmit = () => {
  if (!workLog.value.trim()) {
    alert('업무 일지를 작성해주세요.')
    return
  }

  console.log({
    author,
    team,
    title: title.value,
    workLog: workLog.value,
    issues: issues.value,
    nextPlan: nextPlan.value,
  })

  hasUnsavedChanges.value = false
  alert('제출 완료!')
}
</script>

<template>
  <div class="worklog-form">
    <!-- 로딩 오버레이 -->
    <div v-if="isLoading" class="loading-overlay">로딩중...</div>

    <!-- 제목 입력 -->
    <div class="form-section">
      <label class="label">제목</label>
      <input
          v-model="title"
          type="text"
          class="input-area"
          placeholder="제목을 입력해주세요"
          @input="markUnsaved"
      />
    </div>

    <!-- 작성자 + 팀명 -->
    <div class="info-row">
      <div>
        <label class="label">작성자</label>
        <div>{{ author }}</div>
      </div>
      <div>
        <label class="label">팀명</label>
        <div>{{ team }}</div>
      </div>
    </div>

    <!-- 업무 일지 -->
    <div class="form-section">
      <label class="label">업무 일지 항목</label>
      <textarea
          v-model="workLog"
          class="input-area"
          rows="4"
          placeholder="오늘 수행한 업무를 작성해주세요"
          @input="markUnsaved"
      />
    </div>

    <!-- 특이사항 -->
    <div class="form-section">
      <label class="label">특이사항</label>
      <textarea
          v-model="issues"
          class="input-area"
          rows="3"
          placeholder="특이사항이 있다면 작성해주세요"
          @input="markUnsaved"
      />
      <div class="ai-summary-wrapper">
        <button @click="onAISummary" class="btn ai-summary">AI 요약</button>
      </div>
    </div>

    <!-- 익일 업무 계획 -->
    <div class="form-section">
      <label class="label">익일 업무 계획</label>
      <textarea
          v-model="nextPlan"
          class="input-area"
          rows="3"
          placeholder="익일 업무 계획을 작성해주세요"
          @input="markUnsaved"
      />
    </div>

    <!-- 버튼 영역 -->
    <div class="button-row">
      <button @click="onCancel" class="btn cancel">취소</button>
      <button @click="onSpellCheck" class="btn check">맞춤법 검사</button>
      <button @click="onSubmit" class="btn submit">제출</button>
    </div>
  </div>
</template>

<style scoped>
.worklog-form {
  max-width: 700px;
  margin: 0 auto;
  padding: 2rem;
  font-family: Arial, sans-serif;
  position: relative;
}

.label {
  display: block;
  font-weight: 600;
  margin-bottom: 0.5rem;
}

.input-area {
  width: 100%;
  padding: 0.75rem;
  border: 1px solid #ccc;
  border-radius: 6px;
  resize: vertical;
  font-size: 0.95rem;
  box-sizing: border-box;
}

.info-row {
  display: flex;
  gap: 2rem;
  margin-bottom: 1.5rem;
}

.form-section {
  margin-bottom: 1.5rem;
}

.ai-summary-wrapper {
  margin-top: 0.5rem;
  text-align: right;
}

.button-row {
  display: flex;
  justify-content: flex-end;
  gap: 0.75rem;
  margin-top: 2rem;
}

.btn {
  padding: 0.5rem 1rem;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-weight: 600;
  font-size: 0.95rem;
}

.btn.cancel {
  background-color: #e2e8f0;
}

.btn.cancel:hover {
  background-color: #cbd5e1;
}

.btn.check {
  background-color: #93c5fd;
}

.btn.check:hover {
  background-color: #60a5fa;
}

.btn.submit {
  background-color: #10b981;
  color: white;
}

.btn.submit:hover {
  background-color: #059669;
}

.btn.ai-summary {
  background-color: #fbbf24;
}

.btn.ai-summary:hover {
  background-color: #f59e0b;
}

.loading-overlay {
  position: absolute;
  inset: 0;
  background-color: rgba(255, 255, 255, 0.8);
  display: flex;
  justify-content: center;
  align-items: center;
  font-size: 1.5rem;
  font-weight: bold;
  z-index: 10;
}
</style>
