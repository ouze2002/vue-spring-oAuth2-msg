<template>
  <div class="users-container">
    <h1>사용자 관리</h1>
    <div class="users-content">
      <div class="users-header">
        <div class="search-box">
          <input 
            type="text" 
            v-model="searchQuery" 
            placeholder="사용자 검색..."
            @keyup.enter="searchUsers"
          >
          <button @click="searchUsers">
            <i class="fas fa-search"></i>
          </button>
        </div>
        <button class="btn-primary" @click="openCreateModal">
          <i class="fas fa-plus"></i> 새 사용자 추가
        </button>
      </div>
      
      <div class="users-table-container">
        <table class="users-table">
          <thead>
            <tr>
              <th>ID</th>
              <th>이름</th>
              <th>이메일</th>
              <th>역할</th>
              <th>가입일</th>
              <th>상태</th>
              <th>액션</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="user in users" :key="user.id">
              <td>{{ user.id }}</td>
              <td>{{ user.name }}</td>
              <td>{{ user.email }}</td>
              <td>
                <span class="role-badge" :class="user.role.toLowerCase()">
                  {{ user.role }}
                </span>
              </td>
              <td>{{ formatDate(user.createdAt) }}</td>
              <td>
                <span class="status-badge" :class="{'active': user.isActive}">
                  {{ user.isActive ? '활성' : '비활성' }}
                </span>
              </td>
              <td class="actions">
                <button class="btn-icon" @click="editUser(user)">
                  <i class="fas fa-edit"></i>
                </button>
                <button class="btn-icon danger" @click="confirmDelete(user)">
                  <i class="fas fa-trash"></i>
                </button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
      
      <div class="pagination">
        <button 
          :disabled="currentPage === 1" 
          @click="changePage(currentPage - 1)"
        >
          이전
        </button>
        <span>페이지 {{ currentPage }} / {{ totalPages }}</span>
        <button 
          :disabled="currentPage >= totalPages" 
          @click="changePage(currentPage + 1)"
        >
          다음
        </button>
      </div>
    </div>
    
    <!-- 사용자 생성/수정 모달 -->
    <UserModal 
      v-if="showModal" 
      :user="selectedUser"
      @close="closeModal"
      @save="saveUser"
    />
    
    <!-- 삭제 확인 모달 -->
    <ConfirmModal
      v-if="showDeleteConfirm"
      title="사용자 삭제"
      message="정말로 이 사용자를 삭제하시겠습니까? 이 작업은 되돌릴 수 없습니다."
      @confirm="deleteUser"
      @cancel="showDeleteConfirm = false"
    />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import UserModal from '@/components/users/UserModal.vue';
import ConfirmModal from '@/components/common/ConfirmModal.vue';

// 상태
const users = ref([]);
const searchQuery = ref('');
const currentPage = ref(1);
const totalPages = ref(1);
const showModal = ref(false);
const showDeleteConfirm = ref(false);
const selectedUser = ref(null);
const userToDelete = ref(null);

// 라이프사이클 훅
onMounted(() => {
  fetchUsers();
});

// 메서드
const fetchUsers = async () => {
  try {
    // TODO: 실제 API 호출로 대체
    // const response = await axios.get('/api/users', {
    //   params: { 
    //     page: currentPage.value, 
    //     search: searchQuery.value 
    //   }
    // });
    // users.value = response.data.users;
    // totalPages.value = response.data.totalPages;
    
    // 임시 데이터
    users.value = Array(10).fill().map((_, i) => ({
      id: i + 1,
      name: `사용자${i + 1}`,
      email: `user${i + 1}@example.com`,
      role: ['관리자', '사용자', '매니저'][Math.floor(Math.random() * 3)],
      isActive: Math.random() > 0.3,
      createdAt: new Date(Date.now() - Math.floor(Math.random() * 1000 * 60 * 60 * 24 * 30)).toISOString()
    }));
    totalPages.value = 3;
    
  } catch (error) {
    console.error('사용자 목록을 불러오는 중 오류 발생:', error);
    console.log('사용자 목록을 불러오는 데 실패했습니다.');
  }
};

const searchUsers = () => {
  currentPage.value = 1;
  fetchUsers();
};

const changePage = (page) => {
  if (page < 1 || page > totalPages.value) return;
  currentPage.value = page;
  fetchUsers();
};

const openCreateModal = () => {
  selectedUser.value = null;
  showModal.value = true;
};

const editUser = (user) => {
  selectedUser.value = { ...user };
  showModal.value = true;
};

