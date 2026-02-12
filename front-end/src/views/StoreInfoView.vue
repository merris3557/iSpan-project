<script setup>
import BaseButton from '@/components/common/BaseButton.vue';
</script>

<template>
  <div class="container py-4">
    <h1 class="text-gdg mb-4">店家頁面(使用者)</h1>

    <div class="mb-4 p-5 bg-gdg-light border text-center">
      <p class="text-muted mb-0">這裡可以放置店家的詳細介紹與圖片內容</p>
    </div>

    <RouterLink :to="{ name: 'Reservation' }" v-slot="{ navigate }">
      <BaseButton color="gdg" @click="navigate">我要訂位</BaseButton>
    </RouterLink>
  </div>
</template>

<style scoped></style>