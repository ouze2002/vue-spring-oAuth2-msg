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

  // 동적 라우트 설정을 기다립니다.
  await setupDynamicRoutes();

  // 라우터는 모든 동적 경로가 준비된 후에 사용합니다.
  app.use(router);

  // 인터셉터 설정
  const userStore = useUserStore();
  app.config.globalProperties.$axios = axiosInstance;
  setupInterceptors(userStore, router);

  // 모든 설정이 완료된 후 앱을 마운트합니다.
  app.mount('#app');
}

initializeApp();
