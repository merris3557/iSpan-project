<template>
  <div class="user-list-view">
    <div class="d-flex justify-content-between align-items-center mb-4">
      <h2 class="text-admin-primary fw-bold mb-0">使用者管理</h2>
      <!-- <button class="btn-admin-primary">
        <i class="bi bi-person-plus me-2"></i>新增使用者
      </button> -->
    </div>

    <!-- Search Bar Section -->
    <div class="admin-card p-3 mb-4">
      <div class="row g-3 align-items-center">
        <!-- Role Filter -->
        <div class="col-auto">
          <select v-model="filterOwner" class="admin-form-select">
            <option value="default" disabled selected>--篩選身分--</option>
            <option value="all">全部</option>
            <option value="false">一般用戶</option>
            <option value="true">商家</option>
          </select>
        </div>
        <!-- Status Filter -->
        <div class="col-auto">
          <select v-model="filterBanned" class="admin-form-select">
            <option value="default" disabled selected>--篩選狀態--</option>
            <option value="all">全部</option>
            <option value="false">正常</option>
            <option value="true">已停權</option>
          </select>
        </div>
        <!-- Search Category -->
        <div class="col-auto">
          <select v-model="searchCategory" class="admin-form-select" default="id">
            <option value="default" disabled selected>--選擇查詢欄位--</option>
            <option value="all">全部查詢</option>
            <option value="id">ID</option>
            <option value="username">使用者名稱</option>
            <option value="email">電子郵件</option>
            <option value="phone">電話</option>
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
              placeholder="搜尋使用者..."
              @input="onSearch"
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
              <th>使用者名稱</th>
              <th>電子郵件</th>
              <th>電話</th>
              <th>身分</th>
              <th>停權狀態</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="user in paginatedUsers" :key="user.id">
              <td>{{ user.id }}</td>
              <td>
                <div class="d-flex align-items-center">
                  <!-- <div class="user-avatar-sm me-2">
                    {{ user.username.charAt(0).toUpperCase() }}
                  </div> -->
                  <span class="fw-medium">{{ user.username }}</span>
                </div>
              </td>
              <td>{{ user.email }}</td>
              <td>{{ user.phone }}</td>
              <td>
                <span :class="['status-badge', user.isStoreOwner ? 'status-owner' : 'status-user']">
                  {{ user.isStoreOwner ? '店家' : '一般用戶' }}
                </span>
              </td>
              <td>
                <span :class="['status-badge', user.isBanned ? 'status-banned' : 'status-active']">
                  {{ user.isBanned ? '已停權' : '正常' }}
                </span>
              </td>
              <td>
                <div class="action-btns">
                  <button class="btn-icon text-primary" title="編輯" @click="openEditModal(user)">
                    <i class="bi bi-pencil-square"></i>
                  </button>
                  <!-- <button class="btn-icon text-danger" title="停權" v-if="!user.isBanned">
                    <i class="bi bi-slash-circle"></i>
                  </button>
                  <button class="btn-icon text-success" title="解除停權" v-else>
                    <i class="bi bi-check-circle"></i>
                  </button> -->
                </div>
              </td>
            </tr>
            <tr v-if="paginatedUsers.length === 0">
              <td colspan="7" class="text-center py-5 text-muted">
                沒有找到匹配的使用者
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- Pagination Section -->
      <div class="pagination-container border-top" v-if="totalItems > 0">
        <div class="pagination-info">
          顯示第 {{ startIndex + 1 }} 至 {{ Math.min(endIndex, totalItems) }} 筆，共 {{ totalItems }} 筆
        </div>
        <nav class="pagination-nav">
          <button 
            class="pagination-btn" 
            :disabled="currentPage === 1" 
            @click="changePage(currentPage - 1)"
          >
            <i class="bi bi-chevron-left"></i>
          </button>
          
          <div class="pagination-pages" v-if="totalPages <= 7">
            <button 
              v-for="page in totalPages" 
              :key="page" 
              :class="['pagination-btn', { active: currentPage === page }]"
              @click="changePage(page)"
            >
              {{ page }}
            </button>
          </div>
          <div class="pagination-pages" v-else>
            <!-- Basic ellipsis pagination could be added here for better UX if page count is large -->
            <button 
              v-for="page in visiblePages" 
              :key="page" 
              :class="['pagination-btn', { active: currentPage === page }]"
              @click="changePage(page)"
            >
              {{ page }}
            </button>
          </div>

          <button 
            class="pagination-btn" 
            :disabled="currentPage === totalPages" 
            @click="changePage(currentPage + 1)"
          >
            <i class="bi bi-chevron-right"></i>
          </button>
        </nav>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue';