const closeModal = () => {
  showModal.value = false;
  selectedUser.value = null;
};

const saveUser = async (userData) => {
  try {
    if (userData.id) {
      // 수정
      // await axios.put(`/api/users/${userData.id}`, userData);
      console.log('사용자 정보가 수정되었습니다.');
    } else {
      // 생성
      // await axios.post('/api/users', userData);
      console.log('새 사용자가 추가되었습니다.');
    }
    closeModal();
    fetchUsers();
  } catch (error) {
    console.error('사용자 저장 중 오류 발생:', error);
    toast.error('사용자 저장에 실패했습니다.');
  }
};

const confirmDelete = (user) => {
  userToDelete.value = user;
  showDeleteConfirm.value = true;
};

const deleteUser = async () => {
  if (!userToDelete.value) return;
  
  try {
    // await axios.delete(`/api/users/${userToDelete.value.id}`);
    console.log('사용자가 삭제되었습니다.');
    fetchUsers();
  } catch (error) {
    console.error('사용자 삭제 중 오류 발생:', error);
    console.log('사용자 삭제에 실패했습니다.');
  } finally {
    showDeleteConfirm.value = false;
    userToDelete.value = null;
  }
};

const formatDate = (dateString) => {
  return new Date(dateString).toLocaleDateString('ko-KR', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit'
  });
};
</script>

<style scoped>
.users-container {
  padding: 24px;
  max-width: 1200px;
  margin: 0 auto;
}

h1 {
  font-size: 24px;
  margin-bottom: 24px;
  color: #333;
}

.users-content {
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  padding: 24px;
}

.users-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 20px;
  flex-wrap: wrap;
  gap: 16px;
}

.search-box {
  display: flex;
  align-items: center;
  background: #f5f5f5;
  border-radius: 4px;
  padding: 0 12px;
  flex: 1;
  max-width: 400px;
}

.search-box input {
  border: none;
  background: transparent;
  padding: 10px 0;
  flex: 1;
  outline: none;
}

.search-box button {
  background: none;
  border: none;
  color: #666;
  cursor: pointer;
  padding: 8px;
}

.btn-primary {
  background: #4a6cf7;
  color: white;
  border: none;
  border-radius: 4px;
  padding: 10px 16px;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 500;
  transition: background 0.2s;
}

.btn-primary:hover {
  background: #3a5ce4;
}

.users-table-container {
  overflow-x: auto;
  margin-bottom: 20px;
}

.users-table {
  width: 100%;
  border-collapse: collapse;
}

.users-table th,
.users-table td {
  padding: 12px 16px;
  text-align: left;
  border-bottom: 1px solid #eee;
}

.users-table th {
  background: #f9fafb;
  font-weight: 600;
  color: #4b5563;
  text-transform: uppercase;
  font-size: 12px;
  letter-spacing: 0.05em;
}

.users-table tbody tr:hover {
  background: #f9fafb;
}

.role-badge {
  display: inline-block;
  padding: 4px 8px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
}

.role-badge.관리자 {
  background: #e0f2fe;
  color: #0369a1;
}

.role-badge.사용자 {
  background: #dcfce7;
  color: #15803d;
}

.role-badge.매니저 {
  background: #fef3c7;
  color: #92400e;
}

.status-badge {
  display: inline-block;
  padding: 4px 8px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
  background: #e5e7eb;
  color: #4b5563;
}

.status-badge.active {
  background: #d1fae5;
  color: #065f46;
}

.actions {
  display: flex;
  gap: 8px;
}

.btn-icon {
  background: none;
  border: none;
  color: #6b7280;
  cursor: pointer;
  padding: 6px;
  border-radius: 4px;
  transition: all 0.2s;
}

.btn-icon:hover {
  background: #f3f4f6;
  color: #4b5563;
}

.btn-icon.danger {
  color: #ef4444;
}

.btn-icon.danger:hover {
  background: #fee2e2;
}

.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 16px;
  margin-top: 24px;
}

.pagination button {
  padding: 8px 16px;
  border: 1px solid #e5e7eb;
  background: white;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.2s;
}

.pagination button:not(:disabled):hover {
  background: #f3f4f6;
}

.pagination button:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

/* 반응형 스타일 */
@media (max-width: 768px) {
  .users-header {
    flex-direction: column;
  }
  
  .search-box {
    max-width: 100%;
  }
  
  .btn-primary {
    width: 100%;
    justify-content: center;
  }
}
</style>
