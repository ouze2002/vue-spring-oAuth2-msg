<template>
  <nav class="navbar">
    <button class="hamburger" @click="toggleSidebar">&#9776;</button>
    <router-link to="/dashboard" class="navbar-logo">MyApp</router-link>

    <div class="navbar-right">
      <select v-model="currentLocale" @change="changeLanguage" class="language-selector">
        <option value="ko">한국어</option>
        <option value="en">English</option>
        <option value="ja">日本語</option>
      </select>

      <template v-if="isLoggedIn">
        <div class="notification-icon" @click="toggleNotifications">
          <i class="fas fa-bell"></i>
          <span v-if="unreadCount > 0" class="notification-badge">{{ unreadCount }}</span>
        </div>
        <div class="user-dropdown" ref="userDropdown">
          <button @click="toggleDropdown" class="dropdown-toggle">
            <i class="fas fa-user-circle"></i>
          </button>
          <transition name="fade">
            <div v-if="showDropdown" class="dropdown-menu">
              <div class="dropdown-header">{{ userName }}</div>
              <a @click="handleLogout" class="dropdown-item">{{ $t('navbar.logout') }}</a>
            </div>
          </transition>
        </div>
      </template>

      <template v-else>
        <router-link to="/login" class="auth-link">{{ $t('navbar.login') }}</router-link>
        <router-link to="/signup" class="auth-link">{{ $t('navbar.signup') }}</router-link>
      </template>
    </div>

    <transition name="slide-fade">
      <div v-if="showNotifications" ref="notificationPanel" class="notifications-panel">
        <div class="panel-header">
          <h3>{{ $t('navbar.notifications.title') }}</h3>
        </div>
        <div class="panel-content">
          <div v-if="notifications.length === 0" class="no-notifications">
            <i class="fas fa-check-circle"></i>
            <p>{{ $t('navbar.notifications.empty') }}</p>
          </div>
          <div v-else class="notification-list">
            <div 
              v-for="notification in notifications" 
              :key="notification.id"
              class="notification-item"
              :class="{ 'is-read': notification.read }"
              @click="markAsRead(notification)">
              <div class="item-icon">
                <i :class="getNotificationIcon(notification.type)"></i>
              </div>
              <div class="item-content">
                <p class="item-message">{{ notification.message }}</p>
                <small class="item-time">{{ formatTimeAgo(notification.createdAt) }}</small>
              </div>
            </div>
          </div>
        </div>
      </div>
    </transition>
  </nav>
</template>

<script setup>
import { computed, ref, onMounted, onUnmounted } from 'vue';
import { useUserStore } from '@/store/user';
import axiosInstance from '@/api/axiosInstance';
import { useRouter } from 'vue-router';
import { useI18n } from 'vue-i18n';
import { notificationService } from '@/services/notificationService';
import { formatDistanceToNow } from 'date-fns';
import { ko } from 'date-fns/locale';

const emit = defineEmits(['toggle-sidebar']);

const userStore = useUserStore();
const router = useRouter();
const i18n = useI18n();
const currentLocale = ref(i18n.locale.value);

const isLoggedIn = computed(() => userStore.isLoggedIn);
const userName = computed(() => userStore.username);

const showNotifications = ref(false);
const notifications = ref([]);
const notificationPanel = ref(null);
const unreadCount = ref(0);

const showDropdown = ref(false);
const userDropdown = ref(null);

const fetchNotifications = async () => {
  if (!userStore.isLoggedIn) return;
  try {
    const response = await axiosInstance.get('/notifications');
    console.log('알림 응답 데이터:', response.data);
    
    // 응답 데이터에서 content가 배열인지 확인
    const notificationsData = Array.isArray(response.data.content) 
      ? response.data.content 
      : [];
    
    // read 속성 사용 (백엔드 응답에 맞게 수정)
    notifications.value = notificationsData.map(notification => ({
      ...notification,
      read: Boolean(notification.read) // read 속성 사용
    }));
    
    unreadCount.value = notifications.value.filter(n => !n.read).length;
    console.log('처리된 알림 데이터:', notifications.value);
  } catch (error) {
    console.error('알림을 가져오는 데 실패했습니다:', error);
  }
};

