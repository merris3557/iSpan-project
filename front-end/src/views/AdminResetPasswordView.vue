<script setup>
import { ref } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import BaseCard from '@/components/common/BaseCard.vue';
import BaseButton from '@/components/common/BaseButton.vue';
import api from '@/api/config';
import Swal from 'sweetalert2';

const router = useRouter();
const route = useRoute();
const newPassword = ref('');
const confirmPassword = ref('');
const isSubmitting = ref(false);

const handleResetPassword = async () => {
    // 1. 檢查密碼格式 (至少8碼，包含大小寫、數字、特殊符號)
    const passwordRegex = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&])[A-Za-z\d@$!%*?&]{8,}$/;
    if (!passwordRegex.test(newPassword.value)) {
        Swal.fire({
            icon: 'error',
            title: '格式錯誤',
            text: '密碼格式有誤',
            confirmButtonColor: '#1e3c72'
        });
        return;
    }

    if (newPassword.value !== confirmPassword.value) {
        Swal.fire({
            icon: 'error',
            title: '錯誤',
            text: '兩次輸入的密碼不一致',
            confirmButtonColor: '#1e3c72'
        });
        return;
    }

    isSubmitting.value = true;
    const token = route.query.token;

    if (!token) {
        Swal.fire({
            icon: 'error',
            title: '錯誤',
            text: '無效或已過期的重設連結（缺少 Token）',
            confirmButtonColor: '#1e3c72'
        });
        isSubmitting.value = false;
        return;
    }

    const data = { 
        password: newPassword.value,
        token: token,
        newPassword: newPassword.value,
        confirmPassword: confirmPassword.value
    };

    try {
        const response = await api.post('/admins/reset-password', data);
        await Swal.fire({
            icon: 'success',
            title: '成功',
            text: '管理員密碼重設成功！請使用新密碼登入。',
            confirmButtonColor: '#1e3c72',
            timer: 2000
        });
        router.push('/admin/login');
    } catch (error) {
        console.error('Admin reset password failed:', error);
        
        // Backend could return various token messages like "already been used", "expired", "invalid"
        Swal.fire({
            icon: 'error',
            title: '重設請求失敗',
            text: '無效或已過期的重設連結',
            confirmButtonColor: '#1e3c72'
        });
    } finally {
        isSubmitting.value = false;
    }
};
</script>

<template>
    <div class="admin-login-page bg-admin-content min-vh-100 d-flex align-items-center justify-content-center p-3">
        <BaseCard class="admin-card" maxWidth="450px" :shadow="false" :border="false">
            <template #header>
                <div class="text-center mb-4 mt-2">
                    <h2 class="text-admin-primary fw-bold h4">設定管理員新密碼</h2>
                </div>
            </template>

            <form @submit.prevent="handleResetPassword">
                <div class="mb-3">
                    <label for="newPassword" class="form-label text-dark fw-medium small">新密碼</label>
                    <input 
                        type="password" 
                        id="newPassword" 
                        v-model="newPassword" 
                        class="form-control admin-form-control" 
                        placeholder="請輸入新密碼"
                        required
                    >
                    <div class="form-text text-muted mt-1" style="font-size: 0.8rem;">
                        *密碼需至少 8 個字元，並包含大小寫英文字母、數字及特殊符號
                    </div>
                </div>

                <div class="mb-4">
                    <label for="confirmPassword" class="form-label text-dark fw-medium small">確認新密碼</label>
                    <input 
                        type="password" 
                        id="confirmPassword" 
                        v-model="confirmPassword" 
                        class="form-control admin-form-control" 
                        placeholder="請再次輸入新密碼"
                        required
                    >
                </div>

                <div class="d-grid gap-2">
                    <BaseButton 
                        color="primary" 
                        class="btn-admin-primary w-100 py-2 fw-bold" 
                        :label="isSubmitting ? '重設中...' : '重設密碼'" 
                        :disabled="isSubmitting"
                        @click="handleResetPassword"
                    />
                </div>
                
                <!-- <div class="text-center mt-3 pt-2">
                    <button type="button" @click="router.push('/admin/login')" class="btn btn-link text-muted small text-decoration-none fw-medium">
                        <i class="bi bi-arrow-left me-1"></i> 取消並返回登入
                    </button>
                </div> -->
            </form>
        </BaseCard>
    </div>
</template>

<style lang="scss" scoped>
@import '@/assets/styles/custom.scss';

.admin-login-page {
  background-color: #f5f6fa;
}

.admin-form-control {
  &:focus {
    border-color: #1e3c72;
    box-shadow: 0 0 0 0.25rem rgba(30, 60, 114, 0.15);
  }
}
</style>
