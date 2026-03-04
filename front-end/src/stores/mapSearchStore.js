import { defineStore } from 'pinia';
import api from '@/api/config.js';

/**
 * MapSearch Pinia Store
 * 管理地圖搜尋的狀態：搜尋結果、載入中、錯誤訊息
 */
export const useMapSearchStore = defineStore('mapSearch', {
    state: () => ({
        results: [],      // StoreSearchResultDto 列表
        loading: false,
        error: null,
    }),

    actions: {
        /**
         * 呼叫後端搜尋 API
         * @param {string} keyword - 關鍵字（可空）
         * @param {string[]} categories - 標籤陣列（可空）
         */
        async searchStores(keyword = '', categories = []) {
            this.loading = true;
            this.error = null;

            try {
                // 組裝 query string（categories 可多值）
                const params = new URLSearchParams();
                if (keyword && keyword.trim()) {
                    params.append('keyword', keyword.trim());
                }
                categories.forEach(c => params.append('categories', c));

                const queryString = params.toString();
                const url = queryString ? `/map/search?${queryString}` : '/map/search';

                const data = await api.get(url);
                this.results = data;
            } catch (err) {
                this.error = '搜尋失敗，請稍後再試';
                console.error('[mapSearchStore] 搜尋失敗:', err);
            } finally {
                this.loading = false;
            }
        },

        /** 清空搜尋結果 */
        clearResults() {
            this.results = [];
            this.error = null;
        },
    },
});
