<template>
  <div class="auth-container">
    <h2>{{ $t('login.title') }}</h2>
    <form @submit.prevent="handleLogin">
      <input v-model="username" :placeholder="$t('login.usernamePlaceholder')" />
      <input type="password" v-model="password" :placeholder="$t('login.passwordPlaceholder')" />
      <button type="submit">{{ $t('login.loginButton') }}</button>
    </form>
    <div class="social-login">
      <button @click="handleKakaoLogin" class="kakao-btn">{{ $t('login.kakaoButton') }}</button>
      <button @click="handleGoogleLogin" class="google-btn">{{ $t('login.googleButton') }}</button>
    </div>
    <div class="signup-link">
      <p>{{ $t('login.signupPrompt') }} <router-link to="/signup">{{ $t('login.signupLink') }}</router-link></p>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import { useI18n } from 'vue-i18n';
import { useRouter } from 'vue-router';
import axiosInstance, { calculateCookieExpires } from '@/api/axiosInstance';
import Cookies from 'js-cookie';
import { useUserStore } from '@/store/user';

const { t } = useI18n();
const username = ref('');
const password = ref('');
const router = useRouter();
const userStore = useUserStore();

const handleLogin = async () => {
  try {
    const res = await axiosInstance.post(
      '/auth/login',
      { username: username.value, password: password.value },
      { withCredentials: true }
    );

    const expires = calculateCookieExpires(res.data.accessToken);
    Cookies.set('accessToken', res.data.accessToken, { expires, path: '/' });
    userStore.setUserFromToken(res.data.accessToken);
    
    // 대시보드로 이동
    window.location.href = "/dashboard";

  } catch (err) {
    alert(t('login.alert.failure', { error: err.response?.data || err.message }));
  }
};

const KAKAO_REST_API_KEY = '1128aad3e67e548c55fffaf671435b23';
const KAKAO_REDIRECT_URI = `${import.meta.env.VITE_APP_URL}/oauth2/redirect/kakao`;
const KAKAO_AUTH_LOGIN_URL = `https://kauth.kakao.com/oauth/authorize?client_id=${KAKAO_REST_API_KEY}&redirect_uri=${KAKAO_REDIRECT_URI}&response_type=code`;

const handleKakaoLogin = () => {
  window.location.href = KAKAO_AUTH_LOGIN_URL;
};

const GOOGLE_CLIENT_ID = '1006203654968-h1tva0ns91o9nq0dl4ujt6iffndde8ug.apps.googleusercontent.com';
const GOOGLE_REDIRECT_URI = `${import.meta.env.VITE_APP_URL}/oauth2/redirect/google`;
const GOOGLE_AUTH_URL = `https://accounts.google.com/o/oauth2/v2/auth?client_id=${GOOGLE_CLIENT_ID}&redirect_uri=${GOOGLE_REDIRECT_URI}&response_type=code&scope=openid%20email%20profile`;

const handleGoogleLogin = () => {
  window.location.href = GOOGLE_AUTH_URL;
};
</script>


<style scoped>
.auth-container {
  max-width: 400px;
  margin: 50px auto;
  padding: 20px;
  border: 1px solid #ccc;
  border-radius: 8px;
}
input {
  display: block;
  width: 100%;
  margin-bottom: 10px;
  padding: 10px;
  box-sizing: border-box;
}
button {
  width: 100%;
  padding: 10px;
  background-color: #007bff;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}
button:hover {
  background-color: #0056b3;
}
.social-login {
  margin-top: 20px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}
.kakao-btn {
  background-color: #FEE500;
  color: #391B1B;
}
.kakao-btn:hover {
  background-color: #f2d700;
}
.google-btn {
  background-color: #4285F4;
  color: white;
}
.google-btn:hover {
  background-color: #357ae8;
}
.signup-link {
  margin-top: 20px;
  text-align: center;
}
</style>
