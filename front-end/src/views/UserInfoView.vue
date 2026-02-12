<template>
  <div class="user-info-layout">
    <!-- Mobile Toggle Button (Visible only on small screens) -->
    <button class="btn mobile-toggle d-md-none" @click="toggleSidebar">
      <i class="bi bi-list"></i>
    </button>

    <!-- Sidebar -->
    <aside :class="['sidebar-custom', { 'collapsed': isCollapsed, 'mobile-show': isMobileOpen }]">
      <div class="sidebar-header">
        <div class="logo-placeholder" v-show="!isCollapsed">
          <span class="fs-4 fw-bold">Logo</span>
        </div>
        <button class="btn toggle-btn" @click="toggleSidebar">
          <i :class="['bi', isCollapsed ? 'bi-chevron-right' : 'bi-chevron-left']"></i>
        </button>
      </div>

      <hr class="mx-3 my-2 border-secondary">

      <nav class="sidebar-nav px-3">
        <ul class="nav nav-pills flex-column">
          <li v-for="(item, index) in menuItems" :key="index" class="nav-item">
            <a href="#" :class="['nav-link', { 'active': activeItem === index }]" @click.prevent="activeItem = index">
              <i :class="['bi', item.icon, 'me-2']"></i>
              <span class="nav-text" v-show="!isCollapsed">{{ item.text }}</span>
            </a>
          </li>
        </ul>
      </nav>
    </aside>

    <!-- Overlay for mobile -->
    <div v-if="isMobileOpen" class="sidebar-overlay d-md-none" @click="isMobileOpen = false"></div>

    <!-- Main Content -->
    <main class="main-content flex-grow-1" :class="{ 'content-shifted': !isCollapsed }">
      <div class="p-4 bg-light min-vh-100">
        <div class="container-fluid">
          <h2 class="mb-4">內容區域</h2>
          <div class="card card-gdg p-4">
            <p>正在編輯：{{ menuItems[activeItem].text }}</p>
            <p class="text-muted">此處將放置各項目的詳細內容。</p>
          </div>
        </div>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'

const isCollapsed = ref(false)
const isMobileOpen = ref(false)
const activeItem = ref(0)

const menuItems = [
  { text: '個人資料', icon: 'bi-person' },
  { text: '我的訂位', icon: 'bi-calendar-check' },
  { text: '我的訂單', icon: 'bi-receipt' },
  { text: '客服訊息', icon: 'bi-chat-dots' }
]

const toggleSidebar = () => {
  if (window.innerWidth < 768) {
    isMobileOpen.value = !isMobileOpen.value
    isCollapsed.value = false // Ensure it's expanded on mobile when shown
  } else {
    isCollapsed.value = !isCollapsed.value
  }
}

// Handle window resize to reset states if needed
const handleResize = () => {
  if (window.innerWidth >= 768) {
    isMobileOpen.value = false
  }
}

onMounted(() => {
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
})
</script>

<style lang="scss" scoped>
@import '@/assets/styles/custom.scss';

.user-info-layout {
  display: flex;
  min-height: 100vh;
  width: 100%;
  position: relative;
  overflow-x: hidden;
}

// Sidebar Styles
.sidebar-custom {
  width: 280px;
  background-color: #2c2c2c;
  color: white;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  display: flex;
  flex-direction: column;
  z-index: 1000;
  height: 100vh;
  position: sticky;
  top: 0;

  &.collapsed {
    width: 80px;

    .sidebar-header {
      justify-content: center;
      padding: 1.5rem 0.5rem;
    }

    .nav-link {
      text-align: center;
      padding-left: 0;
      padding-right: 0;
      
      i {
        font-size: 1.25rem;
        margin-right: 0 !important;
      }
    }
  }
}

.sidebar-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 1.5rem 1.5rem 1rem;
  min-height: 80px;

  .toggle-btn {
    background: rgba(255, 255, 255, 0.1);
    color: white;
    border: none;
    border-radius: 4px;
    padding: 2px 8px;
    
    &:hover {
      background: $gdg-gold;
    }
  }
}

.sidebar-nav {
  margin-top: 1rem;
}

.nav-link {
  color: rgba(255, 255, 255, 0.8) !important;
  margin-bottom: 0.5rem;
  border-radius: 8px;
  padding: 0.75rem 1rem;
  white-space: nowrap;
  transition: all 0.2s;

  i {
    font-size: 1.1rem;
  }

  &:hover {
    background-color: rgba(255, 255, 255, 0.1);
    color: $gdg-gold !important;
  }

  &.active {
    background-color: $gdg-gold !important;
    color: white !important;
  }
}

// Mobile Specific Styles
.mobile-toggle {
  position: fixed;
  top: 1rem;
  left: 1rem;
  z-index: 1100;
  background-color: #2c2c2c;
  color: white;
  border-radius: 8px;
  box-shadow: 0 2px 5px rgba(0,0,0,0.2);
  
  &:hover {
    background-color: $gdg-gold;
    color: white;
  }
}

.sidebar-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  z-index: 999;
}

@media (max-width: 767.98px) {
  .sidebar-custom {
    position: fixed;
    left: -280px;
    width: 280px;
    
    &.mobile-show {
      left: 0;
    }

    &.collapsed {
      width: 280px; // Reset width on mobile
    }
  }

  .main-content {
    margin-left: 0 !important;
    padding-top: 4rem; // Reserve space for mobile toggle
  }
}

// Animation for text fade
.nav-text {
  transition: opacity 0.2s;
}
</style>
