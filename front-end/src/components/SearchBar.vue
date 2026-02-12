<template>
  <div class="search-container">
    <div class="search-bar">
      <div class="dropdown-area" v-click-outside="closeDropdown">
        <div class="dropdown-trigger" @click.stop="toggleDropdown">
          <span :class="{ 'has-value': selectedOptions.length > 0 }">
            {{ displayLabel }}
          </span>
          <span class="arrow" :class="{ 'is-open': isOpen }"></span>
        </div>

        <div v-if="isOpen" class="dropdown-panel">
          <ul class="options-list">
            <li 
              v-for="item in options" 
              :key="item" 
              @click.stop="toggleOption(item)"
              :class="{ 'active': selectedOptions.includes(item) }"
            >
              <span class="radio-dot"></span>
              {{ item }}
            </li>
          </ul>
          <div class="submit-action" @click.stop="confirmSearch">
            顯示篩選結果
          </div>
        </div>
      </div>

      <div class="vertical-divider"></div>

      <div class="input-group">
        <input 
          type="text" 
          v-model="searchQuery" 
          placeholder="找餐廳、農場、市集"
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
import { ref, computed } from 'vue';

const options = ['親子友善','寵物友善', '蔬食', '有插座'];
const isOpen = ref(false);
const selectedOptions = ref([]); // 變更為陣列以支援複選
const searchQuery = ref('');

// 計算顯示文字：若沒選顯示"選擇條件"，選了顯示項目並用逗號隔開
const displayLabel = computed(() => {
  if (selectedOptions.value.length === 0) return '選擇條件';
  return selectedOptions.value.join(', ');
});

const toggleDropdown = () => {
  isOpen.value = !isOpen.value;
};

const closeDropdown = () => {
  isOpen.value = false;
};

// 複選邏輯：存在就移除，不存在就加入
const toggleOption = (item) => {
  const index = selectedOptions.value.indexOf(item);
  if (index > -1) {
    selectedOptions.value.splice(index, 1);
  } else {
    selectedOptions.value.push(item);
  }
};

const confirmSearch = () => {
  console.log('送出搜尋:', {
    categories: selectedOptions.value,
    keyword: searchQuery.value
  });
  isOpen.value = false;
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
  background: #fff;
  position: relative;
}

.dropdown-area {
  position: relative;
  padding: 0 15px;
  cursor: pointer;
  min-width: 140px; // 稍微加寬以容納複選文字
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

.submit-action {
  background: $gdg-gold;
  color: #fff;
  text-align: center;
  padding: 12px;
  font-weight: bold;
  cursor: pointer;
  &:hover { background: $gdg-gold-dark; }
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