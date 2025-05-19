import js from '@eslint/js';
import eslintPluginVue from 'eslint-plugin-vue';

const isProd = process.env.NODE_ENV === 'production';

export default [
  js.configs.recommended,
  {
    files: ['**/*.js', '**/*.vue'],
    plugins: {
      vue: eslintPluginVue,
    },
    languageOptions: {
      ecmaVersion: 2022,
      sourceType: 'module',
      globals: {
        process: 'readonly',
      },
    },
    rules: {
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
      'no-console': isProd ? 'warn' : 'off',
      'no-debugger': isProd ? 'warn' : 'off',
      'no-unused-vars': 'warn',
      semi: ['warn', 'always'],
      quotes: ['warn', 'single'],
    },
  },
  {
    files: ['**/*.vue'],
    rules: {
      ...eslintPluginVue.configs.recommended.rules,
    },
  },
];
