import api from "./config";

/**
 * 取得type清單，供feedback表單的下拉選單使用
 */
export const getTypeList = () => {
    return api.get("/feedback/typeList");
};

/**
 * 送出意見回饋表單
 * @param {Object} payload - { caseNumber, name, phone, email, contents, typeId }
 */
export const submitFeedback = (payload) => {
    return api.post("/feedback", payload);
};
