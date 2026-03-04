<template>
  <div class="admin-list-view">
    <div class="d-flex justify-content-between align-items-center mb-4">
      <h2 class="text-admin-primary fw-bold mb-0">管理員管理</h2>
      <button class="btn-admin-primary" @click="openAddModal">
        <i class="bi bi-person-plus me-2"></i>新增管理員
      </button>
    </div>

    <!-- Search Bar Section -->
    <div class="admin-card p-3 mb-4">
      <div class="row g-3 align-items-center">
        <!-- Position Filter -->
        <div class="col-auto">
          <select v-model="filterPosition" class="admin-form-select">
            <option value="all">全部職位</option>
            <option v-for="pos in positions" :key="pos" :value="pos">{{ pos }}</option>
          </select>
        </div>
        <!-- Status Filter -->
        <div class="col-auto">
          <select v-model="filterBanned" class="admin-form-select">
            <option value="all">全部狀態</option>
            <option value="false">正常</option>
            <option value="true">已停權</option>
          </select>
        </div>
        <div class="col">
          <div class="input-group">
            <span class="input-group-text bg-white border-end-0">
              <i class="bi bi-search text-muted"></i>
            </span>
            <input 
              v-model="searchQuery" 
              type="text" 
              class="form-control border-start-0 ps-0 admin-form-control-search" 
              placeholder="搜尋管理員帳號或姓名..."
            >
          </div>
        </div>
        <div class="col-auto">
          <button class="btn-admin-outline" @click="resetFilters">
            <i class="bi bi-arrow-counterclockwise me-2"></i>清除篩選
          </button>
        </div>
      </div>
    </div>

    <div class="admin-card overflow-hidden">
      <div class="table-responsive">
        <table class="admin-table">
          <thead>
            <tr>
              <th>ID</th>
              <th>姓名</th>
              <th>帳號</th>
              <th>職位</th>
              <th>信箱</th>
              <th>停權狀態</th>
              <th>編輯</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="admin in filteredAdmins" :key="admin.id">
              <td>{{ admin.id }}</td>
              <td>
                <span class="fw-medium">{{ admin.username }}</span>
              </td>
              <td>{{ admin.fullName }}</td>
              <td>
                <span class="position-badge">
                  {{ admin.position }}
                </span>
              </td>
              <td>{{ admin.email }}</td>
              <td>
                <span :class="['status-badge', admin.isBanned ? 'status-banned' : 'status-active']">
                  {{ admin.isBanned ? '已停權' : '正常' }}
                </span>
              </td>
              <td>
                <div class="action-btns">
                  <button class="btn-icon text-primary" title="編輯" @click="openEditModal(admin)">
                    <i class="bi bi-pencil-square"></i>
                  </button>
                </div>
              </td>
            </tr>
            <tr v-if="filteredAdmins.length === 0">
              <td colspan="6" class="text-center py-5 text-muted">
                沒有找到匹配的管理員
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { adminAPI } from '@/api/admin';
import Swal from 'sweetalert2';

// Search and Filter state
const searchQuery = ref('');
const filterPosition = ref('all');
const filterBanned = ref('all');

const admins = ref([]);

// Mapping for roles
const positionMap = {
  'SUPER_ADMIN': '總管理員',
  'HUMAN_RESOURCE': '人事',
  'CUSTOMER_SERVICE': '客服',
  'SHOP_MANAGER': '電商'
};

const roleToEnumMap = {
  '總管理員': 'SUPER_ADMIN',
  '人事': 'HUMAN_RESOURCE',
  '客服': 'CUSTOMER_SERVICE',
  '電商': 'SHOP_MANAGER'
};

// Fetch admins on mount
const fetchAdmins = async () => {
    try {
        const response = await adminAPI.getAll();
        // Since axios interceptor returns response.data, 'response' here is the JSON body (ApiResponse)
        if (response.data) {
            admins.value = response.data.map(admin => ({
                id: admin.id,
                username: admin.account,
                fullName: admin.name,
                email: admin.email,
                position: positionMap[admin.position] || admin.position,
                isBanned: !admin.enabled
            }));
        }
    } catch (error) {
        console.error('Failed to fetch admins:', error);
        Swal.fire({
            icon: 'error',
            title: '錯誤',
            text: '無法取得管理員列表'
        });
    }
};

onMounted(() => {
    fetchAdmins();
});

// Unique positions for filter
const positions = computed(() => {
  const posSet = new Set(admins.value.map(a => a.position));
  return Array.from(posSet);
});

const resetFilters = () => {
  searchQuery.value = '';
  filterPosition.value = 'all';
  filterBanned.value = 'all';
};

