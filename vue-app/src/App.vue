<template>
  <component :is="layoutComponent">
    <router-view v-slot="{ Component }">
      <transition name="fade" mode="out-in">
        <component :is="Component" />
      </transition>
    </router-view>
  </component>
  <!-- 알림 컨테이너는 notificationService에서 자동으로 생성됩니다 -->
</template>

<script setup>
import { computed, onBeforeUnmount, watch } from 'vue';
import { useRoute } from 'vue-router';
import { useUserStore } from './store/user';
import Cookies from 'js-cookie';
import { notificationService } from './services/notificationService';

// 레이아웃 컴포넌트 참조
import FullLayout from './layouts/FullLayout.vue';
import SimpleLayout from './layouts/SimpleLayout.vue';

const route = useRoute();
const userStore = useUserStore();

// 레이아웃 컴포넌트 매핑
const layoutComponents = {
  FullLayout,
  SimpleLayout,
  default: FullLayout // 기본 레이아웃
};

// 현재 라우트에 맞는 레이아웃 컴포넌트 결정
const layoutComponent = computed(() => {
  const layoutName = route.meta.layout || 'default';
  return layoutComponents[layoutName] || layoutComponents.default;
});

// 초기 로드 시 토큰 확인 및 사용자 정보 설정
const initAuth = async () => {
  const token = Cookies.get('accessToken');
  if (token) {
    userStore.setUserFromToken(token);
    await userStore.fetchUserProfile();
    console.log('🔑 초기 로드: 사용자 인증됨, SSE 연결 시도');
    notificationService.connect();
  } else {
    console.log('🔒 초기 로드: 로그인되지 않음');
  }
};

// 초기 인증 상태 설정
initAuth();

// 로그인 상태 변경 감지
watch(() => userStore.isLoggedIn, (newVal) => {
  if (newVal) {
    console.log('🔑 로그인 상태 변경: 로그인됨, SSE 연결 시도');
    notificationService.connect();
  } else {
    console.log('🚪 로그인 상태 변경: 로그아웃됨, SSE 연결 해제');
    notificationService.disconnect();
  }
});

// 컴포넌트가 언마운트되기 전에 정리
onBeforeUnmount(() => {
  notificationService.disconnect();
});
</script>

<style>
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

/* 알림 스타일 */
.notification-container {
  position: fixed;
  top: 80px; /* Navbar height + margin */
  right: 20px;
  z-index: 2000; /* 다른 요소들 위에 오도록 z-index 증가 */
  width: 350px;
  display: flex;
  flex-direction: column;
  align-items: flex-end;
}

.notification {
  display: flex;
  align-items: flex-start; /* 아이콘과 텍스트 상단 정렬 */
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.1);
  padding: 16px;
  margin-bottom: 16px;
  transition: all 0.4s cubic-bezier(0.25, 0.8, 0.25, 1);
  transform: translateX(120%); /* 시작 위치 (화면 밖) */
  opacity: 0;
  width: 100%;
  box-sizing: border-box;
  pointer-events: auto;
}

/* 알림 등장 애니메이션 */
.notification:not(.fade-out) {
  transform: translateX(0);
  opacity: 1;
}

.notification.fade-out {
  transform: translateX(120%);
  opacity: 0;
}

.notification-icon {
  font-size: 24px;
  margin-right: 16px;
  line-height: 1.2; /* 텍스트와의 수직 정렬 미세 조정 */
}

.notification-content {
  flex-grow: 1;
}

.notification h4 {
  margin: 0 0 4px 0;
  color: #1f2937;
  font-size: 16px;
  font-weight: 600;
}

.notification p {
  margin: 0;
  color: #4b5563;
  font-size: 14px;
}

.notification-close {
  background: none;
  border: none;
  color: #9ca3af;
  font-size: 20px;
  cursor: pointer;
  padding: 0 0 0 16px;
  line-height: 1;
}
.notification-close:hover {
  color: #1f2937;
}

/* 알림 타입별 색상 */
.notification.success .notification-icon {
  color: #34d399;
}

.notification.error .notification-icon {
  color: #f87171;
}

.notification.warning .notification-icon {
  color: #fbbf24;
}

.notification.info .notification-icon {
  color: #60a5fa;
}
</style>