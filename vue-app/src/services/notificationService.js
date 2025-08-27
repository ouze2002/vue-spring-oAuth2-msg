import { ref } from 'vue';

class NotificationService {
  constructor() {
    this.eventSource = ref(null);
    this.notificationContainer = null;
    this.baseUrl = import.meta.env.VITE_API_URL || 'http://localhost:8081/api';
    this.notificationListeners = [];
    this.initContainer();
  }

  // 알림 컨테이너 초기화
  initContainer() {
    if (typeof window === 'undefined') return;
    if (!document.getElementById('notification-container')) {
      const container = document.createElement('div');
      container.id = 'notification-container';
      container.className = 'notification-container';
      document.body.appendChild(container);
    }
    this.notificationContainer = document.getElementById('notification-container');
  }

  // 아이콘 클래스 반환 헬퍼
  getNotificationIconClass(type) {
    switch (type) {
      case 'success': return 'fas fa-check-circle';
      case 'error': return 'fas fa-exclamation-circle';
      case 'warning': return 'fas fa-exclamation-triangle';
      case 'info':
      default:
        return 'fas fa-info-circle';
    }
  }

  // 알림 표시
  showNotification(notification) {
    if (!notification || !this.notificationContainer) return;

    const notificationEl = document.createElement('div');
    notificationEl.className = `notification ${notification.type || 'info'}`;

    const iconClass = this.getNotificationIconClass(notification.type);

    notificationEl.innerHTML = `
      <i class="notification-icon ${iconClass}"></i>
      <div class="notification-content">
        <h4>${notification.title || '알림'}</h4>
        <p>${notification.message || ''}</p>
      </div>
      <button class="notification-close">&times;</button>
    `;

    this.notificationContainer.appendChild(notificationEl);

    const close = () => {
      notificationEl.classList.add('fade-out');
      setTimeout(() => {
        if (this.notificationContainer.contains(notificationEl)) {
          this.notificationContainer.removeChild(notificationEl);
        }
      }, 300);
    };

    notificationEl.querySelector('.notification-close').addEventListener('click', close);
    setTimeout(close, 5000);
  }

  // SSE 연결
  connect() {
    if (this.eventSource.value) {
      console.log('🔁 이미 SSE가 연결되어 있습니다.');
      return;
    }

    const url = `${this.baseUrl}/notifications/subscribe`;
    console.log('🔌 SSE 연결을 시도합니다. URL:', url);
    
    try {
      this.disconnect();
      
      this.eventSource.value = new EventSource(url, { 
        withCredentials: true 
      });
      
      // 이벤트 리스너 설정
      this.setupEventListeners();
      
    } catch (error) {
      console.error('SSE 연결 중 오류 발생:', error);
      this.handleConnectionError();
    }
  }

  // 이벤트 리스너 설정
  setupEventListeners() {
    if (!this.eventSource.value) return;
    
    this.eventSource.value.addEventListener('open', this.handleOpen, { once: true });
    this.eventSource.value.addEventListener('message', this.handleMessage);
    this.eventSource.value.addEventListener('notification', this.handleNotification);
    this.eventSource.value.addEventListener('error', this.handleError);
  }

  handleOpen = (event) => {
    console.log('🎉 SSE 연결이 열렸습니다.', event);
  };

  handleMessage = (event) => {
    console.log('📨 SSE 메시지 수신:', event);
  };

  handleNotification = (event) => {
    console.log('🔔 알림 이벤트 수신:', event);
    try {
      let data = event.data;
      if (data) {
        try {
          const parsedData = JSON.parse(data);
          data = (typeof parsedData === 'string') ? JSON.parse(parsedData) : parsedData;
        } catch (e) {
          data = { message: data };
        }
        this.showNotification(data);
        this.notificationListeners.forEach(callback => callback(data));
      }
    } catch (error) {
      console.error('알림 처리 중 오류 발생:', error);
    }
  };

  handleError = (event) => {
    console.error('❌ SSE 연결 오류 발생:', event);
    if (this.eventSource.value) {
      console.log('현재 readyState:', this.eventSource.value.readyState, `(${['CONNECTING', 'OPEN', 'CLOSED'][this.eventSource.value.readyState || 0]})`);
    }
    this.handleConnectionError();
  };

  handleConnectionError() {
    setTimeout(() => {
      this.disconnect();
      this.connect();
    }, 5000);
  }

  disconnect() {
    if (this.eventSource.value) {
      console.log('🚪 SSE 연결을 해제합니다.');
      this.eventSource.value.close();
      this.eventSource.value = null;
    }
  }

  onNotification(callback) {
    this.notificationListeners.push(callback);
  }
}

export const notificationService = new NotificationService();
