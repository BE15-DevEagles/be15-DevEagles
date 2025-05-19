export default {
  root: true,
  env: {
    browser: true,
    node: true,
    es2022: true,
  },
  extends: ["eslint:recommended", "plugin:vue/vue3-recommended"],
  parserOptions: {
    ecmaVersion: 2022,
    sourceType: "module",
  },
  rules: {
    "vue/multi-word-component-names": "off",
    "vue/html-self-closing": ["warn", {
      html: { void: "always", normal: "always", component: "always" }
    }],
    "vue/max-attributes-per-line": ["warn", {
      singleline: 3,
      multiline: 1
    }],
    "no-console": process.env.NODE_ENV === "production" ? "warn" : "off",
    "no-debugger": process.env.NODE_ENV === "production" ? "warn" : "off",
    "no-unused-vars": "warn",
    "semi": ["warn", "always"],
    "quotes": ["warn", "single"]
  },
};
