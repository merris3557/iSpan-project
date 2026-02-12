<template>
  <div class="erp-transfer-page">
    <div class="page-header">
      <h1 class="page-title">ERP格式轉換</h1>
      <p class="page-subtitle">選擇來源平台並匯入檔案</p>
    </div>

    <!-- Tabs -->
    <div class="tabs-container">
      <ul class="nav-tabs">
        <li class="tab-item">
          <a href="#" class="tab-link active">
            首頁
            <button class="close-btn"><i class="bi bi-x"></i></button>
          </a>
        </li>
        <li class="tab-item">
          <a href="#" class="tab-link">
            訂單列表
            <button class="close-btn"><i class="bi bi-x"></i></button>
          </a>
        </li>
        <li class="tab-item">
          <a href="#" class="tab-link active-tab">
            ERP格式轉換
            <button class="close-btn"><i class="bi bi-x"></i></button>
          </a>
        </li>
      </ul>
    </div>

    <!-- Main Content -->
    <div class="content-card">
      <div class="form-section">
        <div class="form-group">
          <label class="form-label">請選擇訂單來源平台</label>
          <div class="select-wrapper">
            <select v-model="selectedPlatform" class="form-select">
              <option value="" disabled>請選擇訂單來源平台</option>
              <option value="蝦皮">蝦皮</option>
              <option value="PChome">PChome</option>
              <option value="Momo">Momo</option>
              <option value="Yahoo">Yahoo</option>
              <option value="其他">其他</option>
            </select>
            <i class="bi bi-chevron-down select-arrow"></i>
          </div>
        </div>

        <div class="upload-section">
          <button class="btn-upload" @click="triggerFileInput">
            <i class="bi bi-upload"></i>
            確入來源檔案
          </button>
          <input 
            ref="fileInput" 
            type="file" 
            @change="handleFileUpload" 
            accept=".csv,.xlsx,.xls"
            style="display: none;"
          >
        </div>
      </div>

      <!-- Platform Display Section -->
      <div class="platform-display" v-if="selectedPlatform">
        <div class="display-row">
          <div class="display-label">蝦皮</div>
          <div class="display-value">{{ selectedPlatform }}</div>
        </div>
      </div>

      <!-- Data Table -->
      <div class="data-table-section">
        <table class="data-table">
          <thead>
            <tr>
              <th>銷貨日期</th>
              <th>備註</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="(row, index) in tableData" :key="index">
              <td>{{ row.date }}</td>
              <td>{{ row.note }}</td>
            </tr>
            <tr v-if="tableData.length === 0">
              <td colspan="2" class="empty-state">尚無資料</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </div>
</template>

<script>
import { ref } from 'vue';

export default {
  name: 'ERPTransferView',
  setup() {
    const selectedPlatform = ref('');
    const fileInput = ref(null);
    const tableData = ref([]);

    const triggerFileInput = () => {
      fileInput.value.click();
    };

    const handleFileUpload = (event) => {
      const file = event.target.files[0];
      if (file) {
        console.log('Uploaded file:', file.name);
        // Add file processing logic here
        // For demo, add some sample data
        tableData.value = [
          { date: '2024-02-01', note: '訂單處理中' },
          { date: '2024-02-02', note: '已出貨' },
          { date: '2024-02-03', note: '待確認' }
        ];
      }
    };

    return {
      selectedPlatform,
      fileInput,
      tableData,
      triggerFileInput,
      handleFileUpload
    };
  }
};
</script>

<style lang="scss" scoped>
@import '@/assets/styles/custom.scss';

.erp-transfer-page {
  max-width: 1200px;
  margin: 0 auto;
}

