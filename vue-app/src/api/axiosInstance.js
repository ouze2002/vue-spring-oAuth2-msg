import axios from 'axios';
import Cookies from 'js-cookie';

const axiosInstance = axios.create({
  baseURL: import.meta.env.VITE_API_URL,
  withCredentials: true, // 쿠키도 함께 전송 (refresh token용)
});

// 요청 인터셉터
axiosInstance.interceptors.request.use(
  (config) => {
    const token = Cookies.get('accessToken');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// 인터셉터를 설정하는 함수를 내보냅니다.
export const setupInterceptors = (userStore, router) => {
  axiosInstance.interceptors.response.use(
    (res) => res,
    async (err) => {
      const originalRequest = err.config;

      console.log('Interceptor: Received response with status', err.response?.status);

      // accessToken 만료 + 이미 재시도 중이면 무한 루프 방지
      if (err.response?.status === 401 && !originalRequest._retry) {
        originalRequest._retry = true;
        console.log('Interceptor: 401 detected, attempting refresh...');
        try {
          const res = await axios.post(
            `${import.meta.env.VITE_API_URL}/auth/refresh`,
            {},
            { withCredentials: true }
          );
          const newAccessToken = res.data.accessToken;

          // 1. 쿠키에 저장
          Cookies.set("accessToken", newAccessToken, { expires: 0.021 }); // expires 추가
          console.log('Interceptor: New accessToken set in cookie.');

          // 2. Authorization 헤더 갱신
          originalRequest.headers.Authorization = `Bearer ${newAccessToken}`;

          console.log("Interceptor: refreshToken 요청 성공, 재요청 진행.");

          // 3. 재요청
          return axiosInstance(originalRequest);
        } catch (refreshError) {
          console.error("Interceptor: Refresh 실패:", refreshError);
          userStore.logout(); // 로그아웃 처리
          router.push('/login'); // 로그인 페이지로 리디렉션
          return Promise.reject(refreshError); // 에러 전파
        }
      } else if (err.response?.status === 401 && originalRequest._retry) {
        console.log('Interceptor: 401 detected, but already retried. Redirecting to login.');
        userStore.logout(); // 로그아웃 처리
        router.push('/login'); // 로그인 페이지로 리디렉션
      }

      return Promise.reject(err);
    }
  );
};

export default axiosInstance;