// Computed: Filtered admins
const filteredAdmins = computed(() => {
  let result = admins.value;

  // 1. Position filter
  if (filterPosition.value !== 'all') {
    result = result.filter(admin => admin.position === filterPosition.value);
  }

  // 2. Status filter
  if (filterBanned.value !== 'all') {
    const isBanned = filterBanned.value === 'true';
    result = result.filter(admin => admin.isBanned === isBanned);
  }

  // 3. Search Query filter
  if (!searchQuery.value.trim()) return result;
  
  const query = searchQuery.value.toLowerCase().trim();
  return result.filter(admin => {
    return (
      admin.username.toLowerCase().includes(query) ||
      admin.fullName.toLowerCase().includes(query) ||
      admin.id.toString().includes(query)
    );
  });
});

// 新增管理員彈窗
const openAddModal = async () => {
  const { value: formValues } = await Swal.fire({
    title: '新增管理員',
    html: `
      <div class="text-start">
        <div class="mb-3">
          <label class="form-label fw-bold">帳號</label>
          <input id="swal-input-account" class="form-control" placeholder="請輸入帳號">
        </div>
        <div class="mb-3">
          <label class="form-label fw-bold">姓名</label>
          <input id="swal-input-name" class="form-control" placeholder="請輸入姓名">
        </div>
        <div class="mb-3">
          <label class="form-label fw-bold">信箱</label>
          <input id="swal-input-email" type="email" class="form-control" placeholder="請輸入信箱">
        </div>
        <div class="mb-3">
          <label class="form-label fw-bold">職位</label>
          <select id="swal-input-position" class="form-select">
            <option value="" disabled selected>請選擇職位</option>
            <option value="SUPER_ADMIN">總管理員</option>
            <option value="HUMAN_RESOURCE">人事</option>
            <option value="CUSTOMER_SERVICE">客服</option>
            <option value="SHOP_MANAGER">電商</option>
          </select>
        </div>
      </div>
    `,
    focusConfirm: false,
    showCancelButton: true,
    confirmButtonText: '確定送出',
    cancelButtonText: '取消',
    confirmButtonColor: '#1e3c72',
    preConfirm: () => {
      const account = document.getElementById('swal-input-account').value;
      const name = document.getElementById('swal-input-name').value;
      const email = document.getElementById('swal-input-email').value;
      const position = document.getElementById('swal-input-position').value;
      if (!account || !name || !email || !position) {
        Swal.showValidationMessage('請填寫所有欄位');
        return false;
      }
      return { 
        account, // 帳號
        name,    // 姓名
        email,   // 信箱
        position // 職位
      };
    }
  });

  if (formValues) {
    try {
      // 傳送資料到後端
      console.log('正在傳送資料到後端...', formValues);
      
      const response = await adminAPI.register(formValues);
      
      console.log('後端回應:', response);

      // 請求成功後，重新取得列表
      await fetchAdmins();

      Swal.fire({
        title: '新增成功',
        html: `管理員 <strong>${formValues.name}</strong> 已成功加入<br><small class="text-muted">預設密碼與帳號相同</small>`,
        icon: 'success',
        confirmButtonColor: '#1e3c72',
        timer: 2000
      });
    } catch (error) {

      console.error('新增管理員時發生錯誤:', error);
      
      let errorMessage = error.response?.data?.message || '無法連線至伺服器或傳送資料錯誤';
      
      // Check if there are specific validation errors from the backend
      if (error.response?.data?.data && typeof error.response.data.data === 'object') {
        const validationErrors = Object.values(error.response.data.data);
        if (validationErrors.length > 0) {
          errorMessage = validationErrors.join('<br>');
        }
      }
      
      Swal.fire({
        title: '新增失敗',
        html: errorMessage,
        icon: 'error',
        confirmButtonColor: '#1e3c72'
      });
    }
  }
};

