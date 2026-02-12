<script setup>
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import BaseCard from '@/components/common/BaseCard.vue';
import BaseButton from '@/components/common/BaseButton.vue';
import { authAPI } from '@/api/auth';

const router = useRouter();

const email = ref('');
const password = ref('');
const isSubmitting = ref(false);

const handleRegister = async () => {
  if (isSubmitting.value) return;

  isSubmitting.value = true;
  const registerData = {
    email: email.value,
    password: password.value
    // Add other fields as needed for real registration
  };

  try {
    console.log('Register attempt with:', registerData);
    const response = await authAPI.register(registerData);
    console.log('Register success:', response);
    alert("註冊成功");
    router.push('/login');
  } catch (error) {
    console.error('Register failed:', error);
    alert(`註冊請求失敗 (預計傳送到後端的 JSON):\n${JSON.stringify(registerData, null, 2)}`);
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
        <BaseButton color="light" size="md" class="social-btn border w-100 py-2 d-flex align-items-center justify-content-center">
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
