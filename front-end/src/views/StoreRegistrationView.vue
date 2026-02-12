<script setup>
import { reactive, ref } from 'vue';
import axios from 'axios';
import BaseCard from '@/components/common/BaseCard.vue';
import BaseButton from '@/components/common/BaseButton.vue';

const form = reactive({
  ownerName: '',
  storePhone: '',
  storeAddress: ''
});

const isSubmitting = ref(false);

const handleSubmit = async () => {
  if (isSubmitting.value) return;

  isSubmitting.value = true;
  
  // Create a copy of the data for logging/alerting
  const submissionData = { ...form };

  try {
    console.log('Submitting registration data:', submissionData);
    
    // Placeholder for API endpoint
    // In a real scenario, this would be something like:
    // const response = await axios.post('/api/store/register', submissionData);
    
    // Alert the JSON data as requested for verification
    alert(`商家註冊申請已送出 (JSON 預覽):\n${JSON.stringify(submissionData, null, 2)}`);
    
    // Reset form after success (conditional based on actual API result)
    // form.ownerName = '';
    // form.storePhone = '';
    // form.storeAddress = '';
    
  } catch (error) {
    console.error('Registration failed:', error);
    alert('註冊失敗，請稍後再試。');
  } finally {
    isSubmitting.value = false;
  }
};
</script>

<template>
  <div class="store-registration-page bg-gdg-light min-vh-100 py-5">
    <div class="container">
      <div class="row justify-content-center">
        <div class="col-md-8 col-lg-6">
          <BaseCard :shadow="true" :hoverEffect="false">
            <template #header>
              <div class="text-center py-2">
                <h2 class="text-gdg fw-bold mb-0">申請成為商家</h2>
                <p class="text-muted small mt-2">請填寫以下資訊完成註冊申請</p>
              </div>
            </template>

            <form @submit.prevent="handleSubmit" class="p-2">
              <div class="mb-4">
                <label for="ownerName" class="form-label fw-bold text-dark">負責人姓名</label>
                <input 
                  type="text" 
                  id="ownerName" 
                  v-model="form.ownerName" 
                  class="form-control custom-input" 
                  placeholder="請輸入負責人姓名" 
                  required
                >
              </div>

              <div class="mb-4">
                <label for="storePhone" class="form-label fw-bold text-dark">商家電話</label>
                <input 
                  type="tel" 
                  id="storePhone" 
                  v-model="form.storePhone" 
                  class="form-control custom-input" 
                  placeholder="例如: 02-12345678" 
                  required
                >
              </div>

              <div class="mb-4">
                <label for="storeAddress" class="form-label fw-bold text-dark">商家地址</label>
                <textarea 
                  id="storeAddress" 
                  v-model="form.storeAddress" 
                  class="form-control custom-input" 
                  rows="3" 
                  placeholder="請輸入完整的商家營業地址" 
                  required
                ></textarea>
              </div>

              <div class="d-grid gap-2 mt-5">
                <BaseButton 
                  type="submit" 
                  color="gdg" 
                  size="lg" 
                  class="fw-bold" 
                  :disabled="isSubmitting"
                >
                  {{ isSubmitting ? '處理中...' : '提交註冊申請' }}
                </BaseButton>
              </div>
            </form>
          </BaseCard>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.text-gdg {
  color: #9f9572;
}

.custom-input {
  border: 1px solid #ced4da;
  border-radius: 4px; /* Using slightly rounded but clean design */
  padding: 0.75rem 1rem;
  transition: border-color 0.2s, box-shadow 0.2s;
}

.custom-input:focus {
  border-color: #9f9572;
  box-shadow: 0 0 0 0.2rem rgba(159, 149, 114, 0.1);
  outline: none;
}

/* Match the sharp design mentioned in custom.scss comments if preferred */
/* .custom-input { border-radius: 0; } */
</style>
