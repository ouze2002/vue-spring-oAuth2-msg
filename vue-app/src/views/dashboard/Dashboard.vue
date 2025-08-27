<template>
  <div class="dashboard-container">
    <h1>{{ $t('dashboard.title') }}</h1>
    <p>{{ $t('dashboard.welcome') }}</p>
    <div class="dashboard-widgets">
      <div class="widget">
        <h3>{{ $t('dashboard.widget1.title') }}</h3>
        <p>{{ $t('dashboard.widget1.content') }}</p>
      </div>
      <div class="widget">
        <h3>{{ $t('dashboard.widget2.title') }}</h3>
        <p>{{ $t('dashboard.widget2.content') }}</p>
      </div>
      <div class="widget">
        <h3>{{ $t('dashboard.widget3.title') }}</h3>
        <p>{{ $t('dashboard.widget3.content') }}</p>
      </div>
    </div>
  </div>
</template>

<script setup>
// 대시보드 관련 로직 (예: 데이터 fetching)을 여기에 추가할 수 있습니다.
</script>

<style scoped>
.dashboard-container {
  padding: 20px;
}

.dashboard-widgets {
  display: flex;
  gap: 20px;
  margin-top: 20px;
}

.widget {
  border: 1px solid #ccc;
  padding: 20px;
  border-radius: 8px;
  flex: 1;
}
</style>
