<template>
  <div class="admin-layout">
    <!-- Sidebar -->
    <aside class="admin-sidebar" :class="{ collapsed: sidebarCollapsed }">
      <div class="sidebar-header">
        <div class="logo-container">
          <i class="bi bi-speedometer2"></i>
          <span v-if="!sidebarCollapsed" class="logo-text">速賺家後台系統</span>
        </div>
      </div>

      <nav class="sidebar-nav">
        <ul class="nav-list">
          <!-- 首頁 -->
          <li class="nav-item">
            <router-link to="/admin" class="nav-link" exact-active-class="active">
              <i class="bi bi-house-door"></i>
              <span v-if="!sidebarCollapsed">首頁</span>
            </router-link>
          </li>

          <!-- 前台首頁管理 -->
          <li class="nav-item has-submenu">
            <a href="#" class="nav-link" @click.prevent="toggleSubmenu('frontend')">
              <i class="bi bi-grid"></i>
              <span v-if="!sidebarCollapsed">前台首頁管理</span>
              <i v-if="!sidebarCollapsed" class="bi bi-chevron-down submenu-arrow" :class="{ rotated: openSubmenu === 'frontend' }"></i>
            </a>
            <ul class="submenu" v-if="!sidebarCollapsed && openSubmenu === 'frontend'">
              <li><router-link to="/admin/frontend/banners" class="submenu-link">輪播圖管理</router-link></li>
              <li><router-link to="/admin/frontend/content" class="submenu-link">內容管理</router-link></li>
            </ul>
          </li>

          <!-- 最新消息管理 -->
          <li class="nav-item has-submenu">
            <a href="#" class="nav-link" @click.prevent="toggleSubmenu('news')">
              <i class="bi bi-megaphone"></i>
              <span v-if="!sidebarCollapsed">最新消息管理</span>
              <i v-if="!sidebarCollapsed" class="bi bi-chevron-down submenu-arrow" :class="{ rotated: openSubmenu === 'news' }"></i>
            </a>
            <ul class="submenu" v-if="!sidebarCollapsed && openSubmenu === 'news'">
              <li><router-link to="/admin/news/list" class="submenu-link">消息列表</router-link></li>
              <li><router-link to="/admin/news/create" class="submenu-link">新增消息</router-link></li>
            </ul>
          </li>

          <!-- 擬案知識管理 -->
          <li class="nav-item has-submenu">
            <a href="#" class="nav-link" @click.prevent="toggleSubmenu('knowledge')">
              <i class="bi bi-journal-text"></i>
              <span v-if="!sidebarCollapsed">擬案知識管理</span>
              <i v-if="!sidebarCollapsed" class="bi bi-chevron-down submenu-arrow" :class="{ rotated: openSubmenu === 'knowledge' }"></i>
            </a>
            <ul class="submenu" v-if="!sidebarCollapsed && openSubmenu === 'knowledge'">
              <li><router-link to="/admin/knowledge/list" class="submenu-link">知識列表</router-link></li>
              <li><router-link to="/admin/knowledge/categories" class="submenu-link">分類管理</router-link></li>
            </ul>
          </li>

          <!-- 商品管理 -->
          <li class="nav-item has-submenu">
            <a href="#" class="nav-link" @click.prevent="toggleSubmenu('products')">
              <i class="bi bi-box-seam"></i>
              <span v-if="!sidebarCollapsed">商品管理</span>
              <i v-if="!sidebarCollapsed" class="bi bi-chevron-down submenu-arrow" :class="{ rotated: openSubmenu === 'products' }"></i>
            </a>
            <ul class="submenu" v-if="!sidebarCollapsed && openSubmenu === 'products'">
              <li><router-link to="/admin/products/list" class="submenu-link">商品列表</router-link></li>
              <li><router-link to="/admin/products/create" class="submenu-link">新增商品</router-link></li>
              <li><router-link to="/admin/products/categories" class="submenu-link">商品分類</router-link></li>
            </ul>
          </li>

          <!-- 銷售管理 -->
          <li class="nav-item has-submenu">
            <a href="#" class="nav-link" @click.prevent="toggleSubmenu('sales')">
              <i class="bi bi-cart"></i>
              <span v-if="!sidebarCollapsed">銷售管理</span>
              <i v-if="!sidebarCollapsed" class="bi bi-chevron-down submenu-arrow" :class="{ rotated: openSubmenu === 'sales' }"></i>
            </a>
            <ul class="submenu" v-if="!sidebarCollapsed && openSubmenu === 'sales'">
              <li><router-link to="/admin/sales/orders" class="submenu-link">訂單管理</router-link></li>
              <li><router-link to="/admin/sales/reports" class="submenu-link">銷售報表</router-link></li>
            </ul>
          </li>

          <!-- anna 商品管理 -->
          <li class="nav-item has-submenu">
            <a href="#" class="nav-link" @click.prevent="toggleSubmenu('products')">
              <i class="bi bi-box-seam"></i>
              <span v-if="!sidebarCollapsed">Anna商品管理</span>
              <i v-if="!sidebarCollapsed" class="bi bi-chevron-down submenu-arrow" :class="{ rotated: openSubmenu === 'products' }"></i>
            </a>
            <ul class="submenu" v-if="!sidebarCollapsed && openSubmenu === 'products'">
              <li><router-link to="/admin/backEnd/productsList" class="submenu-link">商品管理</router-link></li>
              <li><router-link to="/admin/backEnd/productsStock" class="submenu-link">庫存管理</router-link></li>
              
            </ul>
          </li>


          <!-- 使用者管理 -->
          <li class="nav-item has-submenu">
            <a href="#" class="nav-link" @click.prevent="toggleSubmenu('users')">
              <i class="bi bi-people"></i>
              <span v-if="!sidebarCollapsed">使用者管理</span>
              <i v-if="!sidebarCollapsed" class="bi bi-chevron-down submenu-arrow" :class="{ rotated: openSubmenu === 'users' }"></i>
            </a>
            <ul class="submenu" v-if="!sidebarCollapsed && openSubmenu === 'users'">
              <li><router-link to="/admin/users/list" class="submenu-link">使用者列表</router-link></li>
              <li><router-link to="/admin/users/storeRegistration" class="submenu-link">商家註冊審核</router-link></li>
            </ul>
          </li>
        </ul>
      </nav>

      <!-- Collapse Toggle Button -->
      <div class="sidebar-footer">
        <button class="collapse-btn" @click="toggleSidebar">
          <i class="bi" :class="sidebarCollapsed ? 'bi-chevron-right' : 'bi-chevron-left'"></i>
        </button>
      </div>
    </aside>

    <!-- Main Content Area -->
    <div class="admin-main" :class="{ expanded: sidebarCollapsed }">
      <!-- Top Header -->
      <header class="admin-header">
        <div class="header-left">
          <button class="menu-toggle" @click="toggleSidebar">
            <i class="bi bi-list"></i>
          </button>
          <!-- Breadcrumb -->
          <nav class="breadcrumb-nav">
            <ol class="breadcrumb">
              <li class="breadcrumb-item"><a href="#">首頁</a></li>
              <li class="breadcrumb-item" v-if="currentSection"><a href="#">{{ currentSection }}</a></li>
              <li class="breadcrumb-item active" v-if="currentPage">{{ currentPage }}</li>
            </ol>
          </nav>
        </div>

        <div class="header-right">
          <div class="user-menu">
            <img src="https://via.placeholder.com/40" alt="User" class="user-avatar">
            <span class="user-name">管理員</span>
            <i class="bi bi-chevron-down"></i>
          </div>
        </div>
      </header>

      <!-- Page Content -->
      <main class="admin-content">
        <router-view></router-view>
      </main>
    </div>
  </div>
