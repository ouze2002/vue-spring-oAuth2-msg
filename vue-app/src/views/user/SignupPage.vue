<template>
  <div class="auth-container">
    <h2>{{ $t('signup.title') }}</h2>
    <form @submit.prevent="signup">
      <input type="text" :placeholder="$t('signup.usernamePlaceholder')" v-model="username" required />
      <input type="password" :placeholder="$t('signup.passwordPlaceholder')" v-model="password" required />
      <input type="password" :placeholder="$t('signup.confirmPasswordPlaceholder')" v-model="confirmPassword" required />
      <button type="submit">{{ $t('signup.signupButton') }}</button>
    </form>
     <p>{{ $t('signup.loginPrompt') }} <router-link to="/login">{{ $t('signup.loginLink') }}</router-link></p>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import { useI18n } from 'vue-i18n';
import { useRouter } from 'vue-router';
import axiosInstance from '@/api/axiosInstance';

const { t } = useI18n();
const username = ref('');
const password = ref('');
const confirmPassword = ref('');
const router = useRouter();

const signup = async () => {
  if (password.value !== confirmPassword.value) {
    alert(t('signup.alert.passwordMismatch'));
    return;
  }
  try {
    await axiosInstance.post('/auth/signup', {
      username: username.value,
      password: password.value,
    });
    alert(t('signup.alert.success'));
    router.push('/login');
  } catch (err) {
    alert(t('signup.alert.failure', { error: err.response?.data || err.message }));
  }
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
  background-color: #28a745;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}
button:hover {
  background-color: #218838;
}
p {
  margin-top: 15px;
  text-align: center;
}
</style>

