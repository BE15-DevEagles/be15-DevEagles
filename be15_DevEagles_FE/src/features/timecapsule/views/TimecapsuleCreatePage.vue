<template>
  <div class="timecapsule-center-wrapper">
    <TimecapsuleCreateForm />
  </div>
</template>

<script setup>
  import TimecapsuleCreateForm from '../components/TimecapsuleCreateForm.vue';
</script>

<style scoped>
  .timecapsule-center-wrapper {
    min-height: 80vh;
    display: flex;
    align-items: center;
    justify-content: center;
  }
</style>
