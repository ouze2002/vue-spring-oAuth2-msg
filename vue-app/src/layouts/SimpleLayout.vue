<template>
  <div class="simple-layout">
    <router-view />
  </div>
</template>

<style scoped>
.simple-layout {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
  overflow: hidden;
}
</style>

<script setup>
// This layout has no specific script logic
</script>
