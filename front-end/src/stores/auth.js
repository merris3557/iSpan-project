import { defineStore } from 'pinia'
import api from '@/api/config'

let moduleSyncUserPromise = null;

export const useAuthStore = defineStore('auth', {
    state: () => ({
        // 不再從 localStorage 讀取 user 的詳細資料與 token
        user: null,
    }),

    getters: {
        // 判斷是否登入：依據記憶體中是否有 user 物件
        isLoggedIn: (state) => !!state.user,
        userName: (state) => state.user ? state.user.name : '',
        isStoreUser: (state) => state.user ? state.user.isStore === true : false,
    },

    actions: {
        login(userData) {
            this.user = userData;
            // 設立一個安全的不帶敏感資訊的 flag 讓重新整理時知道要抓資料
            localStorage.setItem('isUserLoggedIn', 'true');
        },
        async logout() {
            try {
                await api.post('/auth/logout');
            } catch (error) {
                console.error('Logout failed:', error);
            }
            this.logoutLocally();
        },
        // 僅清除前端狀態
        logoutLocally() {
            this.user = null;
            localStorage.removeItem('isUserLoggedIn');
        },
        updateUser(userData) {
            this.user = { ...this.user, ...userData };
        },
        checkAuth() {
            return this.isLoggedIn;
        },
        /**
         * 統一處理登出與提示
         * @param {string} type - 'timeout' (逾時) 或 'unauthorized' (未登入)
         */
        async handleLogoutAndNotify(type = 'timeout') {
            console.warn(`[UserStore] 準備執行 handleLogoutAndNotify，類型: ${type}`);
            this.logoutLocally();
            let config;
            if (type === 'idle') {
                config = { title: '閒置逾久，已自動登出', text: '閒置過久，已自動登出', confirmButtonText: '重新登入' };
            } else if (type === 'timeout') {
                config = { title: '登入逾期', text: '您的登入工作階段已到期，請重新登入', confirmButtonText: '重新登入' };
            } else {
                config = { title: '請先登入', text: '此頁面需要登入後才能訪問', confirmButtonText: '前往登入' };
            }

            const Swal = (await import('sweetalert2')).default;
            console.warn(`[UserStore] 準備彈出 Swal.fire，標題: ${config.title}`);
            await Swal.fire({
                icon: 'warning',
                title: config.title,
                text: config.text,
                confirmButtonText: config.confirmButtonText
            });
            console.warn(`[UserStore] Swal.fire 彈出結束 (使用者已點擊)`);
        },

        async syncUserProfile() {
            console.warn('[syncUserProfile] 開始執行');
            if (moduleSyncUserPromise) {
                console.warn('[syncUserProfile] 偵測到已有進行中的 Promise，等待中...');
                return moduleSyncUserPromise;
            }

            console.warn('[syncUserProfile] 建立新的 Promise');
            moduleSyncUserPromise = (async () => {
                try {
                    const response = await api.get('/auth/me');
                    const responseData = response?.data || response;
                    const latestUserData = responseData?.data || responseData;

                    console.log('[syncUserProfile] 更新 user 資料，有 name:', latestUserData?.name);
                    this.updateUser(latestUserData);
                } catch (error) {
                    console.error('[syncUserProfile] API 請求發生錯誤:', error?.response?.status ?? error.message);
                } finally {
                    moduleSyncUserPromise = null;
                }
            })();

            return moduleSyncUserPromise;
        }
    }
})