const toggleNotifications = () => {
  showNotifications.value = !showNotifications.value;
  if (showNotifications.value) {
    fetchNotifications();
  }
};

const markAsRead = async (notification) => {
  if (notification.read) return;
  
  console.log('읽음 처리 시작 - 알림 ID:', notification.id);
  
  try {
    // 1. 서버에 읽음 처리 요청
    const response = await axiosInstance.patch(`/notifications/${notification.id}/read`);
    console.log('읽음 처리 응답:', response.data);
    
    // 2. 서버에서 반환된 데이터가 성공이면 상태 업데이트
    if (response.data && response.data.success) {
      // 3. 성공 시 서버에서 최신 데이터 다시 가져오기
      await fetchNotifications();
    } else {
      // 4. 서버 응답이 없거나 실패한 경우 로컬에서만 업데이트
      const updatedNotifications = notifications.value.map(n => 
        n.id === notification.id ? { ...n, read: true } : n
      );
      notifications.value = updatedNotifications;
      unreadCount.value = updatedNotifications.filter(n => !n.read).length;
    }
  } catch (error) {
    console.error('알림을 읽음 처리하는 데 실패했습니다:', error);
    
    // 5. 에러 발생 시 사용자에게 알림
    alert('알림 읽음 처리를 실패했습니다. 다시 시도해주세요.');
  }
};

const toggleDropdown = () => {
  showDropdown.value = !showDropdown.value;
};

const handleClickOutside = (event) => {
  if (notificationPanel.value && !notificationPanel.value.contains(event.target)) {
    const notificationIcon = document.querySelector('.notification-icon');
    if (notificationIcon && !notificationIcon.contains(event.target)) {
      showNotifications.value = false;
    }
  }

  if (userDropdown.value && !userDropdown.value.contains(event.target)) {
    showDropdown.value = false;
  }
};

onMounted(() => {
  document.addEventListener('click', handleClickOutside);
  fetchNotifications();
  notificationService.onNotification(notification => {
    fetchNotifications();
  });
});

const toggleSidebar = () => {
  emit('toggle-sidebar');
};

const handleLogout = async () => {
  showDropdown.value = false;
  try {
    await axiosInstance.post("/auth/logout");
  } catch (err) {
    console.error("서버 로그아웃 실패 (무시하고 진행):", err);
  }
  userStore.logout();
  window.location.href = '/login';
};

onUnmounted(() => {
  document.removeEventListener('click', handleClickOutside);
});

const formatTimeAgo = (date) => {
  if (!date) return '';
  return formatDistanceToNow(new Date(date), { addSuffix: true, locale: ko });
};

const getNotificationIcon = (type) => {
  switch (type) {
    case 'success': return 'fas fa-check-circle text-green-500';
    case 'error': return 'fas fa-exclamation-circle text-red-500';
    case 'warning': return 'fas fa-exclamation-triangle text-yellow-500';
    case 'info':
    default:
      return 'fas fa-info-circle text-blue-500';
  }
};

const changeLanguage = (event) => {
  const newLocale = event.target.value;
  i18n.locale.value = newLocale;
  localStorage.setItem('locale', newLocale);
};
</script>

<style scoped>
.navbar {
  display: flex;
  align-items: center;
  background-color: #333;
  color: white;
  padding: 0 20px;
  height: 60px;
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  z-index: 1001;
  box-sizing: border-box;
}

.navbar-logo {
  font-size: 1.5rem;
  font-weight: bold;
  color: white;
  text-decoration: none;
  margin-right: auto; /* Pushes everything else to the right */
}

.navbar-right {
  display: flex;
  align-items: center;
  margin-left: auto; /* Pushes itself to the right */
}

.language-selector {
  margin-right: 20px;
  background-color: #444;
  color: white;
  border: 1px solid #666;
  border-radius: 4px;
  padding: 5px;
}

.auth-link {
  color: white;
  text-decoration: none;
  margin-left: 20px;
  cursor: pointer;
  white-space: nowrap;
}

.hamburger {
  display: none; /* Hidden by default, shown in media query */
  font-size: 24px;
  background: none;
  border: none;
  color: white;
  cursor: pointer;
  margin-right: 15px;
}

