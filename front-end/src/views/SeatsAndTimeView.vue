<script setup>
import { ref, onMounted } from 'vue';
import SeatManager from '@/components/EditSeat.vue';
import TimeSlotManager from '@/components/EditTime.vue';
import EditOffDay from '@/components/EditOffDay.vue';
import BaseButton from '@/components/common/BaseButton.vue';
import Swal from 'sweetalert2';
import AvailTime from '@/components/EditAvailTime.vue';
import storeAPI from '@/api/store';

// 初始狀態
const seatData = ref([
    { type: 2, count: 0 },
    { type: 4, count: 0 },
    { type: 6, count: 0 },
    { type: 8, count: 0 }
]);

const bookingConfig = ref({
    interval: 30,
    duration: 90
});

const offDaysData = ref([]);

const businessHours = ref([
    { day: 'Monday', open: '11:00', close: '21:00', active: false },
    { day: 'Tuesday', open: '11:00', close: '21:00', active: false },
    { day: 'Wednesday', open: '11:00', close: '21:00', active: false },
    { day: 'Thursday', open: '11:00', close: '21:00', active: false },
    { day: 'Friday', open: '11:00', close: '21:00', active: false },
    { day: 'Saturday', open: '11:00', close: '22:00', active: false },
    { day: 'Sunday', open: '11:00', close: '22:00', active: false },
]);

const dayNameToNum = {
    'Sunday': 0, 'Monday': 1, 'Tuesday': 2, 'Wednesday': 3, 'Thursday': 4, 'Friday': 5, 'Saturday': 6
};
const numToDayName = Object.fromEntries(Object.entries(dayNameToNum).map(([k, v]) => [v, k]));

const daysMap = {
    Monday: '星期一', Tuesday: '星期二', Wednesday: '星期三',
    Thursday: '星期四', Friday: '星期五', Saturday: '星期六', Sunday: '星期日'
};

const fetchSettings = async () => {
    try {
        const res = await storeAPI.getReservationSettings();
        if (res && res.success) {
            const data = res.data;

            // 1. 座位
            if (data.seatSettings) {
                data.seatSettings.forEach(s => {
                    const target = seatData.value.find(item => item.type === s.seatType);
                    if (target) target.count = s.totalCount;
                });
            }

            // 2. 時段
            bookingConfig.value.interval = data.timeSlot || 30;
            bookingConfig.value.duration = data.timeLimit || 90;

            // 3. 營業時間
            if (data.openHours) {
                // 先預設全部沒開
                businessHours.value.forEach(bh => bh.active = false);

                data.openHours.forEach(oh => {
                    const dayName = numToDayName[oh.dayOfWeek];
                    const target = businessHours.value.find(bh => bh.day === dayName);
                    if (target) {
                        target.open = oh.openTime.slice(0, 5); // 拿 HH:mm
                        target.close = oh.closeTime.slice(0, 5);
                        target.active = true;
                    }
                });
            }

            // 4. 店休設定
            if (data.offDays) {
                data.offDays.forEach(od => {
                    if (od.offDate === null && od.dayOfWeek === null) {
                        od.type = 'daily';
                    } else {
                        od.type = od.offDate ? 'date' : 'day';
                    }
                    if (od.startTime) od.startTime = od.startTime.slice(0, 5);
                    if (od.endTime) od.endTime = od.endTime.slice(0, 5);
                });
                offDaysData.value = data.offDays;
            }
        }
    } catch (error) {
        console.error('獲取設定失敗:', error);
        Swal.fire('錯誤', '無法載入預約設定', 'error');
    }
};

onMounted(() => {
    fetchSettings();
});

const saveSettings = async () => {
    try {
        // 1. 營業時間驗證：防止跨日或無效時段
        const invalidHours = businessHours.value.filter(bh => bh.active && bh.close <= bh.open);
        if (invalidHours.length > 0) {
            const dayNames = invalidHours.map(bh => daysMap[bh.day] || bh.day).join('、');
            await Swal.fire({
                icon: 'error',
                title: '營業時間設定錯誤',
                text: `以下日期的結束時間必須晚於開始時間：${dayNames}`,
                confirmButtonColor: '#9f9572'
            });
            return;
        }

        // 2. 店休時間驗證：防止跨日或無效時段
        const invalidOffDays = offDaysData.value.filter(od => {
            if (od.startTime && od.endTime) {
                return od.endTime <= od.startTime;
            }
            return false;
        });

        if (invalidOffDays.length > 0) {
            await Swal.fire({
                icon: 'error',
                title: '店休時間設定錯誤',
                text: '店休的結束時間必須晚於開始時間（不支援跨日設定）',
                confirmButtonColor: '#9f9572'
            });
            return;
        }

        // 3. 店休必填欄位驗證：特定日期與常規星期必須填寫對應欄位
        const missingRequiredOffDays = offDaysData.value.filter(od => {
            if (od.type === 'date' && !od.offDate) return true;
            if (od.type === 'day' && (od.dayOfWeek === null || od.dayOfWeek === '')) return true;
            return false;
        });

        if (missingRequiredOffDays.length > 0) {
            await Swal.fire({
                icon: 'error',
                title: '店休設定不完整',
                text: '選擇「特定日期」時必須指定日期，選擇「常規星期」時必須選擇星期幾。',
                confirmButtonColor: '#9f9572'
            });
            return;
        }

        const payload = {
            timeSlot: bookingConfig.value.interval,
            timeLimit: bookingConfig.value.duration,
            seatSettings: seatData.value.map(s => ({
                seatType: s.type,
                totalCount: s.count
            })),
            openHours: businessHours.value
                .filter(bh => bh.active)
                .map(bh => ({
                    dayOfWeek: dayNameToNum[bh.day],
                    openTime: bh.open,
                    closeTime: bh.close,
                    isClosed: false
                })),
            offDays: offDaysData.value.map(od => ({
                offDate: od.type === 'date' ? od.offDate : null,
                dayOfWeek: od.type === 'day' ? od.dayOfWeek : null,
                startTime: od.startTime || null,
                endTime: od.endTime || null
            }))
        };

        const res = await storeAPI.updateReservationSettings(payload);
        if (res && res.success) {
            await Swal.fire({
                icon: 'success',
                title: '資料已成功更新',
                timer: 1500,
                showConfirmButton: false
            });
        }
    } catch (error) {
        console.error('儲存設定失敗:', error);
        Swal.fire('儲存失敗', error.response?.data?.message || '發生錯誤', 'error');
    }
};

</script>

<template>
    <div class="container py-4">
        <h1 class="text-gdg mb-4">商家編輯畫面</h1>

        <section class="mb-5 p-4 border bg-white">
            <h2 class="h5 text-gdg mb-3">座位管理</h2>
            <SeatManager v-model="seatData" />
        </section>

        <hr class="my-5" />
        <section class="mb-5 p-4 border bg-white">
            <AvailTime v-model="businessHours" />
        </section>

        <section class="mb-5 p-4 border bg-white">
            <EditOffDay v-model="offDaysData" />
        </section>

        <section class="mb-5 p-4 border bg-white">
            <h2 class="h5 text-gdg mb-3">時段配置</h2>
            <TimeSlotManager v-model:interval="bookingConfig.interval" v-model:duration="bookingConfig.duration" />
        </section>

        <div class="text-end">
            <BaseButton color="gdg" size="lg" @click="saveSettings">儲存所有設定</BaseButton>
        </div>

    </div>
</template>

<style scoped>
section {
    transition: all 0.3s ease;
}
</style>