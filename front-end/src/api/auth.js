import api from './config';

export const authAPI = {
    /**
     * 使用者登入
     * @param {Object} data - { identifier, password } (identifier can be email or username)
     */
    login(data) {
        return api.post('/auth/login', data);
    },

    /**
     * 使用者註冊
     * @param {Object} data - { email, password, username, phone, ... }
     */
    register(data) {
        return api.post('/auth/register', data);
    },

    /**
     * 忘記密碼請求
     * @param {Object} data - { email }
     */
    forgotPassword(data) {
        return api.post('/auth/forgot-password', data);
    },

    /**
     * 重設密碼
     * @param {Object} data - { token, password }
     */
    resetPassword(data) {
        return api.post('/auth/reset-password', data);
    }
};

export default authAPI;
