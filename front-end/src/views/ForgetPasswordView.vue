<script setup>
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import BaseCard from '@/components/common/BaseCard.vue';
import BaseButton from '@/components/common/BaseButton.vue';
import { authAPI } from '@/api/auth';

const router = useRouter();
const email = ref('');
const isSubmitting = ref(false);

const handleSendResetLink = async () => {
    if (isSubmitting.value) return;

    isSubmitting.value = true;
    const data = { email: email.value };

    try {
        console.log('Forgot password request with:', data);
        const response = await authAPI.forgotPassword(data);
        console.log('Forgot password success:', response);
        alert(`重設密碼信件已發送至 ${email.value}`);
    } catch (error) {
        console.error('Forgot password failed:', error);
        alert(`發送請求失敗 (預計傳送到後端的 JSON):\n${JSON.stringify(data, null, 2)}`);
    } finally {
        isSubmitting.value = false;
    }
};

// Temporary helper for testing flow without email link
const debugGoToReset = () => {
    router.push('/reset-password');
}
</script>

<template>
    <div class="forget-password-page bg-gdg-light min-vh-100 d-flex align-items-center justify-content-center p-3">
        <BaseCard maxWidth="500px" :shadow="true" :hoverEffect="false">
            <template #header>
                <div class="text-center mb-2 mt-2">
                    <!-- <i class="bi bi-github fs-1 text-dark"></i> -->
                    <h2 class="fw-bold mt-3 mb-2">重設您的密碼</h2>
                </div>
            </template>

            <div class="text-center px-4 mb-4">
                <p class="text-muted">
                    輸入您帳戶驗證的電子郵件地址，<br>我們將發送密碼重設連結給您。
                </p>
            </div>

            <form @submit.prevent="handleSendResetLink">
                <div class="mb-4">
                    <label for="email" class="form-label fw-bold small">電子郵件</label>
                    <input 
                        type="email" 
                        id="email" 
                        v-model="email" 
                        class="form-control custom-input" 
                        placeholder="請輸入您的電子郵件地址"
                        required
                    >
                </div>

                <div class="mb-4">
                    <div class="text-center mb-3">
                        <span class="fw-bold fs-5">驗證您的帳戶</span>
                    </div>
                    
                    <!-- Placeholder for CAPTCHA / Turnstile -->
                    <div class="captcha-placeholder p-4 border rounded bg-light text-center">
                        <div class="d-flex justify-content-center align-items-center gap-3">
                            <div class="spinner-border spinner-border-sm text-secondary" role="status"></div>
                            <span class="text-muted small">Cloudflare Turnstile 載入中... (預留位置)</span>
                        </div>
                        <div class="mt-2 text-muted" style="font-size: 0.75rem;">
                            此處將放置驗證功能以防止機器人攻擊
                        </div>
                    </div>
                </div>

                <div class="d-grid gap-2">
                    <BaseButton 
                        color="gdg" 
                        class="text-white py-2 fw-bold" 
                        :label="isSubmitting ? '發送中...' : '發送密碼重設信件'" 
                        :disabled="isSubmitting"
                        @click="handleSendResetLink"
                    />
                    
                    <!-- Debug link for development convenience -->
                    <!-- <div class="text-center mt-2">
                        <a href="#" @click.prevent="debugGoToReset" class="small text-muted text-decoration-none">[開發測試：直接前往重設頁面]</a>
                    </div> -->
                </div>
            </form>
        </BaseCard>
    </div>
</template>

<style scoped>
.custom-input {
    border-radius: 6px;
    border: 1px solid #d0d7de;
    padding: 5px 12px;
    font-size: 16px;
    line-height: 20px;
    background-color: #f6f8fa; /* Slight grey background like github */
}

.custom-input:focus {
    border-color: #0969da;
    box-shadow: 0 0 0 3px rgba(9, 105, 218, 0.3);
    background-color: #fff;
}

.captcha-placeholder {
    border: 1px solid #d0d7de !important;
    background-color: #f6f8fa !important;
}

.bg-gdg-light {
    background-color: #f9f9f9 !important; /* Cleaner background for this page */
}
</style>
