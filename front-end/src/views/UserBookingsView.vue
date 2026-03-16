<script setup>
import { ref, onMounted, computed } from 'vue';
import EditBookingData from '@/components/EditBookingData.vue';
import Swal from 'sweetalert2';
import api from '@/api/config';
import bookingAPI from '@/api/booking';
import { useAuthStore } from '@/stores/auth';

const authStore = useAuthStore();
const userId = computed(() => authStore.user?.id);

// 訂位資料列表
const bookings = ref([]);
const isLoading = ref(false);

// 格式化後端資料以符合 EditBookingData 組件需求
const formatBookingForUI = (b) => {
    // 處理時間格式：有些序列化會是 [18, 30] 陣列，有些是 "18:30:00" 字串
    let timeStr = '00:00';
    if (typeof b.startTime === 'string') {
        timeStr = b.startTime.substring(0, 5);
    } else if (Array.isArray(b.startTime)) {
        const hh = String(b.startTime[0]).padStart(2, '0');
        const mm = String(b.startTime[1]).padStart(2, '0');
        timeStr = `${hh}:${mm}`;
    }

    return {
        id: b.bookingId,
        restaurant: b.storeName,
        name: b.guestName,
        phone: b.guestPhone,
        date: b.bookingDate,
        time: timeStr,
        people: b.guestCount,
        tableType: `${b.reservedSeatType}人桌`
    };
};

// 取得訂位列表
const fetchBookings = async () => {
    if (!userId.value) {
        console.warn('userId 不存在，無法獲取訂位列表');
        return;
    }

    try {
        isLoading.value = true;
        // 調用後端 BookingController 的 @GetMapping("/user/{userId}")
        // 注意：依照專案慣例，後端會包裝成 { success: true, data: [...] }
        const response = await bookingAPI.getUserBookings(userId.value);
        console.log('API Response:', response);

        // 如果 response 本身就是陣列，直接使用；如果是 ApiResponse 物件，取 .data
        const rawList = Array.isArray(response) ? response : (response.data || []);
        
        // 過濾掉過期的訂位 (包含當日時間已過)
        const now = new Date();

        bookings.value = rawList
            .map(formatBookingForUI)
            .filter(b => {
                // b.date 格式為 YYYY-MM-DD, b.time 格式為 HH:mm
                const bDateTime = new Date(`${b.date}T${b.time}:00`);
                return bDateTime >= now;
            });
    } catch (error) {
        console.error('獲取訂位列表失敗:', error);
        Swal.fire('錯誤', '無法載入訂位紀錄，請確認後端服務是否正常', 'error');
    } finally {
        isLoading.value = false;
    }
};

// 處理子組件傳回來的更新資料
const handleUpdate = async (updatedItem) => {
    try {
        const updateDto = {
            guestName: updatedItem.name,
            guestPhone: updatedItem.phone
        };

        // 調用後端 BookingController 的 @PutMapping("/{bookingId}")
        await bookingAPI.updateUserBooking(updatedItem.id, updateDto);

        // 更新本地狀態
        const index = bookings.value.findIndex(item => item.id === updatedItem.id);
        if (index !== -1) {
            bookings.value[index] = { ...updatedItem };
        }

        await Swal.fire({
            icon: 'success',
            title: '資料已更新',
            text: '您的個人聯絡資訊修改成功！',
            timer: 1500,
            showConfirmButton: false
        });
    } catch (error) {
    console.error('更新訂位失敗:', error);
    // 檢查是否有後端回傳的 Validation failed data
    if (error.response && error.response.status === 400 && error.response.data.data) {
        const errorData = error.response.data.data;
        // 把錯誤物件轉換成顯示字串 (例如："手機格式不正確，需為 09 開頭的 10 位數字<br>姓名不能為空")
        const errorMessages = Object.values(errorData).join('<br>');
        
        Swal.fire({
            icon: 'error',
            title: '資料格式錯誤',
            html: errorMessages // 使用 html 確保換行正常顯示
        });
    } else {
        // 其他未知錯誤
        Swal.fire('錯誤', '更新失敗，請稍後再試', 'error');
    }
}
};

// 刪除/取消邏輯
const handleDelete = async (booking) => {
    const result = await Swal.fire({
        title: '確定要取消訂位嗎？',
        text: `您即將取消 ${booking.restaurant} 的訂位。`,
        icon: 'warning',
        showCancelButton: true,
        confirmButtonColor: '#d33',
        cancelButtonColor: '#3085d6',
        confirmButtonText: '確定取消',
        cancelButtonText: '再想想'
    });

    if (result.isConfirmed) {
        try {
            // 調用後端 BookingController 的 @DeleteMapping("/{bookingId}")
            await bookingAPI.deleteBooking(booking.id);

            // 更新本地狀態
            bookings.value = bookings.value.filter(item => item.id !== booking.id);

            Swal.fire('已取消訂位', '我們已更新您的訂位紀錄。', 'success');
        } catch (error) {
            console.error('取消訂位失敗:', error);
            Swal.fire('錯誤', '取消失敗，請稍後再試', 'error');
        }
    }
};

onMounted(() => {
    console.log('UserBookingsView mounted, userId:', userId.value);
    if (authStore.checkAuth()) {
        fetchBookings();
    } else {
        authStore.handleLogoutAndNotify('unauthorized');
    }
});

</script>

<template>
    <div class="user-bookings-view">
        <h4 class="mb-3 border-bottom pb-2">我的訂位</h4>

        <div class="user-bookings-content">
            <div v-if="isLoading" class="text-center py-5">
                <div class="spinner-border text-gdg" role="status">
                    <span class="visually-hidden">載入中...</span>
                </div>
            </div>

            <template v-else>
                <div class="card shadow-sm border-0">
                    <div class="card-header bg-white border-bottom-0 pt-4 pb-2">
                        <h5 class="mb-0 text-gdg fw-bold"><i class="bi bi-calendar-check-fill me-2"></i>訂位紀錄管理</h5>
                    </div>
                    <div class="card-body">
                        <EditBookingData :bookings="bookings" role="user" @update="handleUpdate" @delete="handleDelete" />

                        <div v-if="bookings.length === 0" class="text-center py-5 text-muted">
                            <p class="mb-3">目前沒有訂位紀錄</p>
                            <router-link to="/mapSearch" class="btn btn-gdg text-white px-4">
                                <i class="bi bi-calendar-plus me-2"></i>現在就去訂位
                            </router-link>
                        </div>
                    </div>
                </div>
            </template>

            <div class="mt-4 text-muted small p-3 bg-light rounded border-start border-4 border-gdg">
                <p class="mb-0"><i class="bi bi-info-circle me-2"></i>提醒：使用者模式僅開放修改「姓名」與「電話」，如需更改時間請致電店家。</p>
            </div>
        </div>
    </div>
</template>

<style scoped>
.text-gdg {
    color: #9f9572 !important;
}

.bg-gdg {
    background-color: #9f9572 !important;
}

.btn-gdg {
    background-color: #9f9572 !important;
    border-color: #9f9572 !important;
}

.btn-gdg:hover {
    background-color: #8f8562 !important;
    border-color: #8f8562 !important;
}

.border-gdg {
    border-color: #9f9572 !important;
}
</style>
