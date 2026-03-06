<script setup>
import { ref, computed, onMounted, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import bookingAPI from '@/api/booking';
import BaseButton from '@/components/common/BaseButton.vue';
import Swal from 'sweetalert2';
import { useAuthStore } from '@/stores/auth'; // 引入你的身份驗證 Store

const authStore = useAuthStore();
const router = useRouter();
const route = useRoute();
const storeId = route.params.id;
const storeConfig = ref(null);
const availableTimeSlots = ref([]);

// 人數增減邏輯
const changeGuestCount = (delta) => {
    const seatType = bookingForm.value.reservedSeatType;
    if (!seatType) return; // 沒選桌型不給動
    const min = Math.max(1, seatType - 1);
    const max = seatType;
    const nextValue = bookingForm.value.guestCount + delta;
    if (nextValue >= min && nextValue <= max) {
        bookingForm.value.guestCount = nextValue;
    }
};

// 判斷是否選完條件後完全沒有可用時段
const isNoVacant = computed(() => {
    // 必須先選好日期與座位類型
    if (!bookingForm.value.bookingDate || !bookingForm.value.reservedSeatType) return false;

    // 如果時段列表示空的，或者所有時段的 available 都是 false，則代表無空位
    return availableTimeSlots.value.length === 0 ||
        availableTimeSlots.value.every(slot => !slot.available);
});

// 座位類型的取得方式
const availableTableTypes = computed(() => {
    if (!storeConfig.value || !storeConfig.value.seatSettings) return [];
    // 過濾掉總數為 0 的桌型，讓該按鈕直接不顯示
    return storeConfig.value.seatSettings
        .filter(s => s.totalCount > 0)
        .map(s => s.seatType);
});

// 日期限制邏輯
const tomorrow = new Date();
tomorrow.setDate(tomorrow.getDate() + 1);
const minDate = tomorrow.toISOString().split('T')[0];
const maxDate = computed(() => {
    const d = new Date();
    d.setMonth(d.getMonth() + 1);
    return d.toISOString().split('T')[0];
});

// BookingForm 欄位
const bookingForm = ref({
    userId: authStore.user?.id || null,
    storeId: storeId,
    reservedSeatType: null,
    bookingDate: '',
    startTime: '',
    guestCount: '',
    guestName: '',
    guestPhone: ''
});

// API 呼叫方法
const fetchStoreConfig = async () => {
    // 增加判斷：確保 ID 存在且不是字串 "undefined"
    if (!storeId || storeId === 'undefined') {
        console.warn('店鋪 ID 無效，不發送請求');
        return;
    }
    const res = await bookingAPI.getBookingConfig(storeId);
    storeConfig.value = res.data || res;
};
const fetchSlots = async () => {
    if (!bookingForm.value.bookingDate || !bookingForm.value.reservedSeatType) return;
    const res = await bookingAPI.getAvailableSlots({
        storeId,
        date: bookingForm.value.bookingDate,
        seatType: bookingForm.value.reservedSeatType
    });
    availableTimeSlots.value = res.data || res;
};

// 重置與送出方法
const resetTime = () => { bookingForm.value.startTime = ''; };
const handleBooking = async () => {
    if (!bookingForm.value.startTime) {
        Swal.fire('請選擇座位、日期與時段', '', 'warning');
        return;
    } else if (!bookingForm.value.guestName || !bookingForm.value.guestPhone) {
        Swal.fire('請填寫姓名與電話', '', 'warning');
        return;
    }
    try {
        // 調用寫好的 POST API
        await bookingAPI.createBooking(bookingForm.value);

        // 成功後的處理
        await Swal.fire('訂位成功！', '', 'success');
        router.push({ name: 'UserBookingsTab' });
    } catch (error) {
        console.error('訂位失敗:', error);

        // 檢查是否有後端回傳的 Validation failed data
        if (error.response && error.response.status === 400 && error.response.data.data) {
            const errorData = error.response.data.data;
            // 把錯誤物件轉換成顯示字串 (例如："手機格式不正確，需為 09 開頭的 10 位數字<br>姓名不能為空")
            const errorMessages = Object.values(errorData).join('<br>');
            
            await Swal.fire({
                icon: 'error',
                title: '資料格式錯誤',
                html: errorMessages // 使用 html 確保換行正常顯示
            });
        } else {
            // 失敗時的處理（包含座位被搶走）
            const errorMsg = error.response?.data?.message || '訂位失敗，請稍後再試。';

            await Swal.fire({
                title: '訂位失敗',
                text: errorMsg,
                icon: 'error',
                confirmButtonText: '確定',
                confirmButtonColor: '#d33' // 使用紅色按鈕警告
            });
            // 失敗後重新整理網頁
            window.location.reload();
        }
    }
};

onMounted(fetchStoreConfig);
watch([() => bookingForm.value.bookingDate, () => bookingForm.value.reservedSeatType], () => {
    resetTime();
    fetchSlots();

    // 切換桌型時，自動將人數調整為該桌型的上限，避免出現人數超過桌型容量的情況 (x)
    const seatType = bookingForm.value.reservedSeatType;
    if (seatType) {
        bookingForm.value.guestCount = seatType;
    }
});
</script>

<template>
    <div class="container py-4">
        <h1 class="text-gdg mb-4">{{ storeConfig?.name || '讀取中...' }}</h1>

        <div class="bg-gdg-light p-4 border mb-4">
            <p class="mb-0 text-muted">
                請選擇您的預約桌型、日期與時段。若特定時段已滿，該選項將不予顯示。
                <br>
                如需進行大型聚會或超過人數上限之預約，請嘗試聯絡店家為您安排。
            </p>
        </div>

        <div class="booking-options">
            <div class="mb-4">
                <label class="form-label d-block fw-bold mb-2">1. 選擇座位類型</label>
                <div class="d-flex flex-wrap gap-2">
                    <button v-for="type in availableTableTypes" :key="type" type="button" class="btn"
                        :class="bookingForm.reservedSeatType === type ? 'btn-gdg' : 'btn-outline-secondary'"
                        @click="bookingForm.reservedSeatType = type; resetTime()">
                        {{ type }} 人座
                    </button>
                </div>
            </div>

            <div class="mb-4">
                <label class="form-label fw-bold mb-2">2. 選擇日期</label>
                <input type="date" class="form-control" v-model="bookingForm.bookingDate" style="max-width: 300px;"
                    :min="minDate" :max="maxDate" @change="resetTime">
            </div>

            <div class="mb-4">
                <label class="form-label d-block fw-bold mb-2">3. 選擇時段（請先選擇座位類型與日期）</label>
                <div class="d-flex flex-wrap gap-2">
                    <template v-for="slot in availableTimeSlots" :key="slot.time">
                        <button v-if="slot.available" type="button" class="btn"
                            :class="bookingForm.startTime === slot.time ? 'btn-gdg' : 'btn-outline-secondary'"
                            @click="bookingForm.startTime = slot.time">
                            {{ slot.time }}
                        </button>
                    </template>
                </div>
                <!-- 無空位提示 -->
                <div v-if="isNoVacant" class="alert alert-warning mt-2 py-2">
                    <i class="bi bi-exclamation-triangle-fill me-2"></i>
                    抱歉，您所選之該日期的座位類型無法提供，請重新選擇預約條件或致電店家洽詢。
                </div>
            </div>

            <div class="mb-4">
                <label class="form-label fw-bold mb-3">4. 填寫預約資訊</label>
                <div class="row g-4">
                    <!-- 姓名 -->
                    <div class="col-md-4">
                        <label class="form-label small text-muted mb-1">預約人姓名</label>
                        <input type="text" class="form-control" placeholder="請輸入姓名" v-model="bookingForm.guestName">
                    </div>

                    <!-- 電話 -->
                    <div class="col-md-4">
                        <label class="form-label small text-muted mb-1">聯絡電話</label>
                        <input type="tel" class="form-control" placeholder="請輸入電話" v-model="bookingForm.guestPhone">
                    </div>

                    <!-- 人數選擇 -->
                    <div class="col-md-4">
                        <label class="form-label small text-muted mb-1">預約人數</label>
                        <div class="input-group" style="max-width: 150px;">
                            <button class="btn btn-outline-secondary" type="button"
                                @click="changeGuestCount(-1)">-</button>
                            <input type="text" class="form-control text-center bg-white"
                                v-model="bookingForm.guestCount" readonly>
                            <button class="btn btn-outline-secondary" type="button"
                                @click="changeGuestCount(1)">+</button>
                        </div>
                        <!-- 動態顯示人數規則說明 -->
                        <div v-if="bookingForm.reservedSeatType" class="form-text small mt-1">
                            註：{{ bookingForm.reservedSeatType }}人座僅限預約 {{ Math.max(1, bookingForm.reservedSeatType - 1)
                            }} ~ {{
                                bookingForm.reservedSeatType }} 人
                        </div>
                    </div>
                </div>
            </div>

            <hr class="my-4">

            <BaseButton color="gdg" class="w-100" @click="handleBooking">
                確認訂位
            </BaseButton>
        </div>
    </div>
</template>