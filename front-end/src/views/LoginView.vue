<script setup>
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import BaseCard from '@/components/common/BaseCard.vue';
import BaseButton from '@/components/common/BaseButton.vue';
import { authAPI } from '@/api/auth';
import { useAuthStore } from '@/stores/auth';
import { decodeJwtPayload, getRoleFromToken, getSubFromToken, isTokenExpired } from '@/utils/jwt';


const router = useRouter();
const authStore = useAuthStore();
const email = ref('');
const password = ref('');
const isSubmitting = ref(false);

const goToRegister = () => {
  router.push('/register');
};

const handleLogin = async () => {
  if (isSubmitting.value) return;
  
  isSubmitting.value = true;
  const loginData = {
    identifier: email.value,
    password: password.value
  };

  try {
    console.log('Login attempt with:', loginData);
    const response = await authAPI.login(loginData);
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
    authStore.login(response.data.user, accessToken, response.data.refreshToken);
    
    alert(`登入成功！\n你好, ${authStore.userName}\n角色: ${role}`);
    
    // 導向首頁或 dashboard
    // router.push('/');
    
  } catch (error) {
    console.error('Login failed:', error);
    alert(`登入請求失敗 (預計傳送到後端的 JSON):\n${JSON.stringify(loginData, null, 2)}`);
  } finally {
    isSubmitting.value = false;
  }
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

          <BaseButton color="light" size="lg" class="social-btn border w-100 py-2 d-flex align-items-center justify-content-center">
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