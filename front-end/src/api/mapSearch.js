import api from "./config";

/**
 * 以關鍵字或分類 ID 搜尋店家（對應後端 /api/map/search）
 * @param {string} keyword - 搜尋關鍵字
 * @param {number|null} categoryId - 分類 ID（null 表示不限）
 */
export const searchStores = (keyword, categoryId = null) => {
    const params = {};
    if (keyword) params.keyword = keyword;
    if (categoryId !== null) params.categoryId = categoryId;
    return api.get("/map/search", { params });
};

/**
 * 取得所有分類/標籤清單（供 SearchBar 動態載入）
 */
export const getCategories = () => {
    return api.get("/categories");
};