.notification-icon {
  position: relative;
  cursor: pointer;
  margin-right: 20px;
}

.notification-badge {
  position: absolute;
  top: -5px;
  right: -10px;
  background-color: red;
  color: white;
  border-radius: 50%;
  padding: 2px 6px;
  font-size: 10px;
  font-weight: bold;
}

.user-dropdown {
  position: relative;
}

.dropdown-toggle {
  background: none;
  border: none;
  color: white;
  cursor: pointer;
  padding: 0;
}

.dropdown-toggle i {
  font-size: 24px;
}

.dropdown-menu {
  position: absolute;
  top: calc(100% + 10px);
  right: 0;
  background-color: white;
  border-radius: 4px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.15);
  z-index: 1010;
  min-width: 160px;
  overflow: hidden;
  text-align: center; /* 텍스트 가운데 정렬 */
}

.dropdown-header {
  padding: 12px 16px;
  font-weight: 600;
  color: #333;
  border-bottom: 1px solid #e9ecef;
  text-align: center; /* 헤더 텍스트 가운데 정렬 */
}

.dropdown-item {
  color: #333 !important;
  padding: 12px 16px;
  text-decoration: none;
  display: block;
  text-align: center; /* 텍스트 가운데 정렬 */
  margin: 0;
  white-space: nowrap;
  background-color: white !important;
  transition: background-color 0.2s;
  cursor: pointer; /* 마우스 오버 시 손가락 모양 */
}

.dropdown-item:hover {
  background-color: #f1f1f1;
}

.notifications-panel {
  position: fixed;
  top: 0;
  right: 0;
  width: 360px;
  height: 100%;
  background-color: #f8f9fa;
  box-shadow: -5px 0 15px rgba(0, 0, 0, 0.1);
  z-index: 1002;
  display: flex;
  flex-direction: column;
  color: #333;
}

.panel-header {
  padding: 16px 20px;
  border-bottom: 1px solid #e9ecef;
  flex-shrink: 0;
}

.panel-header h3 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
}

.panel-content {
  flex-grow: 1;
  overflow-y: auto;
}

.notification-list {
  padding: 8px 0;
}

.notification-item {
  display: flex;
  align-items: center;
  padding: 12px 20px;
  cursor: pointer;
  transition: background-color 0.2s ease;
}

.notification-item:hover {
  background-color: #e9ecef;
}

.notification-item.is-read {
  opacity: 0.6;
  background-color: #f8f9fa;
}

.notification-item.is-read .item-message {
  font-weight: normal;
  color: #6c757d;
}

.notification-item:not(.is-read) {
  background-color: #f1f8ff;
  border-left: 3px solid #4dabf7;
}

.item-icon {
  margin-right: 16px;
  font-size: 20px;
}

.item-content {
  flex-grow: 1;
}

.item-message {
  margin: 0;
  font-size: 14px;
  line-height: 1.4;
}

.item-time {
  font-size: 12px;
  color: #6c757d;
}

.no-notifications {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: #868e96;
  text-align: center;
}

.no-notifications i {
  font-size: 48px;
  margin-bottom: 16px;
  color: #ced4da;
}

/* Transition Styles */
.slide-fade-enter-active {
  transition: all 0.3s ease-out;
}

.slide-fade-leave-active {
  transition: all 0.3s cubic-bezier(1, 0.5, 0.8, 1);
}

.slide-fade-enter-from,
.slide-fade-leave-to {
  transform: translateX(100%);
  opacity: 0;
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease, transform 0.2s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
  transform: translateY(-10px);
}

/* Text color utilities */
.text-green-500 { color: #28a745; }
.text-red-500 { color: #dc3545; }
.text-yellow-500 { color: #ffc107; }
.text-blue-500 { color: #007bff; }

@media (max-width: 768px) {
  .hamburger {
    display: block;
  }
  .navbar-logo {
    margin-left: auto;
    margin-right: 0; /* Reset margin for centering */
    position: absolute;
    left: 50%;
    transform: translateX(-50%);
  }
  .navbar-right {
    /* The right items are already flex and will adapt */
  }
}
</style>