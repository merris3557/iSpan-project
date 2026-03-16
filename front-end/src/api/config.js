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
// 前後台分開維護！避免 admin refresh 的 cooldown 影響前台 user refresh，反之亦然也成立。
let userRefreshState = {
    promise: null,
    timestamp: 0,
};
let adminRefreshState = {
    promise: null,
    timestamp: 0,
};

// 5 秒內重複觸發的 401 共用同一個 refresh（防止 F5 瞬間多個請求同時觸發多次 refresh）
const REFRESH_COOLDOWN_MS = 5000;

function doRefresh(isAdminContext) {
    const now = Date.now();
    const endpoint = isAdminContext ? '/admins/refresh' : '/auth/refresh';
    const state = isAdminContext ? adminRefreshState : userRefreshState;

    // 如果已有進行中的 refresh，共用同一個 Promise
    if (state.promise) {
        console.log('[REFRESH] 共用已有的 refresh');
        return state.promise;
    }

    // 如果剛成功 refresh 過（cooldown 期間），直接 resolve（Cookie 已更新）
    if (now - state.timestamp < REFRESH_COOLDOWN_MS) {
        const elapsed = ((now - state.timestamp) / 1000).toFixed(1);
        console.log(`[REFRESH] 在冷卻期內（${elapsed}s 前剛換發過），跳過重複 refresh`);
        return Promise.resolve();
    }

    console.log(`[REFRESH] 啟動新 refresh → ${endpoint}`);
    state.promise = rawApi.post(`${BASE_URL}${endpoint}`)
        .then(() => {
            console.log('[REFRESH] 成功，Cookie 已更新');
            state.timestamp = Date.now();
        })
        .catch(err => {
            console.error('[REFRESH] 失敗', err.response?.status);
            throw err;
        })
        .finally(() => {
            state.promise = null;
        });

    return state.promise;
}

// 設定 Context Hint 的邏輯
const addContextHint = (config) => {
    if (config.url?.startsWith('/admins')) {
        config.headers['X-Context-Hint'] = 'ADMIN';
    } else if (config.url?.startsWith('/auth')) {
        config.headers['X-Context-Hint'] = 'USER';
    } else {
        const isAdminPage = typeof window !== 'undefined' && window.location.pathname.startsWith('/admin');
        config.headers['X-Context-Hint'] = isAdminPage ? 'ADMIN' : 'USER';
    }
    return config;
};

// ===== Request Interceptor =====
api.interceptors.request.use(
    (config) => {
        addContextHint(config);
        console.log(`[API REQ] ${config.method?.toUpperCase()} ${config.baseURL}${config.url} (Context: ${config.headers['X-Context-Hint']})`);
        return config;
    },
    (error) => Promise.reject(error)
);

rawApi.interceptors.request.use(
    (config) => {
        addContextHint(config);
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

            // isAdminContext 應該依據這次請求本身的 Context Hint 來決定，而不是粗暴地看當前網址
            // 避免前台 API 在後台頁面出現 401 時，錯跑後台的登出流程
            let isAdminContext = false;
            const contextHint = error.config?.headers?.['X-Context-Hint'];
            if (contextHint) {
                isAdminContext = contextHint === 'ADMIN';
            } else {
                isAdminContext = typeof window !== 'undefined' && window.location.pathname.startsWith('/admin');
            }

            try {
                // 啟動或共用 Refresh 動作
                console.log(`[REFRESH-WRAPPER] 準備 refresh，觸發來源 URL: ${url}`);
                await doRefresh(isAdminContext);

                const retryConfig = { ...error.config };
                
                // 註解：不要隨便刪除 transformRequest，否則 FormData 和 JSON 資料格式會跑掉而導致 400 錯誤
                // 但為了某些相容性問題，只刪除 adapter
                delete retryConfig.adapter;

                console.log(`[REFRESH-WRAPPER] 重送 ${methodName.toUpperCase()} ${url}`);
                const finalResult = await api.request(retryConfig);

                const resultType = finalResult && typeof finalResult === 'object' ? Object.keys(finalResult) : finalResult;
                console.log(`[REFRESH-WRAPPER] 重送成功，拿到 keys:`, resultType);

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
