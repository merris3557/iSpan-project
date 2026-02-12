import { defineStore } from 'pinia'

export const useAuthStore = defineStore('auth', {
    state: () => ({
        user: JSON.parse(localStorage.getItem('user')) || null,
        token: localStorage.getItem('accessToken') || null,
    }),

    getters: {
        isLoggedIn: (state) => !!state.token,
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
        }
    }
})
