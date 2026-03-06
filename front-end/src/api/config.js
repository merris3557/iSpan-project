import axios from 'axios';
import { useAuthStore } from '@/stores/auth';
import { useAdminAuthStore } from '@/stores/adminAuth';

const BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api';

// ===== 主要 axios 實例 =====
const api = axios.create({
    baseURL: BASE_URL,
    timeout: 10000,
    withCredentials: true,
    headers: { 'Content-Type': 'application/json', 'Accept': 'application/json' }
});

// ===== Retry 專用 axios 實例（無 refresh interceptor，避免遞迴）=====
const rawApi = axios.create({
    baseURL: BASE_URL,
    timeout: 10000,
    withCredentials: true,
    headers: { 'Content-Type': 'application/json', 'Accept': 'application/json' }
});

// ===== Refresh 狀態管理 =====
let refreshState = {
    promise: null,      // 進行中的 refresh Promise
    timestamp: 0,       // 上次成功 refresh 的時間戳
};

// 5 秒內重複觸發的 401 共用同一個 refresh（防止 F5 瞬間多個請求同時觸發多次 refresh）
const REFRESH_COOLDOWN_MS = 5000;

function doRefresh(isAdminContext) {
    const now = Date.now();
    const endpoint = isAdminContext ? '/admins/refresh' : '/auth/refresh';

    // 如果已有進行中的 refresh，或剛完成不久的 refresh，共用同一個 Promise
    if (refreshState.promise) {
        console.log('[REFRESH] 共用已有的 refresh');
        return refreshState.promise;
    }

    // 如果剛成功 refresh 過（cooldown 期間），直接 resolve（Cookie 已更新）
    if (now - refreshState.timestamp < REFRESH_COOLDOWN_MS) {
        console.log('[REFRESH] 在冷卻期內，跳過重複 refresh');
        return Promise.resolve();
    }

    console.log(`[REFRESH] 啟動新 refresh → ${endpoint}`);
    refreshState.promise = rawApi.post(`${BASE_URL}${endpoint}`)
        .then(() => {
            console.log('[REFRESH] 成功，Cookie 已更新');
            refreshState.timestamp = Date.now();
        })
        .catch(err => {
            console.error('[REFRESH] 失敗', err.response?.status);
            throw err;
        })
        .finally(() => {
            refreshState.promise = null;
        });

    return refreshState.promise;
}

// ===== Request Interceptor =====
api.interceptors.request.use(
    (config) => {
        console.log(`[API REQ] ${config.method?.toUpperCase()} ${config.baseURL}${config.url}`);
        return config;
    },
    (error) => Promise.reject(error)
);

// ===== Response Interceptor =====
// 這裡我們只處理成功的 response 解包與一般的錯誤印出，禁止在這裡作 Refresh 攔截！
api.interceptors.response.use(
    (response) => {
        console.log(`[API OK ] ${response.status} ${response.config.url}`);
        return response.data;
    },
    (error) => {
        const status = error.response?.status;
        const url = error.config?.url;
        console.warn(`[API ERR] ${status} ${url}`, error.response?.data?.message ?? error.message);
        return Promise.reject(error);
    }
);

// ===== 終極防護盾：自訂 API 封裝 =====
// 由於 Axios 1.x 攔截器內部極其惡劣的 config 替換 Bug，我們拒絕在 interceptors 中處理重發。
// 我們直接攔截並包裹所有常見的 axios 方法，用標準的 async/await 外殼來確保 Promise 不被吃掉。
const createMethodWrapper = (methodName) => {
    return async (...args) => {
        try {
            // 先嘗試正規的懇求
            return await api[methodName](...args);
        } catch (error) {
            const status = error.response?.status;
            const url = error.config?.url;

            // 如果不是 401 或是特定忽略的路由，直接認命拋出
            if (!error.response || status !== 401) {
                return Promise.reject(error);
            }
            const ignoredEndpoints = ['/login', '/logout', '/refresh'];
            if (ignoredEndpoints.some(ep => url?.includes(ep))) {
                return Promise.reject(error);
            }

            // 防無窮迴圈
            if (error.config && error.config._retry) {
                console.warn('[REFRESH] Wrapper 發現 _retry:true，放棄重試');
                return Promise.reject(error);
            }
            error.config._retry = true; // 註記上去，避免其他外殼再攔

            const isAdminContext = typeof window !== 'undefined' && window.location.pathname.startsWith('/admin');

            try {
                // 啟動或共用 Refresh 動作
                await doRefresh(isAdminContext);

                console.warn(`[REFRESH-WRAPPER] Cookie 更新成功，準備用 ${methodName.toUpperCase()} 重新發送請求`);

                // 完全用外殼重發一次 API 請求！這裡的 Promise 絕對純淨無瑕！
                // 為了防呆，我們使用 api.request 再把原本的 config 傳進去
                const retryConfig = { ...error.config };
                delete retryConfig.adapter;
                delete retryConfig.transformRequest;
                delete retryConfig.transformResponse;

                const finalResult = await api.request(retryConfig);
                console.warn('[REFRESH-WRAPPER] 重發成功！拿到的最終資料鍵值:', Object.keys(finalResult || {}));

                return finalResult;
            } catch (refreshError) {
                console.error('[REFRESH-WRAPPER] 徹底失敗，強制登出', refreshError);
                if (isAdminContext) {
                    const adminAuthStore = useAdminAuthStore();
                    await adminAuthStore.handleLogoutAndNotify('timeout');
                    window.location.href = '/admin/login';
                } else {
                    const authStore = useAuthStore();
                    await authStore.handleLogoutAndNotify('timeout');
                    window.location.href = '/login';
                }
                return Promise.reject(refreshError);
            }
        }
    };
};

const apiWrapper = {
    get: createMethodWrapper('get'),
    post: createMethodWrapper('post'),
    put: createMethodWrapper('put'),
    delete: createMethodWrapper('delete'),
    patch: createMethodWrapper('patch'),
    request: createMethodWrapper('request'),
};

export default apiWrapper;
