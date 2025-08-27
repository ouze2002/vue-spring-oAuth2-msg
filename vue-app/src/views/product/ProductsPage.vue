<template>
  <div class="products-container">
    <h2>{{ $t('products.title') }}</h2>

    <div class="search-controls">
      <input 
        type="text" 
        v-model="searchInput" 
        :placeholder="$t('products.search.placeholder')" 
      />
      <button @click="applyFilter">{{ $t('products.search.button') }}</button>
      <button @click="clearFilter">{{ $t('products.search.clear') }}</button>
    </div>

    <!-- ✅ 테이블은 이제 커스텀 훅에서 제공하는 ref로 직접 렌더링 -->
    <div ref="tableContainer" class="data-table-container"></div>
  </div>
</template>

<style src="@/styles/products-page.css"></style>

<script setup>
import { ref, computed } from 'vue';
import { useI18n } from 'vue-i18n';
import { useTabulatorTable } from '@/composables/useTabulatorTable.js';

const { t } = useI18n();
const searchInput = ref('');
const searchQuery = ref('');

const apiUrl = computed(() => `${import.meta.env.VITE_API_URL}/items`);

const columns = computed(() => [
  { title: t('products.table.id'), field: "id", width: 80 },
  { title: t('products.table.name'), field: "name" },
  { title: t('products.table.description'), field: "description" },
]);

// ✅ 커스텀 훅 사용
const { tableContainer, refresh } = useTabulatorTable({
  columns,
  dataUrl: apiUrl.value,
  searchQuery,
  searchFields: ['name', 'description'], // 반드시 지정
  onRowClick: (rowData) => {
    // console.log('Row clicked:', rowData);
  },
  onDataLoaded: (data) => {
    // console.log('Data loaded:', data);
  },
  onError: (error) => {
    // console.error('Error loading data:', error);
  }
});

// 검색 실행
const applyFilter = () => {
  searchQuery.value = searchInput.value;
  refresh();
};

// 검색 초기화
const clearFilter = () => {
  searchQuery.value = '';
  refresh();
};
</script>
