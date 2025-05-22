<template>
  <button
    :class="[
      'btn',
      `btn-${type}`,
      { 'btn-outline': outline },
      { 'btn-sm': size === 'sm' },
      { 'btn-lg': size === 'lg' },
    ]"
    :disabled="disabled"
    @click="$emit('click')"
  >
    <slot></slot>
  </button>
</template>

<script>
  export default {
    name: 'BaseButton',
    props: {
      type: {
        type: String,
        default: 'primary',
        validator: value =>
          ['primary', 'secondary', 'success', 'error', 'warning', 'info'].includes(value),
      },
      outline: {
        type: Boolean,
        default: false,
      },
      size: {
        type: String,
        default: '',
        validator: value => ['', 'sm', 'lg'].includes(value),
      },
      disabled: {
        type: Boolean,
        default: false,
      },
    },
    emits: ['click'],
  };
</script>
