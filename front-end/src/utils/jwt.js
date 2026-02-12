/**
 * JWT 工具函數
 * 用於解析 JWT token 並取得 payload 中的資料
 */

/**
 * 解碼 JWT Token 的 Payload
 * @param {string} token - JWT token 字串
 * @returns {Object|null} - 解碼後的 payload 物件，失敗則回傳 null
 */
export function decodeJwtPayload(token) {
    try {
        if (!token) return null;

        // JWT 格式: header.payload.signature
        const parts = token.split('.');
        if (parts.length !== 3) {
            console.error('Invalid JWT format');
            return null;
        }

        // 取得 payload 部分 (第二段)
        const payload = parts[1];

        // Base64Url 解碼
        // 需要處理 Base64Url 和標準 Base64 的差異
        const base64 = payload.replace(/-/g, '+').replace(/_/g, '/');

        // 解碼並轉換為 JSON
        const jsonPayload = decodeURIComponent(
            atob(base64)
                .split('')
                .map(c => '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2))
                .join('')
        );

        return JSON.parse(jsonPayload);
    } catch (error) {
        console.error('JWT decode error:', error);
        return null;
    }
}

/**
 * 從 JWT 中取得使用者角色
 * @param {string} token - JWT token
 * @returns {string|null} - 使用者角色
 */
export function getRoleFromToken(token) {
    const payload = decodeJwtPayload(token);
    return payload?.role || null;
}

/**
 * 從 JWT 中取得使用者 Email (sub)
 * @param {string} token - JWT token
 * @returns {string|null} - 使用者 email
 */
export function getSubFromToken(token) {
    const payload = decodeJwtPayload(token);
    return payload?.sub || null;
}

/**
 * 檢查 JWT 是否已過期
 * @param {string} token - JWT token
 * @returns {boolean} - true 表示已過期
 */
export function isTokenExpired(token) {
    const payload = decodeJwtPayload(token);
    if (!payload || !payload.exp) return true;

    // exp 是 Unix timestamp (秒)，需要轉換為毫秒
    const expirationTime = payload.exp * 1000;
    return Date.now() >= expirationTime;
}

/**
 * 取得 Token 的過期時間
 * @param {string} token - JWT token
 * @returns {Date|null} - 過期時間
 */
export function getTokenExpiration(token) {
    const payload = decodeJwtPayload(token);
    if (!payload || !payload.exp) return null;
    return new Date(payload.exp * 1000);
}

export default {
    decodeJwtPayload,
    getRoleFromToken,
    getSubFromToken,
    isTokenExpired,
    getTokenExpiration
};
