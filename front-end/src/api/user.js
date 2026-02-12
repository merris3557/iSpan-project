import api from './config';

export const userAPI = {
    /**
     * 取得當前登入者詳細資料 (後端驗證測試)
     */
    getProfile() {
        const token = localStorage.getItem('accessToken');
        return api.get('/users/profile', {
            headers: {
                Authorization: `Bearer ${token}`
            }
        });
    }
};

export default userAPI;
