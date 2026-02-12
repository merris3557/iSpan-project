<template>
  <div class="store-registration-check-view">
    <div class="d-flex justify-content-between align-items-center mb-4">
      <h2 class="text-admin-primary fw-bold mb-0">商家註冊審核</h2>
    </div>

    <!-- Status Tabs -->
    <div class="mb-4">
      <ul class="nav nav-tabs admin-tabs">
        <li class="nav-item">
          <a 
            class="nav-link" 
            :class="{ active: currentTab === 'All' }" 
            href="#" 
            @click.prevent="setTab('All')"
          >
            全部
          </a>
        </li>
        <li class="nav-item">
          <a 
            class="nav-link" 
            :class="{ active: currentTab === '未處理' }" 
            href="#" 
            @click.prevent="setTab('未處理')"
          >
            未處理
          </a>
        </li>
        <li class="nav-item">
          <a 
            class="nav-link" 
            :class="{ active: currentTab === '退回中' }" 
            href="#" 
            @click.prevent="setTab('退回中')"
          >
            退回中
          </a>
        </li>
        <li class="nav-item">
          <a 
            class="nav-link" 
            :class="{ active: currentTab === '已結案' }" 
            href="#" 
            @click.prevent="setTab('已結案')"
          >
            已結案
          </a>
        </li>
      </ul>
    </div>

    <div class="admin-card overflow-hidden border-top-0 rounded-top-0">
      <div class="table-responsive">
        <table class="admin-table">
          <thead>
            <tr>
              <th>案件編號</th>
              <th>申請人帳號</th>
              <th>申請人姓名</th>
              <th>申請時間</th>
              <th>案件狀態</th>
              <th>審核</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="item in paginatedData" :key="item.id">
              <td>#{{ item.id }}</td>
              <td>{{ item.email }}</td>
              <td>
                <span class="fw-medium">{{ item.name }}</span>
              </td>
              <td>{{ item.applytime }}</td>
              <td>
                <span :class="['status-badge', getStatusClass(item.status)]">
                  {{ item.status }}
                </span>
              </td>
              <td>
                <button 
                  class="btn-admin-outline-primary btn-sm" 
                  @click="openAuditModal(item)"
                  :disabled="item.status === '已結案'"
                >
                  <i class="bi bi-file-earmark-check me-1"></i>
                  審核
                </button>
              </td>
            </tr>
            <tr v-if="paginatedData.length === 0">
              <td colspan="6" class="text-center py-5 text-muted">
                沒有找到符合狀態的申請資料
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
import { ref, computed } from 'vue';
import axios from 'axios';
import Swal from 'sweetalert2';
import storeRegistrationData from '@/data/storeRegistrationData.json';

const registrations = ref(storeRegistrationData);
const currentPage = ref(1);
const pageSize = 10;
const currentTab = ref('All');

const setTab = (tab) => {
  currentTab.value = tab;
  currentPage.value = 1; // Reset to first page when switching tabs
};

// Computed: Filter logic
const filteredData = computed(() => {
  if (currentTab.value === 'All') {
    return registrations.value;
  }
  return registrations.value.filter(item => item.status === currentTab.value);
});

// Computed: Pagination logic
const totalItems = computed(() => filteredData.value.length);
const totalPages = computed(() => Math.ceil(totalItems.value / pageSize));
const startIndex = computed(() => (currentPage.value - 1) * pageSize);
const endIndex = computed(() => startIndex.value + pageSize);

const paginatedData = computed(() => {
  return filteredData.value.slice(startIndex.value, endIndex.value);
});

// Helper for status badge class
const getStatusClass = (status) => {
  switch (status) {
    case '已結案':
        return 'status-active'; // Green
    case '退回中':
        return 'status-banned'; // Red
    case '未處理':
        return 'status-pending'; // Amber
    default:
        return 'status-user'; // Gray
  }
};

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

const changePage = (page) => {
  if (page >= 1 && page <= totalPages.value) {
    currentPage.value = page;
  }
};

// Audit Logic
const openAuditModal = async (item) => {
  // Mock Data for demonstration since current JSON doesn't have these fields
  const mockData = {
    ownerName: item.name,
    storePhone: '0912-345-678', // Placeholder
    storeAddress: '台北市大安區復興南路一段390號' // Placeholder
  };

  const { value: result } = await Swal.fire({
    title: `審核申請 #${item.id}`,
    html: `
      <div class="text-start">
        <div class="mb-3 p-3 bg-light rounded">
          <h6 class="fw-bold mb-2 border-bottom pb-2">申請人資料</h6>
          <p class="mb-1"><strong>姓名:</strong> ${mockData.ownerName}</p>
          <p class="mb-1"><strong>電話:</strong> ${mockData.storePhone}</p>
          <p class="mb-0"><strong>地址:</strong> ${mockData.storeAddress}</p>
        </div>
        <div class="mb-3">
          <label class="form-label fw-bold">審核意見</label>
          <textarea id="audit-opinion" class="form-control" rows="3" placeholder="請輸入審核意見 (退回時必填)"></textarea>
        </div>
      </div>
    `,
    showDenyButton: true,
    showCancelButton: true,
    confirmButtonText: '同意申請',
    denyButtonText: '退回申請',
    cancelButtonText: '取消',
    confirmButtonColor: '#10b981', // Green
    denyButtonColor: '#ef4444', // Red
    width: '600px',
    preConfirm: () => {
      return {
        action: 'approve',
        opinion: document.getElementById('audit-opinion').value
      };
    },
    preDeny: () => {
      const opinion = document.getElementById('audit-opinion').value;
      if (!opinion.trim()) {
        Swal.showValidationMessage('退回申請時必須填寫審核意見');
        return false;
      }
      return {
        action: 'return',
        opinion: opinion
      };
    }
  });

  if (result) {
    if (result.action === 'approve') {
       handleApprove(item, mockData, result.opinion);
    } else if (result.action === 'return') {
       handleReturn(item, result.opinion);
    }
  }
};

