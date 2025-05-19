import js from '@eslint/js';
import eslintPluginVue from 'eslint-plugin-vue';
// vue-eslint-parser는 eslint-plugin-vue의 flat/recommended 설정에 의해 내부적으로 사용될 수 있지만,
// 명시적으로 이해를 돕기 위해 언급합니다. 별도 설치가 필요할 수 있습니다.
// import vueEslintParser from 'vue-eslint-parser';
import eslintConfigPrettier from 'eslint-config-prettier'; // Prettier와의 충돌 방지

const isProd = process.env.NODE_ENV === 'production';

export default [
  // 1. 기본적인 JavaScript 린팅 규칙
  js.configs.recommended,

  // 2. Vue.js 린팅 규칙 (eslint-plugin-vue의 flat-config 권장사항 사용)
  // 이 설정은 .vue 파일에 대한 파서(vue-eslint-parser), 플러그인 및 권장 규칙을 포함합니다.
  ...eslintPluginVue.configs['flat/recommended'],

  // 3. 전역 커스텀 규칙 및 언어 옵션
  // (기존 설정에서 .js와 .vue 파일 모두에 적용되던 공통 규칙 및 옵션)
  {
    languageOptions: {
      ecmaVersion: 2022,
      sourceType: 'module',
      globals: {
        process: 'readonly',
      },
    },
    rules: {
      'no-console': isProd ? 'warn' : 'off',
      'no-debugger': isProd ? 'warn' : 'off',
      'no-unused-vars': 'warn',
      semi: ['warn', 'always'],
      quotes: ['warn', 'single'],
    },
  },

  // 4. Vue 파일에 대한 추가적인 커스텀 규칙 및 오버라이드
  {
    files: ['**/*.vue'],
    rules: {
      // 기존에 사용하시던 Vue 관련 규칙들
      'vue/multi-word-component-names': 'off',
      'vue/html-self-closing': [
        'warn',
        {
          html: { void: 'always', normal: 'always', component: 'always' },
        },
      ],
      'vue/max-attributes-per-line': [
        'warn',
        {
          singleline: 3,
          multiline: 1,
        },
      ],
      // 기타 필요한 Vue 규칙 추가
    },
  },

  // 5. Prettier 설정 (ESLint 규칙과 충돌하는 부분을 비활성화, 반드시 배열의 마지막에 위치해야 함)
  eslintConfigPrettier,
];
