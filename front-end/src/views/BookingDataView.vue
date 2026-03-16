<script setup>
import { ref, onMounted, computed } from 'vue';
import EditBookingData from '@/components/EditBookingData.vue';
import Swal from 'sweetalert2';
import bookingAPI from '@/api/booking';
import storeAPI from '@/api/store';

const bookingList = ref([]);
const isLoading = ref(false);
const storeId = ref(null);
const showPastBookings = ref(false);

const filteredBookings = computed(() => {
    const now = new Date();

    if (showPastBookings.value) {
        return bookingList.value;
    } else {
        return bookingList.value.filter(b => {
            const bDateTime = new Date(`${b.date}T${b.time}:00`);
            return bDateTime >= now;
        });
    }
});

// 格式化後端資料以符合 EditBookingData 組件需求 (role="shop")
const formatBookingForUI = (b) => {
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
        name: b.guestName,
        phone: b.guestPhone,
        date: b.bookingDate,
        time: timeStr,
        people: b.guestCount,
    };
};

const fetchBookings = async () => {
    try {
        isLoading.value = true;
        // 1. 先取得當前店家的資訊以拿到 storeId
        if (!storeId.value) {
            const storeRes = await storeAPI.getMyStoreInfo();
            // storeRes 可能已被攔截器拆解，或是 ApiResponse 物件
            const storeData = storeRes.data?.storeId ? storeRes.data : storeRes;
            storeId.value = storeData.storeId;
        }

        if (!storeId.value) throw new Error('無法識別店家身分');

        // 2. 根據 storeId 獲取訂位列表
        const response = await bookingAPI.getStoreBookings(storeId.value);
        console.log('Shop Bookings Response:', response);

        const rawList = Array.isArray(response) ? response : (response.data || []);
        bookingList.value = rawList.map(formatBookingForUI);
    } catch (error) {
        console.error('獲取店家訂位失敗:', error);
        Swal.fire('錯誤', '無法載入訂位資料', 'error');
    } finally {
        isLoading.value = false;
    }
};

// 處理刪除的函式 (軟刪除)
const handleDelete = async (booking) => {
    const result = await Swal.fire({
        title: '請確認刪除申請',
        text: `確定刪除 ${booking.name} (編號: ${booking.id}) 的訂位嗎？`,
        icon: 'warning',
        showCancelButton: true,
        confirmButtonColor: '#d33',
        cancelButtonColor: '#3085d6',
        confirmButtonText: '確定刪除',
        cancelButtonText: '取消'
    });

    if (result.isConfirmed) {
        try {
            await bookingAPI.deleteBooking(booking.id);
            bookingList.value = bookingList.value.filter(item => item.id !== booking.id);

            Swal.fire({
                title: '已刪除！',
                text: '該訂位資訊已成功移除。',
                icon: 'success',
                timer: 1500,
                showConfirmButton: false
            });
        } catch (error) {
            console.error('刪除訂位失敗:', error);
            Swal.fire('錯誤', '刪除失敗，請稍後再試', 'error');
        }
    }
};

// 處理更新 (店家可以改日期與時間)
const handleUpdate = async (updatedItem) => {
    try {
        const updateDto = {
            guestName: updatedItem.name,
            guestPhone: updatedItem.phone,
            bookingDate: updatedItem.date,
            startTime: updatedItem.time // 格式如 "18:30"
        };

        // 調用後端專為店家設計的更新端點
        await bookingAPI.updateStoreBooking(updatedItem.id, updateDto);

        // 成功後重新抓取清單，以確保與後端的 endTime 計算結果同步（或者直接更新本地）
        await fetchBookings();

        await Swal.fire({
            icon: 'success',
            title: '資料已更新',
            text: '成功修改訂位日期與時段',
            timer: 1500,
            showConfirmButton: false
        });
    } catch (error) {
        console.error('更新訂位失敗:', error);
        const msg = error.response?.data?.message || '更新失敗，請確認時間格式是否正確';
        Swal.fire('錯誤', msg, 'error');
    }
};

onMounted(fetchBookings);

</script>

<template>
    <div class="container py-4">
        <div class="d-flex justify-content-between align-items-center mb-4">
            <h1 class="text-gdg mb-0">訂位管理系統</h1>
            <div class="form-check form-switch fs-5 text-gdg">
                <input class="form-check-input" type="checkbox" id="showPastSwitch" v-model="showPastBookings" style="cursor: pointer;">
                <label class="form-check-label fw-bold" for="showPastSwitch" style="cursor: pointer;">包含過往訂位</label>
            </div>
        </div>
        <EditBookingData :bookings="filteredBookings" role="shop" @delete="handleDelete" @update="handleUpdate" />
        <!-- 傳給子層時變成bookings與delete -->
    </div>
</template>

<style scoped></style>