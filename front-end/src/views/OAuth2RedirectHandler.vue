<template>
  <div class="oauth-redirect min-vh-100 d-flex align-items-center justify-content-center bg-light">
    <div class="text-center">
      <div class="spinner-border text-primary" role="status" style="width: 3rem; height: 3rem;">
        <span class="visually-hidden">Loading...</span>
      </div>
      <h4 class="mt-3 text-muted fw-bold">正在處理登入授權，請稍候...</h4>
    </div>
  </div>
</template>

<script setup>
import { onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useAuthStore } from '@/stores/auth';
import { userAPI } from '@/api/user';
import { authAPI } from '@/api/auth';
import Swal from 'sweetalert2';

const route = useRoute();
const router = useRouter();
const authStore = useAuthStore();

onMounted(async () => {
  // 處理帳號封鎖或其他 OAuth2 登入失敗的情況
  const errorMsg = route.query.error;
  if (errorMsg) {
    await Swal.fire({
      icon: 'error',
      title: '登入失敗',
      text: decodeURIComponent(errorMsg),
      confirmButtonColor: '#9f9572'
    });
    router.push('/login');
    return;
  }

  const accessToken = route.query.accessToken;
  const refreshToken = route.query.refreshToken;

  const require2fa = route.query.require2fa === 'true';
  const preAuthToken = route.query.preAuthToken;

  if (require2fa && preAuthToken) {
    try {
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
        },
        allowOutsideClick: false
      });

      if (code) {
        // 發送 OAuth2 2FA 驗證請求
        const verifyRes = await authAPI.oauth2Verify2FA({ preAuthToken, code });
        const { accessToken: newAccess, refreshToken: newRefresh, user } = verifyRes.data;

        // 登入成功，儲存 Token 並設定 Pinia store
        localStorage.setItem('accessToken', newAccess);
        localStorage.setItem('refreshToken', newRefresh);
        authStore.login(user, newAccess, newRefresh);

        await Swal.fire({
          icon: 'success',
          title: '登入成功！',
          html: `歡迎回來, <b>${authStore.userName}</b>`,
          timer: 1500,
          showConfirmButton: false
        });
        
        router.push('/');
      } else {
        // 使用者取消輸入
        router.push('/login');
      }
    } catch (error) {
      console.error('OAuth2 2FA failed', error);
      Swal.fire({
        icon: 'error',
        title: '驗證失敗',
        text: error.response?.data?.message || '無效的驗證碼，請重新登入',
        confirmButtonColor: '#9f9572'
      });
      router.push('/login');
    }
    return;
  }

  if (accessToken && refreshToken) {
    try {
      // 暫存 Token 讓 Axios Interceptor 可以發送請求
      localStorage.setItem('accessToken', accessToken);
      localStorage.setItem('refreshToken', refreshToken);
      
      // 取得使用者詳細資料
      const response = await userAPI.getProfile();
      const user = response.data;
      
      // 正式透過 store 把資料狀態設定好
      authStore.login(user, accessToken, refreshToken);
      
      await Swal.fire({
        icon: 'success',
        title: '登入成功！',
        html: `歡迎回來, <b>${authStore.userName}</b>`,
        timer: 1500,
        showConfirmButton: false
      });
      
      router.push('/');
    } catch (error) {
      console.error('OAuth login failed to get profile', error);
      Swal.fire({
        icon: 'error',
        title: '登入失敗',
        text: '無法取得登入授權之使用者資料，請稍後再試',
        confirmButtonColor: '#9f9572'
      });
      router.push('/login');
    }
  } else {
    Swal.fire({
      icon: 'error',
      title: '登入失敗',
      text: '登入授權憑證回傳有誤，請稍後再試',
      confirmButtonColor: '#9f9572'
    });
    router.push('/login');
  }
});
</script>

<style scoped>
.oauth-redirect {
  background-color: rgba(160, 150, 115, 0.05) !important;
}
.text-primary {
  color: #9f9572 !important;
}
</style>
