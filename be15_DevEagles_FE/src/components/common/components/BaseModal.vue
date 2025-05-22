<template>
  <Teleport to="body">
    <transition name="modal-fade">
      <div v-if="modelValue" class="modal-backdrop" @click.self="closeModal">
        <div class="modal">
          <div class="modal-header">
            <h2 class="modal-title">{{ title }}</h2>
            <button class="modal-close" @click="closeModal">&times;</button>
          </div>
          <div class="modal-body">
            <slot></slot>
          </div>
          <div v-if="$slots.footer" class="modal-footer">
            <slot name="footer">
              <BaseButton @click="closeModal">닫기</BaseButton>
            </slot>
          </div>
        </div>
      </div>
    </transition>
  </Teleport>
</template>

<script>
  export default {
    name: 'BaseModal',
    props: {
      modelValue: {
        type: Boolean,
        required: true,
      },
      title: {
        type: String,
        default: '',
      },
    },
    emits: ['update:modelValue'],
    methods: {
      closeModal() {
        this.$emit('update:modelValue', false);
      },
    },
  };
</script>

<style>
  .modal-fade-enter-active,
  .modal-fade-leave-active {
    transition: opacity 0.3s ease;
  }

  .modal-fade-enter-from,
  .modal-fade-leave-to {
    opacity: 0;
  }
</style>
