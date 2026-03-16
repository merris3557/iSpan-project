<script setup>
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import Swal from 'sweetalert2';
import BaseCard from '@/components/common/BaseCard.vue';
import BaseButton from '@/components/common/BaseButton.vue';
import { authAPI } from '@/api/auth';

const router = useRouter();

const email = ref('');
const password = ref('');
const isSubmitting = ref(false);

const handleGoogleLogin = () => {
  window.location.href = 'http://localhost:8080/oauth2/authorization/google';
};

const handleRegister = async () => {
  if (isSubmitting.value) return;

  // 1. 檢查信箱格式
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  if (!emailRegex.test(email.value)) {
    Swal.fire({
      icon: 'error',
      title: '格式錯誤',
      text: '帳號格式有誤',
      confirmButtonColor: '#9f9572'
    });
    return;
  }

  // 2. 檢查密碼格式 (至少8碼，包含大小寫、數字、特殊符號)
  const passwordRegex = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&])[A-Za-z\d@$!%*?&]{8,}$/;
  if (!passwordRegex.test(password.value)) {
    Swal.fire({
      icon: 'error',
      title: '格式錯誤',
      text: '密碼格式有誤',
      confirmButtonColor: '#9f9572'
    });
    return;
  }

  isSubmitting.value = true;
  const registerData = {
    email: email.value,
    password: password.value
    // Add other fields as needed for real registration
  };

  try {
    const response = await authAPI.register(registerData);
    
    await Swal.fire({
      icon: 'success',
      title: '註冊成功',
      text: '即將為您重新導向至登入頁面',
      timer: 2000,
      showConfirmButton: false
    });
    
    router.push('/login');
  } catch (error) {
    console.error('Register failed:', error);
    
    let errorMsg = error.response?.data?.message || '註冊失敗，請確認密碼是否符合規定，或該信箱已註冊過';
    
    // Check if there are specific validation errors from the backend (MethodArgumentNotValidException)
    if (error.response?.data?.data && typeof error.response.data.data === 'object') {
      const validationErrors = Object.values(error.response.data.data);
      if (validationErrors.length > 0) {
        errorMsg = validationErrors.join('<br>');
      }
    }

    Swal.fire({
      icon: 'error',
      title: '註冊失敗',
      html: errorMsg,
      confirmButtonColor: '#9f9572'
    });
  } finally {
    isSubmitting.value = false;
  }
};

const goToLogin = () => {
  router.push('/login');
};
</script>

<template>
  <div class="register-page bg-gdg-light min-vh-100 d-flex align-items-center justify-content-center p-3">
    <BaseCard maxWidth="450px" :shadow="true" :hoverEffect="false">
      <template #header>
        <div class="text-center mb-4">
          <h2 class="text-gdg fw-bold">加入會員</h2>
        </div>
      </template>

      <div class="d-grid gap-3 pt-2">
        <BaseButton color="light" size="md" type="button" @click="handleGoogleLogin" class="social-btn border w-100 py-2 d-flex align-items-center justify-content-center">
          <img src="https://www.gstatic.com/images/branding/product/1x/gsa_512dp.png" alt="Google" width="20" class="me-2">
          <span class="small fw-medium text-dark" style="font-size: 14px;">透過 Google 註冊</span>
        </BaseButton>

        <div class="divider-container my-3">
          <span class="divider-text">或</span>
        </div>
      </div>

      <form @submit.prevent="handleRegister">
        <div class="mb-3">
          <label for="email" class="form-label text-dark fw-medium small">電子郵件</label>
          <input 
            type="email" 
            id="email" 
            v-model="email" 
            class="form-control form-control-lg custom-input" 
            placeholder="請輸入電子郵件"
            required
          >
        </div>
        
        <div class="mb-4">
          <label for="password" class="form-label text-dark fw-medium small">密碼</label>
          <input 
            type="password" 
            id="password" 
            v-model="password" 
            class="form-control form-control-lg custom-input" 
            placeholder="請輸入密碼"
            required
          >
          <div class="form-text text-muted mt-1" style="font-size: 0.8rem;">
            *密碼需至少 8 個字元，並包含大小寫英文字母、數字及特殊符號
          </div>
        </div>

        <div class="d-grid gap-3 pt-2">
          <BaseButton 
            color="gdg" 
            size="md" 
            @click="handleRegister"
            :disabled="isSubmitting"
            class="fw-bold py-2"
          >
            <span style="font-size: 12px;">{{ isSubmitting ? '註冊中...' : '註冊' }}</span>
          </BaseButton>
          
          <div class="text-center mt-3">
            <span class="text-muted small">已經有帳號了？</span>
            <a href="#" @click.prevent="goToLogin" class="text-primary small fw-bold text-decoration-none ms-1">登入</a>
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
  padding: 0 15px;
  font-size: 0.8rem;
  font-weight: 500;
  color: #999;
  text-transform: uppercase;
}

.social-btn {
  background-color: #fff;
  border-radius: 8px !important;
  transition: background-color 0.2s;
}

.social-btn:hover {
  background-color: #f8f9fa;
}

.bg-gdg-light {
    background-color: rgba(160, 150, 115, 0.05) !important;
}
</style>
