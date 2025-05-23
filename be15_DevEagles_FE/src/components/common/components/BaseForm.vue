<template>
  <div class="form-group">
    <label v-if="label && type !== 'checkbox' && type !== 'radio'" :for="id" class="form-label">{{
      label
    }}</label>

    <!-- Text, Email, Password, Number, Date, etc. -->
    <input
      v-if="type !== 'select' && type !== 'textarea' && type !== 'checkbox' && type !== 'radio'"
      :id="id"
      :type="type"
      class="input"
      :class="{ 'input-error': error }"
      :value="modelValue"
      :placeholder="placeholder"
      v-bind="$attrs"
      @input="$emit('update:modelValue', $event.target.value)"
    />

    <!-- Select (Dropdown) -->
    <select
      v-if="type === 'select'"
      :id="id"
      class="input"
      :class="{ 'input-error': error }"
      :value="modelValue"
      v-bind="$attrs"
      @change="$emit('update:modelValue', $event.target.value)"
    >
      <option v-if="placeholder" value="" disabled selected>{{ placeholder }}</option>
      <option v-for="option in options" :key="option.value" :value="option.value">
        {{ option.text }}
      </option>
    </select>

    <!-- Textarea -->
    <textarea
      v-if="type === 'textarea'"
      :id="id"
      class="input"
      :class="{ 'input-error': error }"
      :value="modelValue"
      v-bind="$attrs"
      :rows="rows"
      @input="$emit('update:modelValue', $event.target.value)"
    ></textarea>

    <!-- Checkbox Group -->
    <div v-if="type === 'checkbox'" class="checkbox-group">
      <label v-if="label" class="form-label group-label">{{ label }}</label>
      <div v-for="option in options" :key="option.value" class="checkbox">
        <input
          :id="`${id}-${option.value}`"
          type="checkbox"
          :value="option.value"
          :checked="Array.isArray(modelValue) && modelValue.includes(option.value)"
          v-bind="$attrs"
          @change="handleCheckboxChange(option.value)"
        />
        <label :for="`${id}-${option.value}`">{{ option.text }}</label>
      </div>
    </div>

    <!-- Radio Group -->
    <div v-if="type === 'radio'" class="radio-group">
      <label v-if="label" class="form-label group-label">{{ label }}</label>
      <div v-for="option in options" :key="option.value" class="radio">
        <input
          :id="`${id}-${option.value}`"
          type="radio"
          :name="name || id"
          :value="option.value"
          :checked="modelValue === option.value"
          v-bind="$attrs"
          @change="$emit('update:modelValue', option.value)"
        />
        <label :for="`${id}-${option.value}`">{{ option.text }}</label>
      </div>
    </div>

    <div v-if="error" class="form-error">{{ error }}</div>
    <div v-if="helper && !error" class="form-helper">{{ helper }}</div>
  </div>
</template>

<script>
  export default {
    name: 'BaseInput',
    inheritAttrs: false,
    props: {
      id: {
        type: String,
        default() {
          return `input-${Math.random().toString(36).substr(2, 9)}`;
        },
      },
      label: {
        type: String,
        default: '',
      },
      modelValue: {
        type: [String, Number, Array, Boolean],
        default: '',
      },
      type: {
        type: String,
        default: 'text', // text, email, password, number, date, select, textarea, checkbox, radio
      },
      error: {
        type: String,
        default: '',
      },
      helper: {
        type: String,
        default: '',
      },
      options: {
        type: Array,
        default: () => [],
      },
      placeholder: {
        type: String,
        default: '',
      },
      rows: {
        type: [String, Number],
        default: 3,
      },
      name: {
        type: String,
        default: '',
      },
    },
    emits: ['update:modelValue'],
    methods: {
      handleCheckboxChange(value) {
        if (Array.isArray(this.modelValue)) {
          const newValue = [...this.modelValue];
          const index = newValue.indexOf(value);
          if (index === -1) {
            newValue.push(value);
          } else {
            newValue.splice(index, 1);
          }
          this.$emit('update:modelValue', newValue);
        } else {
          this.$emit('update:modelValue', [value]);
        }
      },
    },
  };
</script>
