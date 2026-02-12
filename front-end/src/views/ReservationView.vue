<script setup>
import { ref, computed } from 'vue';
import { useRouter } from 'vue-router';
import BaseButton from '@/components/common/BaseButton.vue';
import Swal from 'sweetalert2';

const router = useRouter();

// --- 1. 商店配置 (對應資料表 stores_info 與 seats 的統計) ---
const storeConfig = {
  name: '唐門食堂',
  timeLimit: 90, // 用餐限制 (對應 stores_info.time_limit)
  // 模擬資料庫計算後的總桌數：2人座3張, 4人座2張...
  tables: {
    2: 3,
    4: 2,
    6: 1,
    8: 1
  }
};

// 取得所有可用的桌型 (用於生成按鈕)
const availableTableTypes = Object.keys(storeConfig.tables).map(Number);

// 固定開放時段 (可根據資料表動態生成，目前先固定)
const timeSlots = ['12:00', '14:00', '16:00', '18:00', '20:00'];

// --- 2. 模擬已訂位資料 (對應 booking 表，需含起訖時間) ---
const bookedData = ref([
  { date: '2026-02-10', startTime: '12:00', endTime: '13:30', type: 2 },
  { date: '2026-02-10', startTime: '12:30', endTime: '14:00', type: 2 },
  { date: '2026-02-10', startTime: '18:00', endTime: '19:30', type: 8 },
  { date: '2026-02-10', startTime: '13:00', endTime: '14:30', type: 2 },
]);

// --- 3. 使用者選擇狀態 ---
const isLogin = ref(true);
const bookingForm = ref({
  seatType: null,
  date: '',
  time: ''
});

// --- 4. 核心判斷邏輯：重疊判定 ---
const isSlotAvailable = (targetTime) => {
  if (!bookingForm.value.date || !bookingForm.value.seatType) return true;

  const targetDate = bookingForm.value.date;
  const targetType = bookingForm.value.seatType;

  // 計算目標時段的起訖分鐘數
  const [h, m] = targetTime.split(':').map(Number);
  const targetStartTotal = h * 60 + m;
  const targetEndTotal = targetStartTotal + storeConfig.timeLimit;

  // 篩選出同日期、同桌型且時間有重疊的訂位
  const overlaps = bookedData.value.filter(slot => {
    if (slot.date !== targetDate || slot.type !== targetType) return false;

    const [sH, sM] = slot.startTime.split(':').map(Number);
    const [eH, eM] = slot.endTime.split(':').map(Number);
    const slotStartTotal = sH * 60 + sM;
    const slotEndTotal = eH * 60 + eM;

    // 重疊判定：(開始A < 結束B) 且 (結束A > 開始B)
    return targetStartTotal < slotEndTotal && targetEndTotal > slotStartTotal;
  });

  // 如果佔用桌數 < 總桌數，則該時段可選
  return overlaps.length < storeConfig.tables[targetType];
};

// --- 5. 其他輔助邏輯 ---
// 1. 取得明天 (minDate)
const tomorrow = new Date();
tomorrow.setDate(tomorrow.getDate() + 1);

const minDate = `${tomorrow.getFullYear()}-${String(tomorrow.getMonth() + 1).padStart(2, '0')}-${String(tomorrow.getDate()).padStart(2, '0')}`;

// 2. 取得一個月後 (maxDate)
const maxDate = computed(() => {
  const targetDate = new Date();
  targetDate.setMonth(targetDate.getMonth() + 1); // 自動處理跨年邏輯
  
  const y = targetDate.getFullYear();
  const m = String(targetDate.getMonth() + 1).padStart(2, '0');
  const d = String(targetDate.getDate()).padStart(2, '0');
  
  return `${y}-${m}-${d}`;
});

const resetTime = () => { bookingForm.value.time = ''; };

const handleBooking = () => {
  if (!bookingForm.value.seatType || !bookingForm.value.date || !bookingForm.value.time) {
    Swal.fire({ icon: 'error', title: '請完整選擇預約資訊！' });
    return;
  }

  if (!isLogin.value) {
    console.log('未登入預約資料：', bookingForm.value);
    Swal.fire('請先登入', '預約功能需要會員權限', 'info');
  } else {
    Swal.fire('訂位成功！', `${bookingForm.value.date} ${bookingForm.value.time}`, 'success');
    router.push({ name: 'UserBookings' });
  }
};
</script>

<template>
  <div class="container py-4">
    <h1 class="text-gdg mb-4">{{ storeConfig.name }}</h1>

    <div class="bg-gdg-light p-4 border mb-4">
      <p class="mb-0 text-muted">
        請選擇您的預約資訊。若該時段桌位已滿，按鈕將會隱藏。
      </p>
    </div>

    <div class="booking-options">
      <div class="mb-4">
        <label class="form-label d-block fw-bold mb-2">1. 選擇座位類型</label>
        <div class="d-flex flex-wrap gap-2">
          <button v-for="type in availableTableTypes" :key="type" type="button" class="btn"
            :class="bookingForm.seatType === type ? 'btn-gdg' : 'btn-outline-secondary'"
            @click="bookingForm.seatType = type; resetTime()">
            {{ type }} 人座
          </button>
        </div>
      </div>

      <div class="mb-4">
        <label class="form-label fw-bold mb-2">2. 選擇日期</label>
        <input type="date" class="form-control" v-model="bookingForm.date" style="max-width: 300px;" :min="minDate"
          :max="maxDate" @change="resetTime">
      </div>

      <div class="mb-4">
        <label class="form-label d-block fw-bold mb-2">3. 選擇時段</label>
        <div class="d-flex flex-wrap gap-2">
          <template v-for="slot in timeSlots" :key="slot">
            <button v-if="isSlotAvailable(slot)" type="button" class="btn"
              :class="bookingForm.time === slot ? 'btn-gdg' : 'btn-outline-secondary'" @click="bookingForm.time = slot">
              {{ slot }}
            </button>
          </template>
        </div>
      </div>

      <hr class="my-4">

      <BaseButton color="gdg" class="w-100" @click="handleBooking">
        確認訂位
      </BaseButton>
    </div>
  </div>
</template>