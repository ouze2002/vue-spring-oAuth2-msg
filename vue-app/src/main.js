import { createApp } from 'vue';
import App from './App.vue';
import router, { setupDynamicRoutes } from './router'; // setupDynamicRoutes 임포트
import pinia from './store';
import i18n from './i18n';
import { useUserStore } from './store/user';
import axiosInstance, { setupInterceptors } from './api/axiosInstance';
import './style.css';

async function initializeApp() {
  const app = createApp(App);

  app.use(pinia);
  app.use(i18n);

  // 인터셉터를 먼저 설정
  const userStore = useUserStore();
  app.config.globalProperties.$axios = axiosInstance;
  setupInterceptors(userStore, router);

  // 인터셉터 설정 후 동적 라우트 설정
  try {
    await setupDynamicRoutes();
  } catch (error) {
    console.error('Dynamic routes setup failed:', error);
    // 에러가 발생해도 앱은 계속 실행 (기본 라우트들은 동작)
  }

  // 동적 라우트 설정 완료 후 라우터 등록
  app.use(router);

  // 모든 설정이 완료된 후 앱을 마운트합니다.
  app.mount('#app');
}

initializeApp();
