<script setup>
import { ref } from 'vue';
import EditBookingData from '@/components/EditBookingData.vue';
import Swal from 'sweetalert2';

// 模擬資料庫資料
const fakeBookingData = ref([
  {
    id: "1",
    restaurant: "唐門食堂",
    name: "趙活",
    phone: "0912-345-678",
    date: "2026-02-14",
    time: "18:30",
    people: 1,
    tableType: "2人桌",
  },
  {
    id: "2",
    restaurant: "Tahiti大溪地餐廳",
    name: "Arthur Morgan",
    phone: "0922-888-777",
    date: "2026-02-15",
    time: "12:00",
    people: 8,
    tableType: "8人桌",
  }
]);

// 處理子組件傳回來的更新資料 (與店家端相似)
const handleUpdate = async (updatedItem) => {
  const index = fakeBookingData.value.findIndex(item => item.id === updatedItem.id);

  if (index !== -1) {
    // 執行更新
    fakeBookingData.value[index] = { ...updatedItem };

    // 成功提示
    await Swal.fire({
      icon: 'success',
      title: '資料已更新',
      text: '您的個人聯絡資訊修改成功！',
      timer: 1500,
      showConfirmButton: false
    });

    console.log('使用者更新成功:', updatedItem);
  }
};

// 刪除/取消邏輯 (統一命名為 handleDelete 以對應 component 的 emit)
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
    // 這裡通常是呼叫 API
    fakeBookingData.value = fakeBookingData.value.filter(item => item.id !== booking.id);

    Swal.fire('已取消訂位', '我們已更新您的訂位紀錄。', 'success');
  }
};


</script>

<template>
  <div class="container py-4">
    <h1 class="text-gdg mb-4">我的訂位管理</h1>

    <EditBookingData :bookings="fakeBookingData" role="user" @update="handleUpdate" @delete="handleDelete" />

    <div class="mt-4 text-muted small">
      <p>* 提醒：使用者模式僅開放修改「姓名」與「電話」，如需更改時間請致電店家。</p>
    </div>
  </div>
</template>

<style scoped></style>