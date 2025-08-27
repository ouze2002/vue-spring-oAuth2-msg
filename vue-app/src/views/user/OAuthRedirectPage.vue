<template>
  <div class="oauth-redirect-page">
    <div class="loading-content">
      <div class="spinner"></div>
      <p>{{ $t('oauth.processing', { provider }) }}</p>
    </div>
  </div>
</template>

<script setup>
import { onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useI18n } from 'vue-i18n';
import axiosInstance from '@/api/axiosInstance';
import Cookies from 'js-cookie';
import { useUserStore } from '@/store/user';

const { t } = useI18n();
const props = defineProps({
  provider: String,
});

const route = useRoute();
const router = useRouter();
const userStore = useUserStore();

onMounted(async () => {
  const code = route.query.code;
  const provider = props.provider;

  console.log('OAuthRedirectPage - code:', code);
  console.log('OAuthRedirectPage - provider:', provider);

  if (code && provider) {
    try {
      const res = await axiosInstance.post(`/auth/${provider}`, { code }, {
        withCredentials: true,
      });

      const accessToken = res.data.accessToken;
      Cookies.set("accessToken", accessToken, { expires: 0.021 });
      userStore.setUserFromToken(accessToken);
      window.location.href = "/";
    } catch (err) {
      console.error(`${provider} 로그인 실패:`, err);
      alert(t('oauth.failure', { provider }));
      router.push("/login");
    }
  } else {
    console.error("OAuth 인증 코드 또는 제공자 정보가 없습니다.");
    router.push("/login");
  }
});
</script>

<style scoped>
.oauth-redirect-page {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  width: 100%;
  background-color: #f8f9fa;
}

.loading-content {
  text-align: center;
  color: #333;
}

.spinner {
  width: 40px;
  height: 40px;
  margin: 0 auto 15px;
  border: 4px solid #f3f3f3;
  border-top: 4px solid #3498db;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

p {
  font-size: 1.2rem;
  margin: 0;
}
</style>