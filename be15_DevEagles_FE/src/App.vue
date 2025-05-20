<script setup>

import {useRoute} from "vue-router";
import {computed} from "vue";

const router = useRoute()

const useLayout = computed(() => route.meta.layout !=='none');
</script>

<template>
  <div class="useLayout ? 'layout' : '' ">
    <router-view/>
  </div>
</template>

<style scoped>

</style>
