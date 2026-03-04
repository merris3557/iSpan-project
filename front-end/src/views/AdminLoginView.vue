<script setup>
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import Swal from 'sweetalert2';
import BaseCard from '@/components/common/BaseCard.vue';
import BaseButton from '@/components/common/BaseButton.vue';
import { adminAPI } from '@/api/admin';
import { useAdminAuthStore } from '@/stores/adminAuth';

const router = useRouter();
const adminAuthStore = useAdminAuthStore();
const account = ref('');
const password = ref('');
const isSubmitting = ref(false);

const handleLogin = async () => {
  if (isSubmitting.value) return;
  
  isSubmitting.value = true;
  
  const loginData = {
    account: account.value,
    password: password.value
  };

  try {
    console.log('Admin login attempt with:', loginData);
    
    const response = await adminAPI.login(loginData);
    console.log('Admin login success:', response);
    
    // 取得 admin 資訊及其 token
    const adminData = response.data.admin;
    const accessToken = response.data.accessToken;
    const refreshToken = response.data.refreshToken;
    
    console.log('========== Admin Info ==========');
    console.log('Account:', adminData.account);
    console.log('Name:', adminData.name);
    console.log('Position:', adminData.position);
    console.log('===============================');
    
    // 使用 Pinia store 進行登入狀態管理
    adminAuthStore.login(adminData, accessToken, refreshToken);
    
    await Swal.fire({
      icon: 'success',
      title: '登入成功！',
      html: `帳號: <b>${adminData.account}</b><br>姓名: ${adminData.name}<br>職位: ${adminData.position}`,
      timer: 1500,
      showConfirmButton: false
    });
    
    // 登入成功後跳轉至後台首頁
    router.push('/admin');
    
  } catch (error) {
    console.error('Admin login failed:', error);
    Swal.fire({
      icon: 'error',
      title: '登入失敗',
      text: '登入失敗，請檢查輸入是否正確',
      confirmButtonColor: '#1e3c72'
    });
  } finally {
    isSubmitting.value = false;
  }
};

</script>

<template>
  <div class="admin-login-page bg-admin-content min-vh-100 d-flex align-items-center justify-content-center p-3">
    <BaseCard class="admin-card" maxWidth="400px" :shadow="false" :border="false">
      <template #header>
        <div class="text-center mb-4">
          <h2 class="text-admin-primary fw-bold h4">後台管理系統</h2>
        </div>
      </template>

      <form @submit.prevent="handleLogin">
        <div class="mb-3">
          <label for="account" class="form-label text-dark fw-medium small">帳號</label>
          <input 
            type="text" 
            id="account" 
            v-model="account" 
            class="form-control admin-form-control"
            placeholder="請輸入管理員帳號" 
            required
          >
        </div>

        <div class="mb-4">
          <label for="password" class="form-label text-dark fw-medium small">密碼</label>
          <input 
            type="password" 
            id="password" 
            v-model="password"
            class="form-control admin-form-control" 
            placeholder="請輸入密碼" 
            required
          >
        </div>

        <div class="d-grid gap-3 pt-2">
          <BaseButton 
            color="primary" 
            class="btn-admin-primary w-100 py-2 fw-bold" 
            @click="handleLogin" 
            :disabled="isSubmitting"
          >
            {{ isSubmitting ? '驗證中...' : '管理員登入' }}
          </BaseButton>

          <div class="text-center mt-2">
            <button type="button" @click="router.push('/admin/forgot-password')" class="btn btn-link text-admin-primary small text-decoration-none fw-medium">
              忘記密碼？
            </button>
          </div>
        </div>
      </form>
      
      <div v-if="false" class="mt-4 pt-3 border-top text-center">
        <!-- 移除註冊功能，僅供參考 -->
        <span class="text-muted small">後台帳號限手動創建</span>
      </div>
    </BaseCard>
  </div>
</template>

<style lang="scss" scoped>
@import '@/assets/styles/custom.scss';

.admin-login-page {
  background-color: #f5f6fa;
}

.admin-logo-icon {
  display: inline-block;
  padding: 10px;
  border-radius: 50%;
  background-color: rgba(30, 60, 114, 0.1);
}

// 覆寫或補充 admin-form-control 的細節 (如果 custom.scss 不夠)
.admin-form-control {
  &:focus {
    border-color: #1e3c72;
    box-shadow: 0 0 0 0.25rem rgba(30, 60, 114, 0.15);
  }
}
</style>
