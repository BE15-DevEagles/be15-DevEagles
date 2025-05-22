<template>
  <div class="p-4">
    <h1 class="font-section-title mb-6">환영합니다!</h1>
    <div class="bg-white rounded-lg shadow-drop p-6 max-w-3xl">
      <h2 class="font-section-title text-[var(--color-primary-main)] mb-4">
        DevEagles 협업 플랫폼
      </h2>
      <p class="font-body mb-4">
        이 플랫폼은 팀원들 간의 효율적인 협업을 위해 설계되었습니다. 채팅, 일정 관리, 작업 기록 등
        다양한 기능을 제공합니다.
      </p>
    </div>
  </div>

  <!-- 테스트용 공통 컴포넌트 예제 -->
  <div class="p-4 mt-8 bg-white rounded-lg shadow-drop">
    <BaseButton @click="showModal = true">모달 열기</BaseButton>

    <BaseModal v-model="showModal" title="사용자 정보">
      <form @submit.prevent="submitForm">
        <BaseInput v-model="username" label="사용자명" />
        <BaseInput v-model="email" label="이메일" type="email" :error="emailError" />
      </form>
      <template #footer>
        <BaseButton type="secondary" @click="showModal = false">취소</BaseButton>
        <BaseButton type="primary" @click="submitForm">저장</BaseButton>
      </template>
    </BaseModal>

    <BasePagination v-model:current-page="currentPage" :total-pages="10" />
  </div>

  <!-- BaseForm.vue (BaseInput) 예제 -->
  <div class="p-4 mt-8 bg-white rounded-lg shadow-drop">
    <h2 class="font-section-title mb-4">폼 요소 (BaseInput) 테스트</h2>
    <form @submit.prevent="submitFullForm">
      <BaseInput
        v-model="form.textValue"
        label="이름 (Text)"
        type="text"
        placeholder="이름을 입력하세요"
      />

      <BaseInput
        v-model="form.emailValue"
        label="이메일 (Email)"
        type="email"
        placeholder="이메일 주소를 입력하세요"
        :error="form.emailError"
      />

      <BaseInput
        v-model="form.selectedValue"
        label="선호하는 과일 (Select)"
        type="select"
        placeholder="과일을 선택해주세요"
        :options="form.fruitOptions"
        helper="가장 좋아하는 과일을 선택하세요."
      />

      <BaseInput
        v-model="form.textareaValue"
        label="자기소개 (Textarea)"
        type="textarea"
        :rows="4"
        placeholder="자신에 대해 자유롭게 작성해주세요."
      />

      <BaseInput
        v-model="form.checkboxValues"
        label="좋아하는 취미 (Checkbox - 다중 선택)"
        type="checkbox"
        :options="form.hobbyOptions"
      />

      <BaseInput
        v-model="form.radioValue"
        label="성별 (Radio)"
        type="radio"
        name="gender"
        :options="form.genderOptions"
      />

      <div class="mt-6">
        <BaseButton type="primary" @click="submitFullForm">전체 폼 제출</BaseButton>
      </div>
    </form>

    <div v-if="form.submittedData" class="mt-6 p-4 border border-gray-200 rounded-md bg-gray-50">
      <h3 class="font-section-inner mb-2">제출된 데이터:</h3>
      <pre class="text-sm whitespace-pre-wrap">{{
        JSON.stringify(form.submittedData, null, 2)
      }}</pre>
    </div>
  </div>
</template>

<script setup>
  import { ref, reactive } from 'vue';
  import BaseModal from '@/components/common/components/BaseModal.vue';
  import BaseButton from '@/components/common/components/BaseButton.vue';
  import BaseInput from '@/components/common/components/BaseForm.vue';
  import BasePagination from '@/components/common/components/Pagaination.vue';

  const showModal = ref(false);
  const username = ref('');
  const email = ref('');
  const emailError = ref('');
  const currentPage = ref(1);

  function submitForm() {
    if (!email.value.includes('@')) {
      emailError.value = '이메일 형식이 올바르지 않습니다.';
      return;
    }
    emailError.value = '';
    showModal.value = false;
  }

  const form = reactive({
    textValue: '',
    emailValue: '',
    emailError: '',
    selectedValue: '',
    textareaValue: '',
    checkboxValues: [],
    radioValue: '',
    fruitOptions: [
      { value: 'apple', text: '사과' },
      { value: 'banana', text: '바나나' },
      { value: 'orange', text: '오렌지' },
      { value: 'grape', text: '포도' },
    ],
    hobbyOptions: [
      { value: 'reading', text: '독서' },
      { value: 'sports', text: '운동' },
      { value: 'music', text: '음악 감상' },
      { value: 'travel', text: '여행' },
    ],
    genderOptions: [
      { value: 'male', text: '남성' },
      { value: 'female', text: '여성' },
      { value: 'other', text: '기타' },
    ],
    submittedData: null,
  });

  function submitFullForm() {
    form.emailError = '';
    if (form.emailValue && !form.emailValue.includes('@')) {
      form.emailError = '폼 내부 이메일 형식이 올바르지 않습니다.';
      return;
    }
    form.submittedData = {
      name: form.textValue,
      email: form.emailValue,
      favoriteFruit: form.selectedValue,
      introduction: form.textareaValue,
      hobbies: form.checkboxValues,
      gender: form.radioValue,
    };
    console.log('전체 폼 제출됨:', form.submittedData);
  }
</script>
