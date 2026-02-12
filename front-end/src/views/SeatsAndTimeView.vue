<script setup>
import { ref } from 'vue';
import SeatManager from '@/components/EditSeat.vue';
import TimeSlotManager from '@/components/EditTime.vue';
import BaseButton from '@/components/common/BaseButton.vue';
import Swal from 'sweetalert2';
import AvailTime from '@/components/EditAvailTime.vue';

// 假資料初始狀態
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

const businessHours = ref([
  { day: 'Monday', open: '11:00', close: '21:00', active: true },
  { day: 'Tuesday', open: '11:00', close: '21:00', active: true },
  { day: 'Wednesday', open: '11:00', close: '21:00', active: true },
  { day: 'Thursday', open: '11:00', close: '21:00', active: true },
  { day: 'Friday', open: '11:00', close: '21:00', active: true },
  { day: 'Saturday', open: '11:00', close: '22:00', active: true },
  { day: 'Sunday', open: '11:00', close: '22:00', active: true },
]);

const saveSettings = async () => {
  console.log('儲存設定:', {
    seats: seatData.value,
    config: bookingConfig.value,
    hours: businessHours.value // 這裡會拿到新的時間設定
  });
  // 成功提示
  await Swal.fire({
    icon: 'success',
    title: '資料已更新',
    text: '成功修改座位與時段設定',
    timer: 1500,
    showConfirmButton: false
  });
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