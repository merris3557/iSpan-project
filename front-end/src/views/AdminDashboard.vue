<template>
  <div class="admin-dashboard">
    <div class="page-header">
      <h1 class="page-title">後台管理首頁</h1>
      <p class="page-subtitle">歡迎使用速賺家後台管理系統</p>
    </div>

    <!-- Stats Cards -->
    <div class="stats-grid">
      <div class="stat-card">
        <div class="stat-icon bg-primary">
          <i class="bi bi-people"></i>
        </div>
        <div class="stat-content">
          <div class="stat-label">總會員數</div>
          <div class="stat-value">1,234</div>
          <div class="stat-change positive">
            <i class="bi bi-arrow-up"></i> +12.5%
          </div>
        </div>
      </div>

      <div class="stat-card">
        <div class="stat-icon bg-success">
          <i class="bi bi-cart-check"></i>
        </div>
        <div class="stat-content">
          <div class="stat-label">今日訂單</div>
          <div class="stat-value">89</div>
          <div class="stat-change positive">
            <i class="bi bi-arrow-up"></i> +8.2%
          </div>
        </div>
      </div>

      <div class="stat-card">
        <div class="stat-icon bg-warning">
          <i class="bi bi-cash-stack"></i>
        </div>
        <div class="stat-content">
          <div class="stat-label">本月營收</div>
          <div class="stat-value">¥45,678</div>
          <div class="stat-change positive">
            <i class="bi bi-arrow-up"></i> +15.3%
          </div>
        </div>
      </div>

      <div class="stat-card">
        <div class="stat-icon bg-danger">
          <i class="bi bi-box-seam"></i>
        </div>
        <div class="stat-content">
          <div class="stat-label">商品總數</div>
          <div class="stat-value">567</div>
          <div class="stat-change negative">
            <i class="bi bi-arrow-down"></i> -2.1%
          </div>
        </div>
      </div>
    </div>

    <!-- Recent Orders Table -->
    <div class="content-card">
      <div class="card-header">
        <h2 class="card-title">最近訂單</h2>
        <a href="#" class="view-all-link">查看全部 <i class="bi bi-arrow-right"></i></a>
      </div>
      <div class="table-responsive">
        <table class="data-table">
          <thead>
            <tr>
              <th>訂單編號</th>
              <th>客戶名稱</th>
              <th>商品</th>
              <th>金額</th>
              <th>狀態</th>
              <th>日期</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="order in recentOrders" :key="order.id">
              <td class="order-id">{{ order.id }}</td>
              <td>{{ order.customer }}</td>
              <td>{{ order.product }}</td>
              <td class="amount">¥{{ order.amount }}</td>
              <td>
                <span class="status-badge" :class="'status-' + order.status">
                  {{ getStatusText(order.status) }}
                </span>
              </td>
              <td class="date">{{ order.date }}</td>
              <td>
                <div class="action-buttons">
                  <button class="btn-action btn-view" title="查看">
                    <i class="bi bi-eye"></i>
                  </button>
                  <button class="btn-action btn-edit" title="編輯">
                    <i class="bi bi-pencil"></i>
                  </button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- Quick Actions -->
    <div class="quick-actions">
      <h2 class="section-title">快速操作</h2>
      <div class="actions-grid">
        <a href="#" class="action-item">
          <div class="action-icon">
            <i class="bi bi-plus-circle"></i>
          </div>
          <span>新增商品</span>
        </a>
        <a href="#" class="action-item">
          <div class="action-icon">
            <i class="bi bi-file-earmark-text"></i>
          </div>
          <span>建立訂單</span>
        </a>
        <a href="#" class="action-item">
          <div class="action-icon">
            <i class="bi bi-megaphone"></i>
          </div>
          <span>發布消息</span>
        </a>
        <a href="#" class="action-item">
          <div class="action-icon">
            <i class="bi bi-bar-chart-line"></i>
          </div>
          <span>查看報表</span>
        </a>
      </div>
    </div>
  </div>
</template>

<script>
import { ref } from 'vue';

export default {
  name: 'AdminDashboard',
  setup() {
    const recentOrders = ref([
      {
        id: 'ORD-2024-001',
        customer: '張小明',
        product: '商品A',
        amount: '1,250',
        status: 'completed',
        date: '2024-02-01'
      },
      {
        id: 'ORD-2024-002',
        customer: '李美華',
        product: '商品B',
        amount: '850',
        status: 'pending',
        date: '2024-02-02'
      },
      {
        id: 'ORD-2024-003',
        customer: '王大衛',
        product: '商品C',
        amount: '2,100',
        status: 'processing',
        date: '2024-02-02'
      },
      {
        id: 'ORD-2024-004',
        customer: '陳小芳',
        product: '商品D',
        amount: '680',
        status: 'completed',
        date: '2024-02-03'
      },
      {
        id: 'ORD-2024-005',
        customer: '林志明',
        product: '商品E',
        amount: '1,560',
        status: 'cancelled',
        date: '2024-02-03'
      }
    ]);

    const getStatusText = (status) => {
      const statusMap = {
        completed: '已完成',
        pending: '待處理',
        processing: '處理中',
        cancelled: '已取消'
      };
      return statusMap[status] || status;
    };

    return {
      recentOrders,
      getStatusText
    };
  }
};
</script>

<style lang="scss" scoped>
@import '@/assets/styles/custom.scss';

.admin-dashboard {
  max-width: 1400px;
  margin: 0 auto;
}