</template>

<script>
import { ref, computed } from 'vue';
import { useRoute } from 'vue-router';

export default {
  name: 'AdminLayout',
  setup() {
    const route = useRoute();
    const sidebarCollapsed = ref(false);
    const openSubmenu = ref(null);

    const toggleSidebar = () => {
      sidebarCollapsed.value = !sidebarCollapsed.value;
      if (sidebarCollapsed.value) {
        openSubmenu.value = null;
      }
    };

    const toggleSubmenu = (menu) => {
      openSubmenu.value = openSubmenu.value === menu ? null : menu;
    };

    const currentSection = computed(() => {
      const path = route.path;
      if (path.includes('/admin/frontend')) return '前台首頁管理';
      if (path.includes('/admin/news')) return '最新消息管理';
      if (path.includes('/admin/knowledge')) return '擬案知識管理';
      if (path.includes('/admin/products')) return '商品管理';
      if (path.includes('/admin/sales')) return '銷售管理';
      if (path.includes('/admin/users')) return '使用者管理';
      return '';
    });

    const currentPage = computed(() => {
      const path = route.path;
      if (path.includes('/list')) return '列表';
      if (path.includes('/create')) return '新增';
      if (path.includes('/categories')) return '分類管理';
      if (path.includes('/orders')) return '訂單管理';
      if (path.includes('/reports')) return '報表';
      if (path.includes('/admin/users/list')) return '使用者列表';
      return 'ERP格式轉換';
    });

    return {
      sidebarCollapsed,
      openSubmenu,
      toggleSidebar,
      toggleSubmenu,
      currentSection,
      currentPage
    };
  }
};
</script>

