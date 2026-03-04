import { defineStore } from 'pinia'
import { isTokenExpired } from '@/utils/jwt'
import api from '@/api/config'

export const useAuthStore = defineStore('auth', {
    state: () => ({
        user: JSON.parse(localStorage.getItem('user')) || null,
        token: localStorage.getItem('accessToken') || null,
    }),

    getters: {
        isLoggedIn: (state) => !!state.token && !isTokenExpired(state.token),
        isExpired: (state) => !!state.token && isTokenExpired(state.token),
        userName: (state) => state.user ? state.user.name : '',
    },

    actions: {
        login(userData, token, refreshToken) {
            this.user = userData;
            this.token = token;
            localStorage.setItem('user', JSON.stringify(userData));
            localStorage.setItem('accessToken', token);
            localStorage.setItem('refreshToken', refreshToken);
        },
        logout() {
            this.user = null;
            this.token = null;
            localStorage.removeItem('user');
            localStorage.removeItem('accessToken');
            localStorage.removeItem('refreshToken');
        },
        updateUser(userData) {
            this.user = { ...this.user, ...userData };
            localStorage.setItem('user', JSON.stringify(this.user));
        },
        checkAuth() {
            if (this.isExpired) {
                this.logout();
                return false;
            }
            return this.isLoggedIn;
        },
        /**
         * 統一處理登出與提示
         * @param {string} type - 'timeout' (逾時) 或 'unauthorized' (未登入)
         */
        async handleLogoutAndNotify(type = 'timeout') {
            this.logout();
            const config = type === 'timeout'
                ? { title: '登入已逾時', text: '您的登入工作階段已到期，請重新登入' }
                : { title: '請先登入', text: '此頁面需要登入後才能訪問' };

            const Swal = (await import('sweetalert2')).default;
            await Swal.fire({
                icon: 'warning',
                title: config.title,
                text: config.text,
                confirmButtonText: type === 'timeout' ? '重新登入' : '前往登入'
            });

            // 由於 store 無法直接存取 router，這部分留給呼叫端處理跳轉，或從外部傳入 router
        },
        async syncUserProfile() {
            if (!this.checkAuth()) return;

            try {
                const response = await api.get('/auth/me');

                // axios interceptor 已經回傳 response.data，所以這裡只需要再取 .data (ApiResponse 物件裡的 data)
                const latestUserData = response.data;

                this.updateUser(latestUserData);

            } catch (error) {
                console.error('同步使用者資料失敗:', error);

                if (error.response && [401, 403].includes(error.response.status)) {
                    this.logout();

                    const Swal = (await import('sweetalert2')).default;
                    Swal.fire({
                        icon: 'warning',
                        title: '登入狀態失效',
                        text: '您的帳號狀態已變更或驗證過期，請重新登入。'
                    }).then(() => {
                        window.location.href = '/login';
                    });
                }
            }
        }
    }
})
