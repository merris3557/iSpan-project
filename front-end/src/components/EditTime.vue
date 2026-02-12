<script setup>
// 透過 defineModel 與父組件實作雙向綁定
const interval = defineModel('interval', { type: Number });
const duration = defineModel('duration', { type: Number });

// 選項寫死，當父組件執行「儲存」動作時，會將這些選中的數值寫入資料庫。
const intervalOptions = [30, 60, 90];
const durationOptions = [60, 90, 120, 150, 180];
</script>

<template>
    <div class="time-slot-manager">
        <h3 class="text-gdg h5 mb-4">訂位規則設定</h3>

        <div class="row g-4">
            <div class="col-md-6">
                <div class="form-group">
                    <label class="form-label text-secondary fw-bold mb-2">
                        <i class="bi bi-clock-history me-1"></i> 訂位開放間隔 (分鐘)
                    </label>
                    <select v-model.number="interval" class="form-select border-gdg custom-select">
                        <option v-for="opt in intervalOptions" :key="opt" :value="opt">
                            {{ opt }} 分鐘
                        </option>
                    </select>
                </div>
            </div>

            <div class="col-md-6">
                <div class="form-group">
                    <label class="form-label text-secondary fw-bold mb-2">
                        <i class="bi bi-hourglass-split me-1"></i> 用餐時間限制 (分鐘)
                    </label>
                    <select v-model.number="duration" class="form-select border-gdg custom-select">
                        <option v-for="opt in durationOptions" :key="opt" :value="opt">
                            {{ opt }} 分鐘
                        </option>
                    </select>
                </div>
            </div>
        </div>
    </div>
</template>

<style scoped>
.border-gdg {
    border-color: #9f9572;
}

.border-gdg:focus {
    border-color: #776f54;
    box-shadow: 0 0 0 0.25 rgba(159, 149, 114, 0.25);
}

.custom-select {
    border-radius: 0;
    /* 維持 GDG 一貫的直角風格 */
    cursor: pointer;
    background-color: #fcfbf7;
}

.form-label {
    display: block;
    font-size: 0.9rem;
}

/* 讓 select 更有質感 */
select.form-select {
    padding: 0.6rem 1rem;
}
</style>