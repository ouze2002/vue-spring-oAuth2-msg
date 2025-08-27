<template>
  <aside class="sidebar" :class="{ 'is-open': isOpen }">
    <ul class="menu-links">
      <menu-item
        v-for="menu in menus"
        :key="menu.id"
        :menu="menu"
      />
    </ul>
    
    <!-- SSE 연결 상태 표시 -->
    <div class="sse-status" :class="{ 'connected': isConnected, 'disconnected': !isConnected }">
      <span class="status-dot"></span>
      <span class="status-text">{{ sseStatusText }}</span>
    </div>
  </aside>
</template>

<script setup>
import { computed } from 'vue';
import { useI18n } from 'vue-i18n';
import { useUserStore } from '../store/user';
import MenuItem from './MenuItem.vue';

const { t } = useI18n();

defineProps({
  isOpen: Boolean,
});

const userStore = useUserStore();
const menus = computed(() => userStore.menus);

// App.vue에서 SSE 연결 상태를 관리하므로 항상 true로 설정
const isConnected = true;

const sseStatusText = computed(() => {
  return isConnected ? t('sidebar.sse.connected') : t('sidebar.sse.disconnected');
});
</script>

<style scoped>
.sidebar {
  width: 240px; /* 너비 약간 증가 */
  background-color: #f8f9fa; /* 밝은 회색 배경 */
  padding: 12px;
  height: 100vh;
  position: fixed;
  top: 60px; /* Navbar 높이 */
  left: 0;
  transition: transform 0.3s ease-in-out;
  z-index: 1000;
  border-right: 1px solid #dee2e6; /* 오른쪽 경계선 추가 */
  box-sizing: border-box;
}

.sidebar.is-open {
  transform: translateX(0);
}

@media (max-width: 768px) {
  .sidebar {
    transform: translateX(-100%); /* 모바일에서는 기본적으로 숨김 */
  }
}

@media (min-width: 769px) {
  .sidebar {
    transform: translateX(0); /* 데스크탑에서는 항상 보임 */
  }
}

.menu-links {
  list-style: none;
  padding: 0;
  margin: 0;
  margin-bottom: 20px;
}

/* SSE 상태 표시 스타일 */
.sse-status {
  position: fixed;
  bottom: 20px;
  left: 20px;
  display: flex;
  align-items: center;
  padding: 8px 12px;
  border-radius: 16px;
  font-size: 12px;
  background-color: #f8f9fa;
  box-shadow: 0 2px 5px rgba(0,0,0,0.1);
  transition: all 0.3s ease;
}

.status-dot {
  display: inline-block;
  width: 8px;
  height: 8px;
  border-radius: 50%;
  margin-right: 6px;
}

.connected .status-dot {
  background-color: #4caf50; /* 초록색: 연결됨 */
  box-shadow: 0 0 8px #4caf50;
}

.disconnected .status-dot {
  background-color: #f44336; /* 빨간색: 연결 끊김 */
  box-shadow: 0 0 8px #f44336;
}

.status-text {
  color: #333;
  font-weight: 500;
}

/* 모바일에서의 스타일 조정 */
@media (max-width: 768px) {
  .sse-status {
    left: 50%;
    transform: translateX(-50%);
    bottom: 10px;
  }
}
</style>
