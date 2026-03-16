// 控制哪些環境要輸出 log，true = 輸出，false = 關閉
const LOG_CONFIG = {
    dev: false,   // 開發環境
    test: false,  // 測試環境
    prod: false,  // 正式環境
}

const currentMode = import.meta.env.MODE  // 'development' | 'test' | 'production'

export const shopLog = (...args) => {
    if (
        (currentMode === 'development' && LOG_CONFIG.dev) ||
        (currentMode === 'test' && LOG_CONFIG.test) ||
        (currentMode === 'production' && LOG_CONFIG.prod)
    ) {
        console.log(...args)
    }
}