<style lang="scss" scoped>
@import '@/assets/styles/custom.scss';

.admin-layout {
  display: flex;
  min-height: 100vh;
  background-color: #f5f6fa;
}

// Sidebar Styles
.admin-sidebar {
  width: 250px;
  background: linear-gradient(180deg, #1e3c72 0%, #2a5298 100%);
  color: #fff;
  display: flex;
  flex-direction: column;
  transition: all 0.3s ease;
  position: fixed;
  left: 0;
  top: 0;
  height: 100vh;
  box-shadow: 2px 0 8px rgba(0, 0, 0, 0.1);
  z-index: 1000;

  &.collapsed {
    width: 70px;

    .logo-text {
      display: none;
    }

    .nav-link span {
      display: none;
    }

    .submenu-arrow {
      display: none;
    }
  }
}

.sidebar-header {
  padding: 1.5rem 1rem;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);

  .logo-container {
    display: flex;
    align-items: center;
    gap: 0.75rem;

    i {
      font-size: 1.5rem;
      color: #fff;
    }

    .logo-text {
      font-size: 1.1rem;
      font-weight: 600;
      white-space: nowrap;
    }
  }
}

.sidebar-nav {
  flex: 1;
  overflow-y: auto;
  padding: 1rem 0;

  &::-webkit-scrollbar {
    width: 6px;
  }

  &::-webkit-scrollbar-track {
    background: rgba(255, 255, 255, 0.05);
  }

  &::-webkit-scrollbar-thumb {
    background: rgba(255, 255, 255, 0.2);
    border-radius: 3px;
  }
}

.nav-list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.nav-item {
  margin-bottom: 0.25rem;
}

.nav-link {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 0.875rem 1.25rem;
  color: rgba(255, 255, 255, 0.85);
  text-decoration: none;
  transition: all 0.2s ease;
  position: relative;

  i {
    font-size: 1.25rem;
    min-width: 20px;
  }

  span {
    flex: 1;
  }

  .submenu-arrow {
    font-size: 0.875rem;
    transition: transform 0.3s ease;

    &.rotated {
      transform: rotate(180deg);
    }
  }

  &:hover {
    background-color: rgba(255, 255, 255, 0.1);
    color: #fff;
  }

  &.active {
    background-color: rgba(255, 255, 255, 0.15);
    color: #fff;
    border-left: 3px solid #ffd700;
  }
}

