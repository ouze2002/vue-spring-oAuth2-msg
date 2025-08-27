<template>
  <div>
    <Navbar @toggle-sidebar="toggleSidebar" />
    <Sidebar :is-open="isSidebarOpen" />
    <div v-if="isSidebarOpen && isMobile" class="overlay" @click="closeSidebar"></div>
    <main class="content-container">
      <router-view />
    </main>
  </div>
</template>

<script setup>
import { ref, watch, onMounted, onUnmounted } from 'vue';
import { useRoute } from 'vue-router';
import Navbar from '../components/Navbar.vue';
import Sidebar from '../components/Sidebar.vue';

const isSidebarOpen = ref(false);
const isMobile = ref(window.innerWidth <= 768);
const route = useRoute();

const toggleSidebar = () => {
  isSidebarOpen.value = !isSidebarOpen.value;
};

const closeSidebar = () => {
  isSidebarOpen.value = false;
};

// 라우트가 변경될 때마다 사이드바를 닫습니다.
watch(() => route.path, () => {
  closeSidebar();
});

// 창 크기 변경 감지
const handleResize = () => {
  isMobile.value = window.innerWidth <= 768;
  if (!isMobile.value) {
    isSidebarOpen.value = false; // PC 모드에서는 사이드바 강제 닫기
  }
};

onMounted(() => {
  window.addEventListener('resize', handleResize);
});

onUnmounted(() => {
  window.removeEventListener('resize', handleResize);
});
</script>

<style>
body {
  margin: 0;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
}

#app {
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  text-align: center;
  color: #2c3e50;
}

.content-container {
  padding-top: 60px; /* Navbar height */
  padding-left: 20px;
  padding-right: 20px;
  padding-bottom: 20px;
  min-height: calc(100vh - 60px); /* Ensure it takes full height below navbar */
  box-sizing: border-box; /* Include padding in height calculation */
  overflow-x: hidden; /* Prevent horizontal scroll */
  overflow-y: auto; /* 세로 스크롤 허용 */
}

.overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.5);
  z-index: 999; /* Sidebar is 1000, so overlay is below it */
}

@media (min-width: 769px) {
  .content-container {
    margin-left: 220px; /* Sidebar width + padding */
  }
}

/* No specific mobile styles for content-container here, as sidebar is hidden */
</style>