<script setup>
import Navbar from '@/layouts/navbar.vue';
import Footer from '@/layouts/footer.vue';
import { onMounted, onUnmounted, watch } from 'vue';
import { useProductsDepot } from '@/stores/productsDepot';
import { useCartStore } from '@/stores/cart';
import { useAuthStore } from '@/stores/auth';
import { useAdminAuthStore } from '@/stores/adminAuth';
import { IdleTracker } from '@/utils/idleTracker';

const depot = useProductsDepot();

// ===== 閒置逾時設定 =====
// 建議與 application.properties 的 jwt.access-token-expiration-ms 保持一致
// 測試期：60 秒；生產環境：900 秒（15 分鐘）
const IDLE_TIMEOUT_MS = 900000;

// Store 必須在 setup 頂層取得，才能讓 watch 正確監聽
const authStore = useAuthStore();
const adminAuthStore = useAdminAuthStore();
const cartStore = useCartStore();

let userIdleTracker = null;
let adminIdleTracker = null;

// ===== 啟動 / 停止閒置偵測 =====
const startUserIdle = () => {
    if (userIdleTracker) return;   // 避免重複啟動
    userIdleTracker = new IdleTracker(IDLE_TIMEOUT_MS, async () => {
        userIdleTracker = null;
        await authStore.handleLogoutAndNotify('idle');
        window.location.href = '/login';
    });
    userIdleTracker.start();
};

const stopUserIdle = () => {
    userIdleTracker?.stop();
    userIdleTracker = null;
};

const startAdminIdle = () => {
    if (adminIdleTracker) return;   // 避免重複啟動
    adminIdleTracker = new IdleTracker(IDLE_TIMEOUT_MS, async () => {
        adminIdleTracker = null;
        await adminAuthStore.handleLogoutAndNotify('idle');
        window.location.href = '/admin/login';
    });
    adminIdleTracker.start();
};

const stopAdminIdle = () => {
    adminIdleTracker?.stop();
    adminIdleTracker = null;
};

// ===== 監聽登入狀態 =====
// 使用者：登入時啟動 IdleTracker，登出時停止
watch(() => authStore.isLoggedIn, (isLoggedIn) => {
    if (isLoggedIn) {
        startUserIdle();
    } else {
        stopUserIdle();
    }
});

// 管理員：登入時啟動 IdleTracker，登出時停止
watch(() => adminAuthStore.isLoggedIn, (isLoggedIn) => {
    if (isLoggedIn) {
        startAdminIdle();
    } else {
        stopAdminIdle();
    }
});

// ===== 頁面初始載入（F5 刷新）時同步登入狀態 =====
onMounted(async () => {
    const isAdminPage = typeof window !== 'undefined' && window.location.pathname.startsWith('/admin');

    if (isAdminPage) {
        // 後台管理員：只在後台頁面同步管理員狀態
        if (localStorage.getItem('isAdminLoggedIn') === 'true') {
            await adminAuthStore.syncAdminProfile();
            // watch 監聽到 isLoggedIn 變成 true 時會自動呼叫 startAdminIdle()
        }
    } else {
        // 前台使用者：只在前台頁面同步使用者狀態與購物車
        if (localStorage.getItem('isUserLoggedIn') === 'true') {
            await authStore.syncUserProfile();
            if (authStore.isLoggedIn) {
                await cartStore.fetchCart();
                // watch 監聽到 isLoggedIn 變成 true 時會自動呼叫 startUserIdle()
            }
        }
    }
});

// 離開頁面時停止追蹤，避免記憶體洩漏
onUnmounted(() => {
    stopUserIdle();
    stopAdminIdle();
});
</script>

<template>
  <RouterView />
</template>

<style scoped></style>