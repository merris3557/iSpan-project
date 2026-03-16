<script setup>
import { ref } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import BaseCard from '@/components/common/BaseCard.vue';
import BaseButton from '@/components/common/BaseButton.vue';
import { authAPI } from '@/api/auth';
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
            confirmButtonColor: '#9f9572'
        });
        return;
    }

    if (newPassword.value !== confirmPassword.value) {
        Swal.fire({
            icon: 'error',
            title: '錯誤',
            text: '兩次輸入的密碼不一致'
        });
        return;
    }

    isSubmitting.value = true;
    const token = route.query.token;

    if (!token) {
        Swal.fire({
            icon: 'error',
            title: '錯誤',
            text: '無效的重設連結（缺少 Token）'
        });
        isSubmitting.value = false;
        return;
    }

    const data = { 
        newPassword: newPassword.value,
        confirmPassword: confirmPassword.value,
        token: token
    };

    try {
        const response = await authAPI.resetPassword(data);
        await Swal.fire({
            icon: 'success',
            title: '成功',
            text: '密碼重設成功！請使用新密碼登入。'
        });
        router.push('/login');
    } catch (error) {
        console.error('Reset password failed:', error);
        
        let errorMessage = '請稍後再試。';
        if (error.response && error.response.data && error.response.data.message) {
            errorMessage = error.response.data.message;
        } else if (error.message) {
            errorMessage = error.message;
        }

        Swal.fire({
            icon: 'error',
            title: '重設請求失敗',
            text: errorMessage
        });
    } finally {
        isSubmitting.value = false;
    }
};
</script>

<template>
    <div class="reset-password-page bg-gdg-light min-vh-100 d-flex align-items-center justify-content-center p-3">
        <BaseCard maxWidth="450px" :shadow="true" :hoverEffect="false">
            <template #header>
                <div class="text-center mb-4">
                    <h2 class="fw-bold">設定新密碼</h2>
                </div>
            </template>

            <form @submit.prevent="handleResetPassword">
                <div class="mb-3">
                    <label for="newPassword" class="form-label fw-bold small">新密碼</label>
                    <input 
                        type="password" 
                        id="newPassword" 
                        v-model="newPassword" 
                        class="form-control custom-input" 
                        placeholder="請輸入新密碼"
                        required
                    >
                    <div class="form-text text-muted mt-1" style="font-size: 0.8rem;">
                        *密碼需至少 8 個字元，並包含大小寫英文字母、數字及特殊符號
                    </div>
                </div>

                <div class="mb-4">
                    <label for="confirmPassword" class="form-label fw-bold small">確認新密碼</label>
                    <input 
                        type="password" 
                        id="confirmPassword" 
                        v-model="confirmPassword" 
                        class="form-control custom-input" 
                        placeholder="請再次輸入新密碼"
                        required
                    >
                </div>

                <div class="d-grid gap-2">
                    <BaseButton 
                        color="gdg" 
                        class="py-2 fw-bold" 
                        :label="isSubmitting ? '重設中...' : '重設密碼'" 
                        :disabled="isSubmitting"
                        @click="handleResetPassword"
                    />
                </div>
            </form>
        </BaseCard>
    </div>
</template>

<style scoped>
.custom-input {
    border-radius: 6px;
    border: 1px solid #ced4da;
    padding: 0.6rem 1rem;
}

.custom-input:focus {
    border-color: #9f9572;
    box-shadow: 0 0 0 0.2rem rgba(159, 149, 114, 0.25);
}
</style>
