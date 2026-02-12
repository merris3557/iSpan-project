<script setup>
const modelValue = defineModel({ type: Array });

const daysMap = {
    Monday: '星期一', Tuesday: '星期二', Wednesday: '星期三',
    Thursday: '星期四', Friday: '星期五', Saturday: '星期六', Sunday: '星期日'
};

// 一鍵同步功能：將第一天的設定套用到所有開啟的日期
const syncAllDays = () => {
    if (!confirm('是否將第一天的時間設定套用到其他所有天數？')) return;
    const firstDay = modelValue.value[0];
    modelValue.value.forEach((item, index) => {
        if (index !== 0) {
            item.open = firstDay.open;
            item.close = firstDay.close;
            item.active = firstDay.active;
        }
    });
};
</script>

<template>
    <div class="time-range-manager">
        <div class="d-flex justify-content-between align-items-center mb-4">
            <h3 class="text-gdg h5 mb-0">訂位時段設定</h3>
            <button @click="syncAllDays" class="btn btn-outline-secondary btn-sm">
                <i class="bi bi-arrow-repeat"></i> 同步至全週
            </button>
        </div>

        <div class="table-responsive">
            <table class="table align-middle">
                <thead class="table-light">
                    <tr>
                        <th style="width: 120px">星期</th>
                        <th style="width: 80px">開放</th>
                        <th>首個開放訂位時間</th>
                        <th>最後開放訂位時間</th>
                    </tr>
                </thead>
                <tbody>
                    <tr v-for="item in modelValue" :key="item.day" :class="{ 'table-disabled': !item.active }">
                        <td class="fw-bold">{{ daysMap[item.day] }}</td>
                        <td>
                            <div class="form-check form-switch">
                                <input class="form-check-input custom-switch" type="checkbox" v-model="item.active">
                            </div>
                        </td>
                        <td>
                            <input type="time" v-model="item.open" :disabled="!item.active"
                                class="form-control form-control-sm border-gdg">
                        </td>
                        <td>
                            <input type="time" v-model="item.close" :disabled="!item.active"
                                class="form-control form-control-sm border-gdg">
                        </td>
                    </tr>
                </tbody>
            </table>
        </div>
    </div>
</template>

<style scoped>
.border-gdg {
    border-color: #9f9572;
}

.text-gdg {
    color: #9f9572;
}

.custom-switch:checked {
    background-color: #9f9572;
    border-color: #9f9572;
}

.table-disabled {
    background-color: #f8f9fa;
    color: #adb5bd;
}

.form-control:disabled {
    background-color: #eee;
    border-color: #ddd;
    cursor: not-allowed;
}

/* 移除 Table 預設邊框 */
.table th {
    font-size: 0.85rem;
    color: #666;
}
</style>