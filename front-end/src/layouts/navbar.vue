<script setup>
import { useRouter } from 'vue-router';
import { useCartStore } from '@/stores/cart';
import { useAuthStore } from '@/stores/auth';


const router = useRouter();
const cartStore = useCartStore();
const authStore = useAuthStore();

const goTo = (path) => {
  router.push(path);
};

const handleLogout = () => {
  authStore.logout();
  router.push('/login');
};

</script>

<template>
  <nav class="navbar navbar-expand-lg navbar-light navbar-gdg sticky-top">
    <div class="container-fluid">
      <!-- Navbar Brand / Home (Distinct Item) -->
      <div class="nav-item-home">
        <a class="nav-link" href="#" @click.prevent="goTo('/')">
          <i class="bi bi-house-door-fill me-2"></i>首頁
        </a>
      </div>

      <button 
        class="navbar-toggler border-0" 
        type="button" 
        data-bs-toggle="collapse" 
        data-bs-target="#gdgNavbar" 
        aria-controls="gdgNavbar" 
        aria-expanded="false" 
        aria-label="Toggle navigation"
      >
        <span class="navbar-toggler-icon"></span>
      </button>

      <div class="collapse navbar-collapse" id="gdgNavbar">
        <ul class="navbar-nav me-auto mb-2 mb-lg-0">
          <!-- Template 1: Simple Button -->
          <li class="nav-item">
            <a class="nav-link" href="#" @click.prevent="goTo('/about')">關於我們</a>
          </li>

          <!-- Template 2: Hover Dropdown -->
    

          <!-- <li class="nav-item dropdown">
            <a 
              class="nav-link dropdown-toggle" 
              href="#" 
              id="navbarDropdown2" 
              role="button" 
              data-bs-toggle="dropdown" 
              aria-expanded="false"
            >
              bbb
            </a>
            <ul class="dropdown-menu border-top-0" aria-labelledby="navbarDropdown2">
              <li><a class="dropdown-item" href="#">1</a></li>
              <li><a class="dropdown-item" href="#">2</a></li>
            </ul>
          </li> -->

          <li class="nav-item">
            <a class="nav-link" href="#"  @click.prevent="goTo('/mapSearch')">美味座標</a>
          </li>

          <li class="nav-item">
            <a class="nav-link" href="#" @click.prevent="goTo('/shopStore')">質感選物</a>
          </li>

          <li class="nav-item dropdown">
            <a 
              class="nav-link dropdown-toggle" 
              href="#" 
              id="navbarDropdown1" 
              role="button" 
              data-bs-toggle="dropdown" 
              aria-expanded="false"
            >
              商家編輯
            </a>
            <ul class="dropdown-menu border-top-0" aria-labelledby="navbarDropdown1">
              <li><a class="dropdown-item" href="#" @click.prevent="goTo('/owner/storeInfo')">店家資訊編輯頁面(商家)</a></li>
              <li><a class="dropdown-item" href="#" @click.prevent="goTo('/owner/bookings/seats')">座位數量與時段維護(商家)</a></li>             
              <li><a class="dropdown-item" href="#" @click.prevent="goTo('/owner/bookings/data')">訂位資料管理(商家)</a></li>
            </ul>
          </li>

          <li class="nav-item">
            <a class="nav-link" href="#" @click.prevent="goTo('/storeRegistration')">申請成為商家</a>
          </li>
        </ul>

        

        <!-- Right Side: Icons & Account -->
        <div class="nav-icons d-flex align-items-center" >
          <!-- 歡迎詞區塊 -->
          <div v-if="authStore.isLoggedIn" class="welcome-msg me-2 text-dark small fw-medium">
            {{ authStore.userName }} 您好
          </div>

          <a class="nav-link position-relative px-3"  title="購物車" @click="router.push('/cart')">
            <i class="bi bi-cart3"></i>
            <span class="position-absolute top-25 start-75 translate-middle badge rounded-pill bg-danger" style="font-size: 0.6rem;">
              {{ cartStore.totalQuantity }}
            </span>
          </a>
          
          <div class="nav-item dropdown">
            <a 
              class="nav-link px-3" 
              href="#" 
              id="accountDropdown" 
              role="button" 
              data-bs-toggle="dropdown" 
              aria-expanded="false"
            >
              <i class="bi bi-person-circle"></i>
            </a>
            <ul class="dropdown-menu dropdown-menu-end" aria-labelledby="accountDropdown">
              <template v-if="!authStore.isLoggedIn">
                <li><a class="dropdown-item" href="#" @click.prevent="goTo('/login')">會員登入</a></li>
                <li><a class="dropdown-item" href="#" @click.prevent="goTo('/register')">加入會員</a></li>
              </template>
              <template v-else>
                <li><a class="dropdown-item" href="#" @click.prevent="handleLogout">登出</a></li>
              </template>
              <li><hr class="dropdown-divider"></li>
              <li><a class="dropdown-item" href="#" @click.prevent="goTo('/userInfo')">會員中心</a></li>
            </ul>
          </div>
        </div>
      </div>
    </div>
  </nav>
</template>

<style scoped>
/* Inherits most styles from custom.scss through navbar-gdg class */
.navbar-toggler:focus {
  box-shadow: none;
}
</style>