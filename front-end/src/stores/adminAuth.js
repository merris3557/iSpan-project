import { defineStore } from 'pinia';
import api from '@/api/config';

const safeJSONParse = (val) => {
    try {
        if (!val || val === 'undefined') return null;
        return JSON.parse(val);
    } catch (e) {
        console.warn('Failed to parse localStorage item', e);
        return null;
    }
};

let moduleSyncAdminPromise = null;

export const useAdminAuthStore = defineStore('adminAuth', {
    state: () => ({
        // initialize state from local storage to enable user to stay logged in
        admin: null,
    }),

    getters: {
        isLoggedIn: (state) => !!state.admin,
        adminName: (state) => state.admin ? state.admin.name : '',
        adminPosition: (state) => state.admin ? state.admin.position : '',
        hasRole: (state) => (role) => state.admin && state.admin.position === role,
        hasAnyRole: (state) => (roles) => state.admin && roles.includes(state.admin.position),
    },

    actions: {
        login(adminData) {
            this.admin = adminData;
            localStorage.setItem('isAdminLoggedIn', 'true');
        },

        async logout() {
            try {
                await api.post('/admins/logout');
            } catch (error) {
                console.error('Logout failed:', error);
            }
            this.logoutLocally();
        },

        logoutLocally() {
            this.admin = null;
            localStorage.removeItem('isAdminLoggedIn');
            localStorage.removeItem('adminUser');
            localStorage.removeItem('adminAccessToken');
            localStorage.removeItem('adminRefreshToken');
            localStorage.removeItem('adminAccount');
            localStorage.removeItem('adminName');
            localStorage.removeItem('adminPosition');
        },

        async handleLogoutAndNotify(type = 'timeout') {
            console.warn(`[Store] 準備執行 handleLogoutAndNotify，類型: ${type}`);
            this.logoutLocally();
            let config;
            if (type === 'idle') {
                config = { title: '閒置逾久，已自動登出', text: '閒置過久，已自動登出', confirmButtonText: '重新登入' };
            } else if (type === 'timeout') {
                config = { title: '登入逾期', text: '管理員登入工作階段已到期，請重新登入', confirmButtonText: '重新登入' };
            } else {
                config = { title: '請先登入', text: '此頁面需要管理員權限才能訪問', confirmButtonText: '前往登入' };
            }

            const Swal = (await import('sweetalert2')).default;
            console.warn(`[Store] 準備彈出 Swal.fire，標題: ${config.title}`);
            await Swal.fire({
                icon: 'warning',
                title: config.title,
                text: config.text,
                confirmButtonText: config.confirmButtonText
            });
            console.warn(`[Store] Swal.fire 彈出結束 (使用者已點擊)`);
        },

        async syncAdminProfile() {
            console.warn('[syncAdminProfile] 開始執行');
            if (moduleSyncAdminPromise) {
                console.warn('[syncAdminProfile] 偵測到已有進行中的 Promise，等待中...');
                return moduleSyncAdminPromise;
            }

            console.warn('[syncAdminProfile] 建立新的 Promise');
            moduleSyncAdminPromise = (async () => {
                try {
                    const response = await api.get('/admins/me');

                    const responseData = response?.data || response;
                    const latestAdminData = responseData?.data || responseData;

                    this.admin = { ...this.admin, ...latestAdminData };
                } catch (error) {
                    console.error('[syncAdminProfile] API 請求發生錯誤:', error);
                } finally {
                    moduleSyncAdminPromise = null;
                    console.warn('[syncAdminProfile] Promise 執行完畢，鎖已解除');
                }
            })();

            return moduleSyncAdminPromise;
        },
    }
});