// 編輯管理員彈窗
const openEditModal = async (admin) => {
  // 將中文職稱轉回 Enum Value 以便預設選取
  const currentEnumPos = roleToEnumMap[admin.position] || admin.position;
  // 將 true/false 狀態轉換為字串 'true'/'false' 供 select 使用
  const currentEnabled = admin.isBanned ? 'false' : 'true';

  const { value: formValues } = await Swal.fire({
    title: '編輯管理員',
    html: `
      <div class="text-start">
        <div class="mb-3">
          <label class="form-label fw-bold">帳號</label>
          <input id="swal-edit-account" class="form-control" value="${admin.username}">
        </div>
        <div class="mb-3">
          <label class="form-label fw-bold">姓名</label>
          <input id="swal-edit-name" class="form-control" value="${admin.fullName}">
        </div>
        <div class="mb-3">
          <label class="form-label fw-bold">信箱</label>
          <input id="swal-edit-email" type="email" class="form-control" value="${admin.email}">
        </div>
        <div class="mb-3">
          <label class="form-label fw-bold">職位</label>
          <select id="swal-edit-position" class="form-select">
            <option value="SUPER_ADMIN" ${currentEnumPos === 'SUPER_ADMIN' ? 'selected' : ''}>總管理員</option>
            <option value="HUMAN_RESOURCE" ${currentEnumPos === 'HUMAN_RESOURCE' ? 'selected' : ''}>人事</option>
            <option value="CUSTOMER_SERVICE" ${currentEnumPos === 'CUSTOMER_SERVICE' ? 'selected' : ''}>客服</option>
            <option value="SHOP_MANAGER" ${currentEnumPos === 'SHOP_MANAGER' ? 'selected' : ''}>電商</option>
          </select>
        </div>
        <div class="mb-3">
          <label class="form-label fw-bold">帳號狀態</label>
          <select id="swal-edit-enabled" class="form-select">
            <option value="true" ${currentEnabled === 'true' ? 'selected' : ''}>正常</option>
            <option value="false" ${currentEnabled === 'false' ? 'selected' : ''}>已停權</option>
          </select>
        </div>
      </div>
    `,
    focusConfirm: false,
    showCancelButton: true,
    confirmButtonText: '儲存變更',
    cancelButtonText: '取消',
    confirmButtonColor: '#1e3c72',
    preConfirm: () => {
      const account = document.getElementById('swal-edit-account').value;
      const name = document.getElementById('swal-edit-name').value;
      const email = document.getElementById('swal-edit-email').value;
      const position = document.getElementById('swal-edit-position').value;
      const enabled = document.getElementById('swal-edit-enabled').value === 'true';

      if (!account || !name || !email || !position) {
        Swal.showValidationMessage('請填寫所有欄位');
        return false;
      }

      return { account, name, email, position, enabled };
    }
  });

  if (formValues) {
    try {
      const response = await adminAPI.update(admin.id, formValues);
      await fetchAdmins();

      Swal.fire({
        title: '更新成功',
        text: `已成功更新 ${formValues.name} 的資料`,
        icon: 'success',
        confirmButtonColor: '#1e3c72',
        timer: 2000
      });
    } catch (error) {
      console.error('更新管理員時發生錯誤:', error);
      
      let errorMessage = error.response?.data?.message || '無法連線至伺服器或儲存資料失敗';
      
      if (error.response?.data?.data && typeof error.response.data.data === 'object') {
        const validationErrors = Object.values(error.response.data.data);
        if (validationErrors.length > 0) {
          errorMessage = validationErrors.join('<br>');
        }
      }
      
      Swal.fire({
        title: '更新失敗',
        html: errorMessage,
        icon: 'error',
        confirmButtonColor: '#1e3c72'
      });
    }
  }
};
</script>

<style lang="scss" scoped>
@import '@/assets/styles/custom.scss';

.admin-list-view {
  animation: fadeIn 0.5s ease-out;
}

// Search UI Styles
.admin-form-select {
  padding: 0.5rem 2rem 0.5rem 1rem;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  font-size: 0.875rem;
  color: #374151;
  background-color: #fff;
  cursor: pointer;
  outline: none;
  transition: all 0.2s ease;

  &:focus {
    border-color: #1e3c72;
    box-shadow: 0 0 0 3px rgba(30, 60, 114, 0.1);
  }
}

.admin-form-control-search {
  &:focus {
    border-color: #e5e7eb !important;
    box-shadow: none !important;
  }
}

.input-group-text {
  border-color: #e5e7eb;
}

.form-control {
  border-color: #e5e7eb;
  font-size: 0.875rem;
  
  &:focus {
    border-color: #1e3c72;
    box-shadow: 0 0 0 3px rgba(30, 60, 114, 0.1);
  }
}

.position-badge {
  padding: 0.25rem 0.75rem;
  border-radius: 6px;
  font-size: 0.8rem;
  background-color: #f3f4f6;
  color: #4b5563;
  border: 1px solid #e5e7eb;
}

.status-badge {
  padding: 0.25rem 0.75rem;
  border-radius: 50px;
  font-size: 0.75rem;
  font-weight: 500;
  display: inline-block;

  &.status-active {
    background-color: rgba(16, 185, 129, 0.1);
    color: #10b981;
  }

  &.status-banned {
    background-color: rgba(239, 68, 68, 0.1);
    color: #ef4444;
  }
}

.action-btns {
  display: flex;
  gap: 0.5rem;
}

.btn-icon {
  background: none;
  border: none;
  padding: 4px;
  cursor: pointer;
  transition: opacity 0.2s;
  font-size: 1.1rem;

  &:hover {
    opacity: 0.7;
  }
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}
</style>
