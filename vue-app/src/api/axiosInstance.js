import axios from 'axios';
import Cookies from 'js-cookie';
import { jwtDecode } from 'jwt-decode';

const axiosInstance = axios.create({
  baseURL: import.meta.env.VITE_API_URL,
  withCredentials: true, // 쿠키도 함께 전송 (refresh token용)
});

// 토큰에서 만료 시간을 추출하여 쿠키 expires 계산
const calculateCookieExpires = (token) => {
  // try {
  //   const decoded = jwtDecode(token);
  //   const exp = decoded.exp; // Unix timestamp
  //   const now = Math.floor(Date.now() / 1000);
  //   const remainingSeconds = exp - now;
  //   const remainingDays = remainingSeconds / (24 * 60 * 60);
    
  //   // 최소 1분, 최대 토큰 만료 시간까지
  //   return Math.max(remainingDays, 1 / 1440); // 1분 = 1/1440일
  // } catch (error) {
  //   console.error('토큰 만료 시간 계산 실패:', error);
  //   return 0.021; // 기본값 30분
  // }
  return undefined; // expires를 undefined로 반환하면 세션 쿠키가 됨
};

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
          console.log('🔄 리프레시 요청 시작:', `${import.meta.env.VITE_API_URL}/auth/refresh`);
          const res = await axios.post(
            `${import.meta.env.VITE_API_URL}/auth/refresh`,
            {},
            { withCredentials: true }
          );
          console.log('✅ 리프레시 응답 성공:', res.data);
          const newAccessToken = res.data.accessToken;

          // 1. 쿠키에 저장 (토큰 만료 시간에 맞춰 동적 설정)
          const expires = calculateCookieExpires(newAccessToken);
          Cookies.set("accessToken", newAccessToken, { expires });
          console.log('Interceptor: New accessToken set in cookie.');

          // 2. Authorization 헤더 갱신
          originalRequest.headers.Authorization = `Bearer ${newAccessToken}`;

          console.log("Interceptor: refreshToken 요청 성공, 재요청 진행.");

          // 3. 재요청
          return axiosInstance(originalRequest);
        } catch (refreshError) {
          console.error("❌ Interceptor: Refresh 실패:", refreshError);
          console.error("❌ 리프레시 에러 상세:", refreshError.response?.data);
          console.error("❌ 리프레시 상태 코드:", refreshError.response?.status);
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
export { calculateCookieExpires };