.submenu {
  list-style: none;
  padding: 0;
  margin: 0;
  background-color: rgba(0, 0, 0, 0.1);

  li {
    margin: 0;
  }

  .submenu-link {
    display: block;
    padding: 0.625rem 1.25rem 0.625rem 3.25rem;
    color: rgba(255, 255, 255, 0.75);
    text-decoration: none;
    font-size: 0.9rem;
    transition: all 0.2s ease;

    &:hover {
      background-color: rgba(255, 255, 255, 0.05);
      color: #fff;
    }

    &.router-link-active {
      color: #ffd700;
      background-color: rgba(255, 255, 255, 0.05);
    }
  }
}

.sidebar-footer {
  padding: 1rem;
  border-top: 1px solid rgba(255, 255, 255, 0.1);

  .collapse-btn {
    width: 100%;
    padding: 0.5rem;
    background-color: rgba(255, 255, 255, 0.1);
    border: none;
    color: #fff;
    border-radius: 4px;
    cursor: pointer;
    transition: all 0.2s ease;

    &:hover {
      background-color: rgba(255, 255, 255, 0.2);
    }

    i {
      font-size: 1.25rem;
    }
  }
}

// Main Content Area
.admin-main {
  flex: 1;
  margin-left: 250px;
  display: flex;
  flex-direction: column;
  transition: margin-left 0.3s ease;

  &.expanded {
    margin-left: 70px;
  }
}

.admin-header {
  height: 64px;
  background-color: #fff;
  border-bottom: 1px solid #e5e7eb;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 1.5rem;
  position: sticky;
  top: 0;
  z-index: 100;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);

  .header-left {
    display: flex;
    align-items: center;
    gap: 1.5rem;
  }

  .menu-toggle {
    background: none;
    border: none;
    font-size: 1.5rem;
    color: #6b7280;
    cursor: pointer;
    padding: 0.5rem;
    display: none;

    &:hover {
      color: #1e3c72;
    }

    @media (max-width: 768px) {
      display: block;
    }
  }
}

// Breadcrumb
.breadcrumb-nav {
  .breadcrumb {
    display: flex;
    align-items: center;
    list-style: none;
    padding: 0;
    margin: 0;
    gap: 0.5rem;
    font-size: 0.875rem;
  }

  .breadcrumb-item {
    display: flex;
    align-items: center;

    &:not(:last-child)::after {
      content: '/';
      margin-left: 0.5rem;
      color: #9ca3af;
    }

    a {
      color: #6b7280;
      text-decoration: none;

      &:hover {
        color: #1e3c72;
      }
    }

    &.active {
      color: #1e3c72;
      font-weight: 500;
    }
  }
}

// Header Right
.header-right {
  display: flex;
  align-items: center;
  gap: 1rem;

  .user-menu {
    display: flex;
    align-items: center;
    gap: 0.75rem;
    padding: 0.5rem 1rem;
    border-radius: 8px;
    cursor: pointer;
    transition: background-color 0.2s ease;

    &:hover {
      background-color: #f9fafb;
    }

    .user-avatar {
      width: 40px;
      height: 40px;
      border-radius: 50%;
      object-fit: cover;
    }

    .user-name {
      font-size: 0.875rem;
      font-weight: 500;
      color: #374151;
    }

    i {
      font-size: 0.75rem;
      color: #9ca3af;
    }
  }
}

// Main Content
.admin-content {
  flex: 1;
  padding: 1.5rem;
  overflow-y: auto;
}

// Responsive
@media (max-width: 768px) {
  .admin-sidebar {
    transform: translateX(-100%);

    &.mobile-open {
      transform: translateX(0);
    }
  }

  .admin-main {
    margin-left: 0;
  }
}
</style>
