import api from './config';

export const userAPI = {
    /**
     * 取得當前登入者詳細資料 (後端驗證測試)
     */
    getProfile() {
        return api.get('/users/profile');
    },
    /**
     * 更新使用者資料 (姓名、密碼等)
     * @param {Object} data - { name: 'New Name', password: 'New Password' }
     */
    updateProfile(data) {
        return api.put('/users/profile', data);
    },
    /**
     * 取得當前用戶的 2FA 狀態
     */
    get2FAStatus() {
        return api.get('/users/2fa/status');
    },
    /**
     * 啟用 2FA - 生成 QR code
     */
    enable2FA() {
        return api.post('/users/2fa/enable');
    },
    /**
     * 驗證並啟用 2FA
     * @param {Object} data - { code: '123456' }
     */
    verify2FA(data) {
        return api.post('/users/2fa/verify', data);
    },
    /**
     * 停用 2FA
     */
    disable2FA() {
        return api.post('/users/2fa/disable');
    }
};

export default userAPI;
