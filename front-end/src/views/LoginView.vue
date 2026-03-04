<script setup>
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import Swal from 'sweetalert2';
import BaseCard from '@/components/common/BaseCard.vue';
import BaseButton from '@/components/common/BaseButton.vue';
import { authAPI } from '@/api/auth';
import { useAuthStore } from '@/stores/auth';
import { decodeJwtPayload, getRoleFromToken, getSubFromToken, isTokenExpired } from '@/utils/jwt';

import { useCartStore } from '@/stores/cart'


const router = useRouter();
const authStore = useAuthStore();
const email = ref('');
const password = ref('');
const isSubmitting = ref(false);

const goToRegister = () => {
  router.push('/register');
};

const handleGoogleLogin = () => {
  window.location.href = 'http://localhost:8080/oauth2/authorization/google';
};

const doLogin = async (dataToSubmit) => {
  try {
    console.log('Login attempt with:', dataToSubmit);
    const response = await authAPI.login(dataToSubmit);
    console.log('Login success:', response);
    
    // ========== 解析 JWT Token ==========
    // 注意: axios interceptor 已經回傳 response.data，所以這裡的 response 就是 {success, message, data}
    const accessToken = response.data.accessToken;
    
    // 方法 1: 取得完整 payload
    const payload = decodeJwtPayload(accessToken);
    console.log('========== JWT Payload ==========');
    console.log('Full Payload:', payload);
    
    // 方法 2: 使用便捷函數取得特定欄位
    const role = getRoleFromToken(accessToken);
    const sub = getSubFromToken(accessToken);
    const expired = isTokenExpired(accessToken);
    
    console.log('Role:', role);
    console.log('Sub (Email):', sub);
    console.log('Is Expired:', expired);
    console.log('=================================');
    
    // 使用 Auth Store 儲存登入資訊
    authStore.login(response.data.user, accessToken, response.data.refreshToken)

    //為了登入後立即更新並顯示購物車icon的數字
    const cartStore = useCartStore()
    console.log('準備 fetchCart')  
    await cartStore.fetchCart()
    console.log('fetchCart 完成', cartStore.items) 

    
    await Swal.fire({
      icon: 'success',
      title: '登入成功！',
      html: `你好, <b>${authStore.userName}</b><br>角色: ${role}`,
      timer: 1500,
      showConfirmButton: false
    });
    
    // 導向首頁或 dashboard+
    router.push('/');
    
  } catch (error) {
    console.error('Login failed:', error);
    const errorMsg = error.response?.data?.message || '登入失敗，請重新嘗試';
    
    if (errorMsg.includes('2FA code is required') || errorMsg.includes('Invalid 2FA code')) {
      // 觸發 2FA 輸入框
      const { value: code } = await Swal.fire({
        title: '雙因素驗證 (2FA)',
        input: 'text',
        inputLabel: '請輸入 Authenticator App 上的 6 位數驗證碼',
        inputPlaceholder: '例如：123456',
        inputValue: '',
        showCancelButton: true,
        confirmButtonText: '驗證',
        cancelButtonText: '取消',
        confirmButtonColor: '#9f9572',
        inputValidator: (value) => {
          if (!value || value.length !== 6 || !/^\d+$/.test(value)) {
            return '請輸入有效的 6 位數字驗證碼';
          }
        }
      });

      if (code) {
        // 使用含 2FA 的資料重新登入
        dataToSubmit.twoFactorCode = code.trim();
        return doLogin(dataToSubmit);
      } else {
        // 使用者取消輸入，重登出狀態
        isSubmitting.value = false;
        return;
      }
    }

    Swal.fire({
      icon: 'error',
      title: '登入失敗',
      text: errorMsg,
      confirmButtonColor: '#9f9572'
    });
    isSubmitting.value = false;
  }
};

const handleLogin = async () => {
  if (isSubmitting.value) return;
  isSubmitting.value = true;
  await doLogin({
    identifier: email.value,
    password: password.value
  });
};
</script>

<template>
  <div class="login-page bg-gdg-light min-vh-100 d-flex align-items-center justify-content-center p-3">
    <BaseCard maxWidth="450px" :shadow="true" :hoverEffect="false">
      <template #header>
        <div class="text-center mb-4">
          <h2 class="text-gdg fw-bold">會員登入</h2>
        </div>
      </template>

      <form @submit.prevent="handleLogin">
        <div class="mb-3">
          <label for="email" class="form-label text-dark fw-medium small">電子郵件或使用者名稱</label>
          <input type="email" id="email" v-model="email" class="form-control form-control-lg custom-input"
            placeholder="" required>
        </div>

        <div class="mb-4">
          <div class="d-flex justify-content-between align-items-center mb-1">
            <label for="password" class="form-label text-dark fw-medium small mb-0">密碼</label>
            <router-link to="/forgot-password" class="text-primary small text-decoration-none">忘記密碼？</router-link>
          </div>
          <input type="password" id="password" v-model="password"
            class="form-control form-control-lg custom-input" placeholder="" required>
        </div>

        <div class="d-grid gap-3 pt-2">
          <BaseButton color="gdg" size="md" @click="handleLogin" :disabled="isSubmitting" class="fw-bold py-2">
            <span style="font-size: 12px;">{{ isSubmitting ? '登入中...' : '登入' }}</span>
          </BaseButton>

          <div class="divider-container my-2">
            <span class="divider-text">或</span>
          </div>

          <BaseButton color="light" size="lg" type="button" @click="handleGoogleLogin" class="social-btn border w-100 py-2 d-flex align-items-center justify-content-center">
            <img src="https://www.gstatic.com/images/branding/product/1x/gsa_512dp.png" alt="Google" width="20" class="me-2">
            <span class="small fw-medium text-dark" style="font-size: 12px;">使用 Google 帳號登入</span>
          </BaseButton>

          <div class="text-center mt-3">
            <span class="text-muted small">尚未擁有帳號？</span>
            <a href="#" @click.prevent="goToRegister" class="text-gdg small fw-bold text-decoration-none ms-1">註冊會員</a>
          </div>
        </div>
      </form>
    </BaseCard>
  </div>
</template>

<style scoped>
.custom-input {
  border-radius: 8px;
  border: 1px solid #ced4da;
  font-size: 0.95rem;
  padding: 0.6rem 1rem;
}

.custom-input:focus {
  border-color: #9f9572;
  box-shadow: 0 0 0 0.2rem rgba(159, 149, 114, 0.1);
}

.divider-container {
  display: flex;
  align-items: center;
  text-align: center;
  color: #adb5bd;
}

.divider-container::before,
.divider-container::after {
  content: '';
  flex: 1;
  border-bottom: 1px solid #dee2e6;
}

.divider-text {
  padding: 0 10px;
  font-size: 0.85rem;
}

.social-btn {
  background-color: #f8f9fa;
  border-radius: 8px !important;
  transition: background-color 0.2s;
}

.social-btn:hover {
  background-color: #e9ecef;
}

.text-gdg {
  color: #9f9572 !important;
}
</style>