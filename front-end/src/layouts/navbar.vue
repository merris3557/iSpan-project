<script setup>
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { useCartStore } from '@/stores/cart';
import { useAuthStore } from '@/stores/auth';
import { storeRegistrationAPI } from '@/api/storeRegistration';
import Swal from 'sweetalert2';


const router = useRouter();
const cartStore = useCartStore();
const authStore = useAuthStore();
const loading = ref(false);

const goToCart = async () => {
    if (!authStore.isLoggedIn) {
        const result = await Swal.fire({
            title: '請先登入',
            text: '登入後即可將商品加入購物車',
            icon: 'info',
            showCancelButton: true,
            confirmButtonText: '前往登入',
            cancelButtonText: '取消'
        });

        if (result.isConfirmed) {
            localStorage.setItem('shopRedirectPath', '/cart');
            router.push('/login');
        }
        return;
    }
    router.push('/cart');
};


const openStoreInfoModal = async () => {
    router.push('/owner/storeInfo');
    
    loading.value = true;
    try {
        const res = await storeRegistrationAPI.getMyApplications();
        const apps = res.data || res || [];
        
        console.log("My applications:", apps);

        const pendingOrReturned = apps.find(a => a.status === 'PENDING' || a.status === 'RETURNED');
        if (pendingOrReturned) {
            Swal.fire('無法申請', '您目前已經有一筆審核中或被退回的申請案，請至「我的訊息」查看或修改。', 'warning');
            loading.value = false;
            return;
        }

        const approvedApps = apps.filter(a => a.status === 'APPROVED').sort((a,b) => b.id - a.id);
        const latestApproved = approvedApps.length > 0 ? approvedApps[0] : null;

        const defaultName = latestApproved?.name || authStore.user?.name || '';
        const defaultStoreName = latestApproved?.storeName || '';
        const defaultPhone = latestApproved?.phone || '';
        const defaultAddress = latestApproved?.address || '';

        loading.value = false;

        const { value: formValues } = await Swal.fire({
            title: '修改商店基本資料',
            html: `
                <div class="text-start">
                    <div class="mb-3">
                        <label class="form-label fw-bold">負責人姓名 <span class="text-danger">*</span></label>
                        <input id="swal-owner-name" class="form-control" value="${defaultName}">
                    </div>
                    <div class="mb-3">
                        <label class="form-label fw-bold">商家名稱</label>
                        <input id="swal-store-name" class="form-control" value="${defaultStoreName}" placeholder="若無可留空">
                    </div>
                    <div class="mb-3">
                        <label class="form-label fw-bold">商家電話 <span class="text-danger">*</span></label>
                        <input id="swal-store-phone" class="form-control" value="${defaultPhone}">
                    </div>
                    <div class="mb-3">
                        <label class="form-label fw-bold">商家地址 <span class="text-danger">*</span></label>
                        <input id="swal-store-address" class="form-control" value="${defaultAddress}">
                    </div>
                    <div class="alert alert-info mt-3 small mb-0">
                        <i class="bi bi-info-circle me-1"></i> 送出後需經管理員審核才會正式更新資料。<br>
                        審核進度可至「會員中心 > 我的訊息」追蹤。
                    </div>
                </div>
            `,
            focusConfirm: false,
            showCancelButton: true,
            confirmButtonText: '送出修改申請',
            cancelButtonText: '取消',
            confirmButtonColor: '#10b981',
            width: '600px',
            preConfirm: () => {
                const container = Swal.getHtmlContainer();
                const ownerName = container.querySelector('#swal-owner-name').value.trim();
                const storeName = container.querySelector('#swal-store-name').value.trim();
                const storePhone = container.querySelector('#swal-store-phone').value.trim();
                const storeAddress = container.querySelector('#swal-store-address').value.trim();

                if (!ownerName || !storePhone || !storeAddress) {
                    Swal.showValidationMessage('請填寫所有標示星號 (*) 的必填欄位');
                    return false;
                }

                return { ownerName, storeName, storePhone, storeAddress };
            }
        });

        if (formValues) {
            submitUpdateApplication(formValues);
        }

    } catch (error) {
        console.error('Failed to prepare edit modal', error);
        loading.value = false;
        Swal.fire('錯誤', '無法載入現有資料，請稍後再試。', 'error');
    }
};

const submitUpdateApplication = async (data) => {
    try {
        Swal.fire({
            title: '提交中...',
            allowOutsideClick: false,
            didOpen: () => { Swal.showLoading(); }
        });

        await storeRegistrationAPI.submitApplication(data);

        Swal.fire('提交成功', '您的修改申請已送出！管理員審核後即會更新。您可至「我的訊息」查看進度。', 'success');

    } catch (error) {
        console.error('Submit error:', error);
        let errorMsg = '提交失敗，請檢查資料或稍後再試。';
        if (error.response && error.response.data && error.response.data.error) {
             errorMsg = error.response.data.error;
        }
        Swal.fire('錯誤', errorMsg, 'error');
    }
};

const goTo = (path) => {
  router.push(path);
};

const handleLogout = () => {
  cartStore.clearCart();
  authStore.logout();
  router.push('/login');
};

const handleMerchantApplication = () => {
  router.push('/storeRegistration');
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
            <a class="nav-link" href="#" @click.prevent="goTo('/about')">島嶼初心</a>
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

          <li class="nav-item dropdown" v-if="authStore.isStoreUser">
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
              <li><a class="dropdown-item" href="#" @click.prevent="openStoreInfoModal">店家基本資料更新(店名、電話、地址)</a></li>
              <li><a class="dropdown-item" href="#" @click.prevent="goTo('/owner/bookings/seats')">座位數量與時段維護(商家)</a></li>             
              <li><a class="dropdown-item" href="#" @click.prevent="goTo('/owner/bookings/data')">訂位資料管理(商家)</a></li>
            </ul>
          </li>

          <li class="nav-item" v-if="!authStore.isStoreUser">
            <a class="nav-link" href="#" @click.prevent="handleMerchantApplication">申請成為商家</a>
          </li>
        </ul>

        

        <!-- Right Side: Icons & Account -->
        <div class="nav-icons d-flex align-items-center" >
          <!-- 歡迎詞區塊 -->
          <div v-if="authStore.isLoggedIn" class="welcome-msg me-2 text-dark small fw-medium">
            {{ authStore.userName }} 您好
          </div>

          <a class="nav-link position-relative px-3"   title="購物車" @click="goToCart">
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
