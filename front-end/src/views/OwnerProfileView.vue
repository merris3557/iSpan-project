<script setup>
import { ref } from 'vue';
import SelectLabel from '@/components/SelectLabel.vue';
import BaseButton from '@/components/common/BaseButton.vue';
import Swal from 'sweetalert2';

const isEditing = ref(false);

const storeDescription = ref('請輸入店家資訊與簡介...');
const myLabels = ref([]);

const addLabel = (newLabel) => {
  if (!myLabels.value.includes(newLabel)) {
    myLabels.value.push(newLabel);
  }
};

const removeLabel = (index) => {
  myLabels.value.splice(index, 1);
};

const handleSave = () => {
  Swal.fire({
    icon: 'success',
    title: '成功儲存資訊！',
    confirmButtonText: '確定',
  });
  isEditing.value = false;
};
</script>

<template>
  <div class="container py-4">
    <h1 class="text-gdg mb-4">店家資訊編輯頁面</h1>

    <div class="info-section mb-4">
      <div v-if="!isEditing">
        <p class="p-3 border bg-light">{{ storeDescription }}</p>
      </div>
      <div v-else>
        <label class="form-label text-gdg fw-bold">編輯簡介：</label>
        <input type="text" v-model="storeDescription" class="form-control border-gdg" placeholder="請輸入店家簡介" />
      </div>
    </div>

    <div class="label-section mb-4">
      <h3 class="text-gdg h5 mb-3">店舖標籤：</h3>
      <div class="tag-list d-flex flex-wrap gap-2">
        <span v-for="(tag, index) in myLabels" :key="tag"
          class="badge rounded-0 border border-gdg text-gdg p-2 d-flex align-items-center">
          {{ tag }}
          <button v-if="isEditing" @click="removeLabel(index)" class="btn-close ms-2"
            style="font-size: 0.5rem;"></button>
        </span>
      </div>

      <div v-if="isEditing" class="edit-box mt-3 p-3 border bg-gdg-light">
        <SelectLabel @add="addLabel" />
      </div>
    </div>

    <div class="mb-5">
      <BaseButton v-if="!isEditing" color="gdg" @click="isEditing = true">編輯資訊</BaseButton>
      <BaseButton v-else color="gdg" @click="handleSave">儲存編輯</BaseButton>
    </div>

    <hr class="my-5">

    <div class="mt-4">
      <h2 class="text-gdg h4 mb-3">訂位編輯系統</h2>
      <RouterLink :to="{ name: 'Seats' }" v-slot="{ navigate }">
        <BaseButton color="gdg" @click="navigate">點擊進入系統</BaseButton>
      </RouterLink>
    </div>
  </div>
</template>

<style scoped>
.border-gdg {
  border-color: #9f9572 !important;
}
</style>