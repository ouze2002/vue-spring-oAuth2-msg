<template>
  <div class="not-found-container">
    <h1>404</h1>
    <p>페이지를 찾을 수 없습니다.</p>
    <router-link to="/">홈으로 돌아가기</router-link>
  </div>
</template>

<script setup>
// NotFound 컴포넌트는 현재 특별한 로직이 없으므로 script setup만 추가합니다.
</script>

<style scoped>
.not-found-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100vh;
  text-align: center;
}

h1 {
  font-size: 5em;
  margin-bottom: 0.5em;
}

p {
  font-size: 1.2em;
  margin-bottom: 1em;
}
</style>