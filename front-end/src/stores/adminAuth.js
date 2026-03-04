import { defineStore } from 'pinia';
import { isTokenExpired } from '@/utils/jwt';
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

export const useAdminAuthStore = defineStore('adminAuth', {
    state: () => ({
        // initialize state from local storage to enable user to stay logged in
        admin: safeJSONParse(localStorage.getItem('adminUser')),
        accessToken: localStorage.getItem('adminAccessToken') || null,
        refreshToken: localStorage.getItem('adminRefreshToken') || null,
    }),

    getters: {
        isLoggedIn: (state) => !!state.accessToken && !isTokenExpired(state.accessToken),
        isExpired: (state) => !!state.accessToken && isTokenExpired(state.accessToken),
        adminName: (state) => state.admin ? state.admin.name : '',
        adminPosition: (state) => state.admin ? state.admin.position : '',
        hasRole: (state) => (role) => state.admin && state.admin.position === role,
        hasAnyRole: (state) => (roles) => state.admin && roles.includes(state.admin.position),
    },

    actions: {
        login(adminData, accessToken, refreshToken) {
            this.admin = adminData;
            this.accessToken = accessToken;
            this.refreshToken = refreshToken;

            // store user details and jwt in local storage to keep user logged in between page refreshes
            localStorage.setItem('adminUser', JSON.stringify(adminData));
            localStorage.setItem('adminAccessToken', accessToken);
            localStorage.setItem('adminRefreshToken', refreshToken);
        },

        logout() {
            this.admin = null;
            this.accessToken = null;
            this.refreshToken = null;

            localStorage.removeItem('adminUser');
            localStorage.removeItem('adminAccessToken');
            localStorage.removeItem('adminRefreshToken');

            // cleanup old legacy keys if necessary
            localStorage.removeItem('adminAccount');
            localStorage.removeItem('adminName');
            localStorage.removeItem('adminPosition');
        },

        async handleLogoutAndNotify(type = 'timeout') {
            this.logout();
            const config = type === 'timeout'
                ? { title: '登入已逾時', text: '管理員登入工作階段已到期，請重新登入' }
                : { title: '請先登入', text: '此頁面需要管理員權限才能訪問' };

            const Swal = (await import('sweetalert2')).default;
            await Swal.fire({
                icon: 'warning',
                title: config.title,
                text: config.text,
                confirmButtonText: type === 'timeout' ? '重新登入' : '前往登入'
            });
        },

        async syncAdminProfile() {
            if (!this.isLoggedIn) return;

            try {
                const response = await api.get('/admins/me');

                // axios interceptor 已經回傳 response.data，所以這裡只需要再取 .data (ApiResponse 物件裡的 data)
                const latestAdminData = response.data;

                this.admin = { ...this.admin, ...latestAdminData };
                localStorage.setItem('adminUser', JSON.stringify(this.admin));

            } catch (error) {
                console.error('同步管理員資料失敗:', error);

                if (error.response && [401, 403].includes(error.response.status)) {
                    this.logout();

                    const Swal = (await import('sweetalert2')).default;
                    Swal.fire({
                        icon: 'warning',
                        title: '登入狀態失效',
                        text: '您的管理員帳號狀態已變更或驗證過期，請重新登入。'
                    }).then(() => {
                        window.location.href = '/admin/login';
                    });
                }
            }
        }
    }
});
