<script setup>
const offDays = defineModel({ type: Array, default: () => [] });

const daysMap = [
    { value: null, label: '請選擇...' },
    { value: 0, label: '星期日' },
    { value: 1, label: '星期一' },
    { value: 2, label: '星期二' },
    { value: 3, label: '星期三' },
    { value: 4, label: '星期四' },
    { value: 5, label: '星期五' },
    { value: 6, label: '星期六' }
];

const addOffDay = () => {
    offDays.value.push({
        offDate: '',
        dayOfWeek: null,
        startTime: '',
        endTime: '',
        type: 'date'
    });
};

const removeOffDay = (index) => {
    offDays.value.splice(index, 1);
};

const handleTypeChange = (item) => {
    if (item.type === 'date') {
        item.dayOfWeek = null;
    } else if (item.type === 'day') {
        item.offDate = '';
    } else if (item.type === 'daily') {
        item.offDate = '';
        item.dayOfWeek = null;
    }
};
</script>

<template>
    <div class="off-day-manager">
        <div class="d-flex justify-content-between align-items-center mb-4">
            <h3 class="text-gdg h5 mb-0">特殊店休設定</h3>
            <button @click="addOffDay" class="btn btn-outline-secondary btn-sm">
                <i class="bi bi-plus-lg"></i> 新增店休
            </button>
        </div>

        <div v-if="offDays.length === 0" class="text-muted text-center py-3 bg-light rounded">
            目前無設定店休日。
        </div>

        <div v-for="(item, index) in offDays" :key="index" class="card mb-3 border-gdg shadow-sm">
            <div class="card-body p-3">
                <div class="row align-items-end g-3">
                    <div class="col-md-2">
                        <label class="form-label small text-muted mb-1">類型</label>
                        <select v-model="item.type" class="form-select border-secondary form-select-sm" @change="handleTypeChange(item)">
                            <option value="date">特定日期</option>
                            <option value="day">常規星期</option>
                            <option value="daily">每天常規</option>
                        </select>
                    </div>

                    <div class="col-md-3" v-if="item.type === 'date'">
                        <label class="form-label small text-muted mb-1">日期</label>
                        <input type="date" v-model="item.offDate" class="form-control border-secondary form-control-sm" />
                    </div>

                    <div class="col-md-3" v-if="item.type === 'day'">
                        <label class="form-label small text-muted mb-1">星期幾</label>
                        <select v-model="item.dayOfWeek" class="form-select border-secondary form-select-sm">
                            <option v-for="d in daysMap" :key="d.value" :value="d.value">{{ d.label }}</option>
                        </select>
                    </div>

                    <div class="col-md-3" v-if="item.type === 'daily'">
                        <label class="form-label small text-muted mb-1">範圍</label>
                        <input type="text" value="每天" disabled class="form-control border-secondary form-control-sm bg-light" />
                    </div>

                    <div class="col-md-2">
                        <label class="form-label small text-muted mb-1">開始時間</label>
                        <input type="time" v-model="item.startTime" class="form-control border-secondary form-control-sm" />
                    </div>
                    
                    <div class="col-md-2">
                        <label class="form-label small text-muted mb-1">結束時間</label>
                        <input type="time" v-model="item.endTime" class="form-control border-secondary form-control-sm" />
                    </div>

                    <div class="col-md-3 mt-3 mt-md-0 d-flex align-items-end">
                        <button class="btn btn-outline-danger btn-sm w-100" @click="removeOffDay(index)">
                            <i class="bi bi-trash"></i> 移除
                        </button>
                    </div>
                </div>
                <div class="mt-2 small text-muted">
                    <i class="bi bi-info-circle text-gdg"></i>
                    <span v-if="!item.startTime && !item.endTime"> 未填寫時間表示全天店休。</span>
                    <span v-else> 設定區間內的預約將會被阻擋。</span>
                </div>
            </div>
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
</style>
