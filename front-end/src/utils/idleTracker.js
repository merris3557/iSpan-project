/**
 * idleTracker.js
 *
 * 閒置偵測模組：監聽使用者操作（mousemove, keydown, click, scroll, touchstart）
 * 若超過指定時間無操作，觸發回調（通常是強制登出）
 *
 * 使用方式：
 *   import { IdleTracker } from '@/utils/idleTracker';
 *   const tracker = new IdleTracker(60000, onIdle);  // 60 秒閒置觸發
 *   tracker.start();
 *   tracker.stop();
 */

const IDLE_EVENTS = ['mousemove', 'keydown', 'mousedown', 'touchstart', 'scroll', 'click'];

export class IdleTracker {
    /**
     * @param {number} timeoutMs - 閒置多少毫秒後觸發 onIdle（建議與 JWT access token 有效期同步）
     * @param {Function} onIdle  - 閒置超時後的回調函數
     */
    constructor(timeoutMs, onIdle) {
        this.timeoutMs = timeoutMs;
        this.onIdle = onIdle;
        this._timer = null;
        this._boundReset = this._resetTimer.bind(this);
    }

    /** 啟動閒置偵測 */
    start() {
        this._attachListeners();
        this._resetTimer();
        console.log(`[IdleTracker] 啟動，閒置逾時 ${this.timeoutMs / 1000} 秒`);
    }

    /** 停止閒置偵測（登出後呼叫） */
    stop() {
        this._detachListeners();
        clearTimeout(this._timer);
        this._timer = null;
        console.log('[IdleTracker] 已停止');
    }

    _attachListeners() {
        IDLE_EVENTS.forEach(event => {
            window.addEventListener(event, this._boundReset, { passive: true });
        });
    }

    _detachListeners() {
        IDLE_EVENTS.forEach(event => {
            window.removeEventListener(event, this._boundReset);
        });
    }

    _resetTimer() {
        clearTimeout(this._timer);
        this._timer = setTimeout(() => {
            console.log('[IdleTracker] 閒置逾時，觸發登出');
            this.stop();
            this.onIdle();
        }, this.timeoutMs);
    }
}

export default IdleTracker;
