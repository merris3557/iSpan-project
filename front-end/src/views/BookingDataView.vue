<script setup>
import { ref } from 'vue';
import EditBookingData from '@/components/EditBookingData.vue';
import Swal from 'sweetalert2';

// 模擬假資料：訂位編號、姓名、電話、日期、時間、人數
const bookingList = ref([
  { id: '1', name: '趙活', phone: '0912-345-678', date: '2026-04-01', time: '18:00', people: 2 },
  { id: '2', name: 'Arthur Morgan', phone: '0988-888-888', date: '2026-04-02', time: '12:30', people: 4 },
  { id: '3', name: 'Geralt', phone: '0977-777-777', date: '2024-06-03', time: '12:00', people: 2 },
]);

// 處理刪除的函式 (改為 async/await 寫法)
const handleDelete = async (bookings) => {
  // 1. 先彈出 Swal 詢問視窗
  const result = await Swal.fire({
    title: '請確認刪除申請',
    text: `確定刪除 ${bookings.name} (編號: ${bookings.id}) 的訂位嗎？`,
    icon: 'warning',
    showCancelButton: true,
    confirmButtonColor: '#d33',
    cancelButtonColor: '#3085d6',
    confirmButtonText: '確定刪除',
    cancelButtonText: '取消'
  });

  // 2. 如果使用者點擊「確定」 (result.isConfirmed 為 true)
  if (result.isConfirmed) {
    // 執行過濾邏輯（刪除資料）
    bookingList.value = bookingList.value.filter(item => item.id !== bookings.id);

    // 3. 顯示成功提示
    Swal.fire({
      title: '已刪除！',
      text: '該訂位資訊已成功移除。',
      icon: 'success',
      timer: 1500,
      showConfirmButton: false
    });

    console.log('已刪除 ID:', bookings.id);
  }
};

// 處理更新
const handleUpdate = async (updatedItem) => {
  const index = bookingList.value.findIndex(item => item.id === updatedItem.id);
  if (index !== -1) {
    // 使用展開運算子，確保 Vue 絕對能偵測到物件內容的變動
    bookingList.value[index] = { ...updatedItem };
    // 成功提示
    await Swal.fire({
      icon: 'success',
      title: '資料已更新',
      text: '成功修改訂位日期與時段',
      timer: 1500,
      showConfirmButton: false
    });

    console.log('資料更新成功:', updatedItem);
  }
};

// 更新成功彈窗

</script>

<template>
  <div class="container py-4">
    <h1 class="text-gdg mb-4">訂位管理系統</h1>
    <EditBookingData :bookings="bookingList" role="shop" @delete="handleDelete" @update="handleUpdate" />
    <!-- 傳給子層時變成bookings與delete -->
  </div>
</template>

<style scoped></style>