.page-header {
  margin-bottom: 1.5rem;

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

// Tabs
.tabs-container {
  margin-bottom: 1.5rem;
  border-bottom: 2px solid #e5e7eb;
}

.nav-tabs {
  display: flex;
  list-style: none;
  padding: 0;
  margin: 0;
  gap: 0.5rem;
}

.tab-item {
  .tab-link {
    display: flex;
    align-items: center;
    gap: 0.75rem;
    padding: 0.75rem 1.5rem;
    background-color: #f3f4f6;
    color: #6b7280;
    text-decoration: none;
    border-radius: 8px 8px 0 0;
    font-size: 0.875rem;
    font-weight: 500;
    transition: all 0.2s ease;

    &:hover {
      background-color: #e5e7eb;
    }

    &.active-tab {
      background-color: #fff;
      color: #1e3c72;
      border-bottom: 3px solid #1e3c72;
    }

    .close-btn {
      background: none;
      border: none;
      padding: 0;
      color: #9ca3af;
      cursor: pointer;
      font-size: 1.125rem;
      line-height: 1;
      transition: color 0.2s ease;

      &:hover {
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
  padding: 2rem;
}

// Form Section
.form-section {
  display: flex;
  align-items: flex-end;
  gap: 1.5rem;
  margin-bottom: 2rem;
  flex-wrap: wrap;

  .form-group {
    flex: 1;
    min-width: 250px;
  }

  .form-label {
    display: block;
    font-size: 0.875rem;
    font-weight: 600;
    color: #374151;
    margin-bottom: 0.5rem;
  }

  .select-wrapper {
    position: relative;

    .form-select {
      width: 100%;
      padding: 0.75rem 2.5rem 0.75rem 1rem;
      border: 1px solid #d1d5db;
      border-radius: 8px;
      font-size: 0.875rem;
      color: #374151;
      background-color: #fff;
      appearance: none;
      cursor: pointer;
      transition: all 0.2s ease;

      &:focus {
        outline: none;
        border-color: #1e3c72;
        box-shadow: 0 0 0 3px rgba(30, 60, 114, 0.1);
      }

      option {
        padding: 0.5rem;
      }
    }

    .select-arrow {
      position: absolute;
      right: 1rem;
      top: 50%;
      transform: translateY(-50%);
      pointer-events: none;
      color: #6b7280;
      font-size: 0.875rem;
    }
  }
}

.upload-section {
  .btn-upload {
    display: flex;
    align-items: center;
    gap: 0.5rem;
    padding: 0.75rem 1.5rem;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    color: #fff;
    border: none;
    border-radius: 8px;
    font-size: 0.875rem;
    font-weight: 600;
    cursor: pointer;
    transition: all 0.3s ease;

    &:hover {
      transform: translateY(-2px);
      box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
    }

    i {
      font-size: 1.125rem;
    }
  }
}

// Platform Display
.platform-display {
  background-color: #f9fafb;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  padding: 1.5rem;
  margin-bottom: 2rem;

  .display-row {
    display: flex;
    align-items: center;
    gap: 1rem;

    .display-label {
      font-size: 0.875rem;
      font-weight: 600;
      color: #6b7280;
      min-width: 80px;
    }

    .display-value {
      font-size: 0.875rem;
      color: #1f2937;
      padding: 0.5rem 1rem;
      background-color: #fff;
      border: 1px solid #d1d5db;
      border-radius: 6px;
    }
  }
}

// Data Table Section
.data-table-section {
  overflow-x: auto;
}

.data-table {
  width: 100%;
  border-collapse: collapse;

  thead {
    background-color: #f9fafb;

    th {
      padding: 1rem;
      text-align: left;
      font-size: 0.875rem;
      font-weight: 600;
      color: #6b7280;
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
      padding: 1rem;
      font-size: 0.875rem;
      color: #374151;

      &.empty-state {
        text-align: center;
        color: #9ca3af;
        padding: 3rem 1rem;
      }
    }
  }
}

// Responsive
@media (max-width: 768px) {
  .form-section {
    flex-direction: column;
    align-items: stretch;

    .form-group {
      width: 100%;
    }
  }

  .nav-tabs {
    overflow-x: auto;
    -webkit-overflow-scrolling: touch;
  }

  .tab-link {
    white-space: nowrap;
  }
}
</style>
