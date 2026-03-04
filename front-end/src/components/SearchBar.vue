<template>
  <div class="search-container">
    <div class="search-bar">
      <div class="dropdown-area" v-click-outside="closeDropdown">
        <div class="dropdown-trigger" @click.stop="toggleDropdown">
          <span :class="{ 'has-value': selectedIds.length > 0 }">
            {{ displayLabel }}
          </span>
          <span class="arrow" :class="{ 'is-open': isOpen }"></span>
        </div>

        <div v-if="isOpen" class="dropdown-panel">
          <!-- 載入中 -->
          <div v-if="loadingCategories" class="loading-hint">載入中...</div>
          <!-- 標籤選項 -->
          <ul v-else class="options-list">
            <li
              v-for="item in categories"
              :key="item.categoryId"
              @click.stop="toggleOption(item)"
              :class="{ 'active': selectedIds.includes(item.categoryId) }"
            >
              <span class="radio-dot"></span>
              {{ item.categoryName }}
            </li>
          </ul>
          <!-- 「顯示篩選結果」按鈕已移除，統一由右側放大鏡觸發搜尋 -->
        </div>
      </div>

      <div class="vertical-divider"></div>

      <div class="input-group">
        <input
          type="text"
          v-model="searchQuery"
          placeholder="尋找美味"
          @keyup.enter="confirmSearch"
        />
        <button class="search-btn" @click="confirmSearch">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <circle cx="11" cy="11" r="8"></circle>
            <line x1="21" y1="21" x2="16.65" y2="16.65"></line>
          </svg>
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { useMapSearchStore } from '@/stores/mapSearchStore';
import storeAPI from '@/api/store';

const mapSearchStore = useMapSearchStore();

// ─── 標籤資料（從資料庫動態載入）─────────────────────────────────────────────
const categories = ref([]);          // [{ categoryId, categoryName }, ...]
const loadingCategories = ref(false);

const fetchCategories = async () => {
  loadingCategories.value = true;
  try {
    const response = await storeAPI.getAllCategories();
    if (response && response.success) {
      categories.value = response.data;
    }
  } catch (error) {
    console.error('載入標籤失敗:', error);
  } finally {
    loadingCategories.value = false;
  }
};

onMounted(() => {
  fetchCategories();
});

// ─── 選取狀態（存 categoryId 整數）──────────────────────────────────────────
const isOpen = ref(false);
const selectedIds = ref([]);    // 已選的 categoryId 陣列
const searchQuery = ref('');

// 計算顯示文字：未選顯示「選擇條件」，選了顯示名稱用逗號隔開
const displayLabel = computed(() => {
  if (selectedIds.value.length === 0) return '選擇條件';
  return categories.value
    .filter(c => selectedIds.value.includes(c.categoryId))
    .map(c => c.categoryName)
    .join(', ');
});

const toggleDropdown = () => {
  isOpen.value = !isOpen.value;
};

const closeDropdown = () => {
  isOpen.value = false;
};

// 複選邏輯：存在就移除，不存在就加入（操作 categoryId）
const toggleOption = (item) => {
  const index = selectedIds.value.indexOf(item.categoryId);
  if (index > -1) {
    selectedIds.value.splice(index, 1);
  } else {
    selectedIds.value.push(item.categoryId);
  }
};

// 呼叫後端搜尋 API（傳 keyword + categoryIds）
const confirmSearch = async () => {
  isOpen.value = false;
  await mapSearchStore.searchStores(searchQuery.value, selectedIds.value);
};

// 點擊外部關閉指令
const vClickOutside = {
  mounted(el, binding) {
    el.clickOutsideEvent = (event) => {
      if (!(el === event.target || el.contains(event.target))) {
        binding.value();
      }
    };
    document.addEventListener('click', el.clickOutsideEvent);
  },
  unmounted(el) {
    document.removeEventListener('click', el.clickOutsideEvent);
  },
};
</script>

<style lang="scss" scoped>
// 變數延用
$gdg-gold: #9f9572;
$gdg-gold-dark: #776f54;

.search-bar {
  display: flex;
  align-items: center;
  border: 1px solid $gdg-gold;
  border-radius: 4px;
  height: 48px;
  max-width: 650px;
  margin: 20px auto;
  padding: 0 10px;
  background: transparent;
  position: relative;
}

.dropdown-area {
  position: relative;
  padding: 0 15px;
  cursor: pointer;
  min-width: 140px;
  user-select: none;

  .dropdown-trigger {
    display: flex;
    align-items: center;
    justify-content: space-between;
    color: $gdg-gold-dark;
    font-size: 16px;

    span {
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
      max-width: 150px;
    }

    .arrow {
      width: 0;
      height: 0;
      border-left: 5px solid transparent;
      border-right: 5px solid transparent;
      border-top: 6px solid $gdg-gold;
      margin-left: 10px;
      transition: transform 0.3s;
      &.is-open { transform: rotate(180deg); }
    }
  }
}

.dropdown-panel {
  position: absolute;
  top: calc(100% + 10px);
  left: 0;
  width: 220px;
  background: #fff;
  border: 1px solid #eee;
  box-shadow: 0 10px 25px rgba(0,0,0,0.1);
  z-index: 9999;
  border-radius: 4px;
}

.loading-hint {
  padding: 12px 20px;
  color: #aaa;
  font-size: 14px;
  text-align: center;
}

.options-list {
  list-style: none;
  padding: 0;
  margin: 0;

  li {
    padding: 12px 20px;
    display: flex;
    align-items: center;
    color: #666;
    transition: background 0.2s;
    cursor: pointer;

    &:hover { background: rgba(160, 150, 115, 0.05); }

    .radio-dot {
      width: 14px;
      height: 14px;
      border: 1px solid #ccc;
      border-radius: 50%;
      margin-right: 12px;
      position: relative;
      flex-shrink: 0;
    }

    &.active {
      color: $gdg-gold;
      .radio-dot {
        border-color: $gdg-gold;
        background: $gdg-gold;
        // 內圈白色小點增加複選感
        &::after {
          content: '';
          position: absolute;
          top: 3px;
          left: 3px;
          width: 6px;
          height: 6px;
          background: #fff;
          border-radius: 50%;
        }
      }
    }
  }
}

.vertical-divider {
  width: 1px;
  height: 24px;
  background: $gdg-gold;
  opacity: 0.3;
}

.input-group {
  flex: 1;
  display: flex;
  align-items: center;
  padding-left: 10px;

  input {
    flex: 1;
    border: none;
    outline: none;
    padding: 8px;
    font-size: 16px;
    background: transparent;
    color: #333;
    &::placeholder { color: #ccc; }
  }

  .search-btn {
    background: none;
    border: none;
    color: $gdg-gold;
    cursor: pointer;
    padding: 5px;
  }
}
</style>