import usersData from '@/data/usersData.json';
import Swal from 'sweetalert2';

// Search state
const searchQuery = ref('');
const searchCategory = ref('all');

// Advanced Filter state
const filterOwner = ref('all');
const filterBanned = ref('all');

const resetFilters = () => {
  searchQuery.value = '';
  searchCategory.value = 'default';
  filterOwner.value = 'default';
  filterBanned.value = 'default';
  currentPage.value = 1;
};

// Pagination state
const currentPage = ref(1);
const pageSize = 10;
const users = ref(usersData);

// Computed: Filtered users based on search and status filters
const filteredUsers = computed(() => {
  let result = users.value;

  // 1. Role filter (Owner / User)
  if (filterOwner.value !== 'all' && filterOwner.value !== 'default') {
    const isOwner = filterOwner.value === 'true';
    result = result.filter(user => user.isStoreOwner === isOwner);
  }

  // 2. Status filter (Active / Banned)
  if (filterBanned.value !== 'all' && filterBanned.value !== 'default') {
    const isBanned = filterBanned.value === 'true';
    result = result.filter(user => user.isBanned === isBanned);
  }

  // 3. Search Query filter
  if (!searchQuery.value.trim()) return result;
  
  const query = searchQuery.value.toLowerCase().trim();
  return result.filter(user => {
    if (searchCategory.value === 'all' || searchCategory.value === 'default') {
      return (
        user.id.toString().includes(query) ||
        user.username.toLowerCase().includes(query) ||
        user.email.toLowerCase().includes(query) ||
        user.phone.includes(query)
      );
    } else if (searchCategory.value === 'id') {
      return user.id.toString().includes(query);
    } else if (searchCategory.value === 'username') {
      return user.username.toLowerCase().includes(query);
    } else if (searchCategory.value === 'email') {
      return user.email.toLowerCase().includes(query);
    } else if (searchCategory.value === 'phone') {
      return user.phone.includes(query);
    }
    return true;
  });
});

// Computed: Pagination logic using filtered results
const totalItems = computed(() => filteredUsers.value.length);
const totalPages = computed(() => Math.ceil(totalItems.value / pageSize));
const startIndex = computed(() => (currentPage.value - 1) * pageSize);
const endIndex = computed(() => startIndex.value + pageSize);

const paginatedUsers = computed(() => {
  return filteredUsers.value.slice(startIndex.value, endIndex.value);
});

// Simple page range for pagination
const visiblePages = computed(() => {
  const pages = [];
  const maxVisible = 5;
  let start = Math.max(1, currentPage.value - 2);
  let end = Math.min(totalPages.value, start + maxVisible - 1);
  
  if (end - start + 1 < maxVisible) {
    start = Math.max(1, end - maxVisible + 1);
  }
  
  for (let i = start; i <= end; i++) {
    pages.push(i);
  }
  return pages;
});

const onSearch = () => {
  currentPage.value = 1; // Reset to first page on search
};

// Reset to page 1 if search category or filters change
watch([searchCategory, filterOwner, filterBanned], () => {
  currentPage.value = 1;
});

const changePage = (page) => {
  if (page >= 1 && page <= totalPages.value) {
    currentPage.value = page;
    // Scroll list top if needed
    // window.scrollTo({ top: 0, behavior: 'smooth' });
  }
};