const handleApprove = async (item, data, opinion) => {
  const confirmResult = await Swal.fire({
    title: '確認要同意這筆申請?',
    html: `
      <div class="text-start bg-light p-3 rounded small">
        <p class="mb-1"><strong>姓名:</strong> ${data.ownerName}</p>
        <p class="mb-1"><strong>電話:</strong> ${data.storePhone}</p>
        <p class="mb-0"><strong>地址:</strong> ${data.storeAddress}</p>
      </div>
      <p class="mt-3 text-muted small">此操作將把案件狀態改為「已結案」</p>
    `,
    icon: 'question',
    showCancelButton: true,
    confirmButtonText: '確認同意',
    cancelButtonText: '取消',
    confirmButtonColor: '#10b981'
  });

  if (confirmResult.isConfirmed) {
    try {
      // Mock API call
      // await axios.post('/admin/store-registration/approve', { id: item.id, opinion });
      
      // Update local state
      const index = registrations.value.findIndex(r => r.id === item.id);
      if (index !== -1) {
        registrations.value[index].status = '已結案';
      }

      Swal.fire('已結案', '申請已成功同意', 'success');
      
      // Convert to JSON for backend handover (Simulated)
      console.log('Backend Payload:', JSON.stringify({
        id: item.id,
        status: '已結案',
        opinion: opinion,
        reviewedAt: new Date().toISOString()
      }, null, 2));

    } catch (error) {
      Swal.fire('錯誤', '更新失敗', 'error');
    }
  }
};

const handleReturn = async (item, opinion) => {
  const confirmResult = await Swal.fire({
    title: '確認要退回這筆申請?',
    html: `
      <div class="text-start">
        <label class="fw-bold">您的審核意見:</label>
        <div class="p-2 bg-light border rounded text-danger mt-1">
          ${opinion}
        </div>
      </div>
      <p class="mt-3 text-muted small">此操作將把案件狀態改為「退回中」</p>
    `,
    icon: 'warning',
    showCancelButton: true,
    confirmButtonText: '確認退回',
    cancelButtonText: '取消',
    confirmButtonColor: '#ef4444'
  });

  if (confirmResult.isConfirmed) {
    try {
      // Mock API call
      // await axios.post('/admin/store-registration/return', { id: item.id, opinion });
      
      // Update local state
      const index = registrations.value.findIndex(r => r.id === item.id);
      if (index !== -1) {
        registrations.value[index].status = '退回中';
      }

      Swal.fire('已退回', '申請已退回給使用者', 'success');

      // Convert to JSON for backend handover (Simulated)
      console.log('Backend Payload:', JSON.stringify({
        id: item.id,
        status: '退回中',
        opinion: opinion,
        reviewedAt: new Date().toISOString()
      }, null, 2));

    } catch (error) {
      Swal.fire('錯誤', '更新失敗', 'error');
    }
  }
};

</script>

<style lang="scss" scoped>
@import '@/assets/styles/custom.scss';

.store-registration-check-view {
  animation: fadeIn 0.5s ease-out;
}

.admin-tabs {
  border-bottom: 1px solid #e5e7eb;
  
  .nav-item {
    margin-bottom: -1px;
    
    .nav-link {
        color: #6b7280;
        border: none;
        border-bottom: 2px solid transparent;
        padding: 0.75rem 1.5rem;
        font-weight: 500;
        transition: all 0.2s ease;
        
        &:hover {
            color: #1e3c72;
            background-color: transparent;
        }
        
        &.active {
            color: #1e3c72;
            background-color: transparent;
            border-bottom-color: #1e3c72;
            font-weight: 600;
        }
    }
  }
}

.table-responsive {
  margin: 0;
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
  
  &.status-pending {
     background-color: rgba(245, 158, 11, 0.1); /* Amber/Orange */
     color: #f59e0b;
  }

  &.status-user {
    background-color: rgba(107, 114, 128, 0.1);
    color: #6b7280;
  }
}

.btn-admin-outline-primary {
    color: #1e3c72;
    border: 1px solid #1e3c72;
    background: transparent;
    transition: all 0.2s;
    
    &:hover:not(:disabled) {
        background-color: #1e3c72;
        color: white;
    }
    
    &:disabled {
        border-color: #e5e7eb;
        color: #9ca3af;
        cursor: not-allowed;
    }
}

// Pagination Styles (Copied from UserListView for consistency)
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
