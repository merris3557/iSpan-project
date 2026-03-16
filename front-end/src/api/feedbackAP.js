import api from "./config";

/**
 * 取得 feedback 清單（支援 status 篩選與分頁）
 * @param {string|null} status - feedback_status 的 statusName，null 或空字串表示全部
 * @param {number} page - 頁碼（從 0 開始）
 */
export const getFeedbackList = (status, page = 0) => {
    const params = { page };
    if (status) params.status = status;
    return api.get("/feedbackList", { params });
};

/**
 * 取得所有 feedback_status 選項（供下拉選單使用）
 */
export const getStatusList = () => {
    return api.get("/feedbackList/status-list");
};

/**
 * 送出回覆並更新 feedback 狀態
 * @param {{ feedbackId: number, reply: string, adminId: number, statusId: number }} payload
 */
export const replyFeedback = (payload) => {
    return api.put("/feedbackList/reply", payload);
};

/**
 * 刪除意見回饋
 * @param {number} id - feedback 的 id
 */
export const deleteFeedback = (id) => {
    return api.delete(`/feedbackList/${id}`);
};