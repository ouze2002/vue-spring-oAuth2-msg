import { ref, onMounted, onUnmounted, watch, isRef } from 'vue';
import { TabulatorFull as Tabulator } from 'tabulator-tables';
import 'tabulator-tables/dist/css/tabulator_simple.min.css';
import '@/styles/data-table.css';
import axiosInstance from '@/api/axiosInstance';

export function useTabulatorTable(options) {
  const {
    columns,
    dataUrl,
    searchQuery = ref(''),
    searchFields, 
    pageSize = 10,
    pageSizes = [10, 20, 50, 100],
    onRowClick = () => {},
    onDataLoaded = () => {},
    onError = () => {}
  } = options;

  if (!Array.isArray(searchFields) || searchFields.length === 0) {
    throw new Error("useTabulatorTable: 'searchFields'는 최소 1개 이상의 필드를 가진 배열이어야 합니다.");
  }

  const tableContainer = ref(null);
  let tabulator = null;

  onMounted(() => {
    if (!tableContainer.value) return;

    const initialColumns = isRef(columns) ? columns.value : columns;

    tabulator = new Tabulator(tableContainer.value, {
      columns: initialColumns,
      layout: "fitColumns",
      pagination: true,
      paginationMode: "remote",
      paginationSize: pageSize,
      paginationSizeSelector: pageSizes,
      ajaxURL: dataUrl,
      sortMode: "remote",
      filterMode: "remote",
      
      ajaxRequestFunc: async function(url, config, params) {
        try {
          console.log('Tabulator ajaxRequestFunc called with:', { url, config, params });
          const response = await axiosInstance.get(url);
          console.log('Tabulator response:', response.data);
          return response.data;
        } catch (error) {
          console.error('Tabulator data load error:', error);
          throw error;
        }
      },
      ajaxURLGenerator: function (url, config, params) {
        const urlParams = new URLSearchParams();

        // 페이지네이션
        urlParams.append('page', Math.max(0, (params.page || 1) - 1));
        urlParams.append('size', params.size || pageSize);

        // 정렬
        if (params.sort?.length > 0) {
          params.sort.forEach(sorter => {
            urlParams.append('sort', `${sorter.field},${sorter.dir}`);
          });
        }

        // 검색
        if (searchQuery.value?.trim()) {
          searchFields.forEach(field => {
            urlParams.append(field, searchQuery.value);
          });
        }

        const finalUrl = `${dataUrl}?${urlParams.toString()}`;
        console.log('Tabulator ajaxURLGenerator generated URL:', finalUrl);
        return finalUrl;
      },

      ajaxResponse: function (url, params, response) {
        return {
          last_page: response.totalPages || 1,
          data: response.content || []
        };
      },

      rowClick: function (e, row) {
        onRowClick(row.getData());
      },

      ajaxError: function (xhr) {
        onError(xhr);
      }
    });

    tabulator.on('dataLoaded', data => {
      onDataLoaded(data);
    });
  });

  onUnmounted(() => {
    if (tabulator) {
      tabulator.destroy();
    }
  });

  watch(searchQuery, () => {
    if (tabulator) {
      tabulator.setData();
    }
  });

  // 컬럼 정의가 Ref인 경우 변경을 감지하여 테이블 업데이트
  if (isRef(columns)) {
    watch(columns, (newColumns) => {
      if (tabulator) {
        tabulator.setColumns(newColumns);
      }
    });
  }

  const refresh = () => tabulator?.setData();

  return {
    tableContainer,
    refresh
  };
}