.page-header {
  margin-bottom: 2rem;

  .page-title {
    font-size: 1.875rem;
    font-weight: 700;
    color: #1f2937;
    margin: 0 0 0.5rem 0;
  }

  .page-subtitle {
    color: #6b7280;
    font-size: 1rem;
    margin: 0;
  }
}

// Stats Grid
.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 1.5rem;
  margin-bottom: 2rem;
}

.stat-card {
  background: #fff;
  border-radius: 12px;
  padding: 1.5rem;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  display: flex;
  align-items: center;
  gap: 1rem;
  transition: all 0.3s ease;

  &:hover {
    transform: translateY(-4px);
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  }

  .stat-icon {
    width: 60px;
    height: 60px;
    border-radius: 12px;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 1.75rem;
    color: #fff;

    &.bg-primary {
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    }

    &.bg-success {
      background: linear-gradient(135deg, #84fab0 0%, #8fd3f4 100%);
    }

    &.bg-warning {
      background: linear-gradient(135deg, #fa709a 0%, #fee140 100%);
    }

    &.bg-danger {
      background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
    }
  }

  .stat-content {
    flex: 1;

    .stat-label {
      font-size: 0.875rem;
      color: #6b7280;
      margin-bottom: 0.25rem;
    }

    .stat-value {
      font-size: 1.875rem;
      font-weight: 700;
      color: #1f2937;
      margin-bottom: 0.25rem;
    }

    .stat-change {
      font-size: 0.875rem;
      display: flex;
      align-items: center;
      gap: 0.25rem;

      &.positive {
        color: #10b981;
      }

      &.negative {
        color: #ef4444;
      }
    }
  }
}

// Content Card
.content-card {
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  overflow: hidden;
  margin-bottom: 2rem;

  .card-header {
    padding: 1.5rem;
    border-bottom: 1px solid #e5e7eb;
    display: flex;
    justify-content: space-between;
    align-items: center;

    .card-title {
      font-size: 1.25rem;
      font-weight: 600;
      color: #1f2937;
      margin: 0;
    }

    .view-all-link {
      color: #1e3c72;
      text-decoration: none;
      font-size: 0.875rem;
      font-weight: 500;
      display: flex;
      align-items: center;
      gap: 0.25rem;
      transition: color 0.2s ease;

      &:hover {
        color: #2a5298;
      }
    }
  }
}

// Table Styles
.table-responsive {
  overflow-x: auto;
}

.data-table {
  width: 100%;
  border-collapse: collapse;

  thead {
    background-color: #f9fafb;

    th {
      padding: 1rem 1.5rem;
      text-align: left;
      font-size: 0.875rem;
      font-weight: 600;
      color: #6b7280;
      text-transform: uppercase;
      letter-spacing: 0.05em;
      border-bottom: 2px solid #e5e7eb;
    }
  }

  tbody {
    tr {
      border-bottom: 1px solid #e5e7eb;
      transition: background-color 0.2s ease;

      &:hover {
        background-color: #f9fafb;
      }

      &:last-child {
        border-bottom: none;
      }
    }

    td {
      padding: 1rem 1.5rem;
      font-size: 0.875rem;
      color: #374151;

      &.order-id {
        font-family: 'Courier New', monospace;
        font-weight: 600;
        color: #1e3c72;
      }

      &.amount {
        font-weight: 600;
        color: #059669;
      }

      &.date {
        color: #6b7280;
      }
    }
  }
}

// Status Badges
.status-badge {
  display: inline-block;
  padding: 0.375rem 0.75rem;
  border-radius: 9999px;
  font-size: 0.75rem;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.025em;

  &.status-completed {
    background-color: #d1fae5;
    color: #065f46;
  }

  &.status-pending {
    background-color: #fef3c7;
    color: #92400e;
  }

  &.status-processing {
    background-color: #dbeafe;
    color: #1e40af;
  }

  &.status-cancelled {
    background-color: #fee2e2;
    color: #991b1b;
  }
}

// Action Buttons
.action-buttons {
  display: flex;
  gap: 0.5rem;
}

.btn-action {
  width: 32px;
  height: 32px;
  border: none;
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.2s ease;
  font-size: 0.875rem;

  &.btn-view {
    background-color: #e0e7ff;
    color: #4338ca;

    &:hover {
      background-color: #c7d2fe;
    }
  }

  &.btn-edit {
    background-color: #dbeafe;
    color: #1e40af;

    &:hover {
      background-color: #bfdbfe;
    }
  }
}

// Quick Actions
.quick-actions {
  margin-top: 2rem;

  .section-title {
    font-size: 1.25rem;
    font-weight: 600;
    color: #1f2937;
    margin-bottom: 1rem;
  }

  .actions-grid {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
    gap: 1rem;
  }

  .action-item {
    background: #fff;
    border: 2px dashed #e5e7eb;
    border-radius: 12px;
    padding: 1.5rem;
    text-align: center;
    text-decoration: none;
    color: #6b7280;
    transition: all 0.3s ease;
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 0.75rem;

    .action-icon {
      width: 60px;
      height: 60px;
      border-radius: 50%;
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      color: #fff;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 1.5rem;
    }

    span {
      font-weight: 500;
      font-size: 0.875rem;
    }

    &:hover {
      border-color: #1e3c72;
      color: #1e3c72;
      transform: translateY(-4px);
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);

      .action-icon {
        transform: scale(1.1);
      }
    }
  }
}

// Responsive
@media (max-width: 768px) {
  .stats-grid {
    grid-template-columns: 1fr;
  }

  .data-table {
    font-size: 0.8125rem;

    thead th,
    tbody td {
      padding: 0.75rem 1rem;
    }
  }
}
</style>
