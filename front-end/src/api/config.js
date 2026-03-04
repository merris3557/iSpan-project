import axios from 'axios';
import Swal from 'sweetalert2';
import { useAuthStore } from '@/stores/auth';

// Create an axios instance with custom config
const api = axios.create({
    baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api', // Default to a common backend port
    timeout: 10000,
    headers: {
        'Content-Type': 'application/json',
        'Accept': 'application/json'
    }
});

// Request interceptor
api.interceptors.request.use(
    (config) => {
        // Decide which token to use based on the URL or availability
        // If it's an admin endpoint or we have an admin token but no user token, use admin token
        const userToken = localStorage.getItem('accessToken');
        const adminToken = localStorage.getItem('adminAccessToken');

        let tokenToUse = userToken;

        // Common admin-only paths (or if we are logged in as admin explicitly)
        // StoreRegistration API mixed paths: /store-registrations is admin GET, user POST
        const isAdminPath = config.url.startsWith('/admins') ||
            (config.url.startsWith('/store-registrations') && config.method === 'get' && !config.url.endsWith('/my')) ||
            (config.url.includes('/review'));

        if (isAdminPath && adminToken) {
            tokenToUse = adminToken;
        } else if (!tokenToUse && adminToken) {
            // fallback if no user token
            tokenToUse = adminToken;
        }

        // Always force admin token for /admins endpoints if it exists, to prevent sending userToken
        if (config.url.startsWith('/admins') && adminToken) {
            tokenToUse = adminToken;
        } else if (config.url.startsWith('/admins') && !adminToken) {
            // If it's an admin path but no admin token, don't send a normal user token
            tokenToUse = null;
        }

        if (tokenToUse) {
            config.headers.Authorization = `Bearer ${tokenToUse}`;
        }
        return config;
    },
    (error) => {
        return Promise.reject(error);
    }
);

// Response interceptor
api.interceptors.response.use(
    (response) => {
        return response.data;
    },
    (error) => {
        // Handle global errors here
        if (error.response && error.response.status === 401) {
            // Unauthorized or Token Expired
            console.error('Session expired or unauthorized');

            const isAdminPath = error.config && error.config.url && (
                error.config.url.startsWith('/admins') ||
                (error.config.url.startsWith('/store-registrations') && error.config.method === 'get' && !error.config.url.endsWith('/my')) ||
                (error.config.url.includes('/review'))
            );

            if (isAdminPath) {
                import('@/stores/adminAuth').then(module => {
                    const adminAuthStore = module.useAdminAuthStore();
                    adminAuthStore.handleLogoutAndNotify('timeout').then(() => {
                        if (typeof window !== 'undefined') {
                            window.location.href = '/admin/login';
                        }
                    });
                });
            } else {
                const authStore = useAuthStore();
                authStore.handleLogoutAndNotify('timeout').then(() => {
                    if (typeof window !== 'undefined') {
                        window.location.href = '/login';
                    }
                });
            }
        }

        console.error('API Error:', error.response || error.message);
        return Promise.reject(error);
    }
);

export default api;