// Edit User Functionality
const openEditModal = async (user) => {
  const { value: formValues } = await Swal.fire({
    title: `編輯使用者 #${user.id}`,
    html: `
      <div class="text-start">
        <div class="mb-3">
          <label class="form-label fw-bold">使用者名稱</label>
          <input id="swal-input-username" class="form-control" value="${user.username}">
        </div>
        <div class="mb-3">
          <label class="form-label fw-bold">電子郵件</label>
          <input id="swal-input-email" class="form-control" value="${user.email}">
        </div>
        <div class="mb-3">
          <label class="form-label fw-bold">電話</label>
          <input id="swal-input-phone" class="form-control" value="${user.phone}">
        </div>
        <div class="mb-3">
          <label class="form-label fw-bold">身分</label>
          <select id="swal-input-role" class="form-select">
            <option value="false" ${!user.isStoreOwner ? 'selected' : ''}>一般用戶</option>
            <option value="true" ${user.isStoreOwner ? 'selected' : ''}>商家</option>
          </select>
        </div>
        <div class="mb-3">
          <label class="form-label fw-bold">狀態</label>
          <select id="swal-input-status" class="form-select">
            <option value="false" ${!user.isBanned ? 'selected' : ''}>正常</option>
            <option value="true" ${user.isBanned ? 'selected' : ''}>已停權</option>
          </select>
        </div>
      </div>
    `,
    focusConfirm: false,
    showCancelButton: true,
    confirmButtonText: '確定',
    cancelButtonText: '取消',
    confirmButtonColor: '#1e3c72',
    preConfirm: () => {
      return {
        username: document.getElementById('swal-input-username').value,
        email: document.getElementById('swal-input-email').value,
        phone: document.getElementById('swal-input-phone').value,
        isStoreOwner: document.getElementById('swal-input-role').value === 'true',
        isBanned: document.getElementById('swal-input-status').value === 'true'
      };
    }
  });

  if (formValues) {
    // Detect changes
    const changes = [];
    if (formValues.username !== user.username) changes.push(`使用者名稱: ${formValues.username}`);
    if (formValues.email !== user.email) changes.push(`電子郵件: ${formValues.email}`);
    if (formValues.phone !== user.phone) changes.push(`電話: ${formValues.phone}`);
    if (formValues.isStoreOwner !== user.isStoreOwner) changes.push(`身分: ${formValues.isStoreOwner ? '商家' : '一般用戶'}`);
    if (formValues.isBanned !== user.isBanned) changes.push(`狀態: ${formValues.isBanned ? '已停權' : '正常'}`);

    if (changes.length > 0) {
      // Secondary confirmation
      const confirmResult = await Swal.fire({
        title: '確認修改？',
        html: `
          <p class="mb-2">即將將 <b>#${user.id}</b> 的資料修改為：</p>
          <ul class="text-start mb-0" style="list-style-position: inside;">
            ${changes.map(change => `<li>${change}</li>`).join('')}
          </ul>
        `,
        icon: 'warning',
        showCancelButton: true,
        confirmButtonText: '確認修改',
        cancelButtonText: '取消',
        confirmButtonColor: '#1e3c72',
        reverseButtons: true
      });

      if (confirmResult.isConfirmed) {
        // Update local data (Simulation)
        const index = users.value.findIndex(u => u.id === user.id);
        if (index !== -1) {
          users.value[index] = { ...users.value[index], ...formValues };
          
          Swal.fire({
            title: '修改成功',
            text: '使用者資料已更新',
            icon: 'success',
            confirmButtonColor: '#1e3c72',
            timer: 1500
          });
        }
      }
    } else {
      Swal.fire({
        title: '無變更',
        text: '您沒有修改任何資料',
        icon: 'info',
        confirmButtonColor: '#1e3c72'
      });
    }
  }
};
</script>

<style lang="scss" scoped>
@import '@/assets/styles/custom.scss';

.user-list-view {
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

.table-responsive {
  margin: 0;
}

.user-avatar-sm {
  width: 32px;
  height: 32px;
  background-color: #1e3c72;
  color: white;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 0.875rem;
  font-weight: 600;
}

.status-badge {
  padding: 0.25rem 0.75rem;
  border-radius: 50px;
  font-size: 0.75rem;
  font-weight: 500;
  display: inline-block;

  &.status-owner {
    background-color: rgba(59, 130, 246, 0.1);
    color: #3b82f6;
  }

  &.status-user {
    background-color: rgba(107, 114, 128, 0.1);
    color: #6b7280;
  }

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

// Pagination Styles
.pagination-container {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1rem 1.5rem;
  background-color: #fff;

  .pagination-info {
    font-size: 0.875rem;
    color: #6b7280;
  }

  .pagination-nav {
    display: flex;
    align-items: center;
    gap: 0.5rem;
  }

  .pagination-pages {
    display: flex;
    gap: 0.25rem;
  }

  .pagination-btn {
    display: flex;
    align-items: center;
    justify-content: center;
    min-width: 32px;
    height: 32px;
    padding: 0 6px;
    border: 1px solid #e5e7eb;
    background-color: #fff;
    color: #374151;
    border-radius: 6px;
    font-size: 0.875rem;
    font-weight: 500;
    cursor: pointer;
    transition: all 0.2s ease;

    &:hover:not(:disabled) {
      border-color: #1e3c72;
      color: #1e3c72;
      background-color: rgba(30, 60, 114, 0.05);
    }

    &.active {
      background-color: #1e3c72;
      border-color: #1e3c72;
      color: #fff;
    }

    &:disabled {
      cursor: not-allowed;
      opacity: 0.5;
      background-color: #f9fafb;
    }
  }
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}
</style>
