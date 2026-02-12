<template>
  <div class="feedback-page">
    <!-- Header Section -->
    <div class="feedback-header">
      <div class="header-content">
        <div class="icon-wrapper">
          <i class="bi bi-chat-dots"></i>
        </div>
        <div class="header-text">
          <h1 class="page-title">客戶回饋</h1>
          <p class="page-subtitle">您的意見對我們很重要，感謝您撥冗填寫</p>
        </div>
      </div>
    </div>

    <!-- Main Form Section -->
    <div class="feedback-container">
      <div class="form-card">
        <!-- Success Message -->
        <div v-if="showSuccessMessage" class="success-banner">
          <i class="bi bi-check-circle-fill"></i>
          <div>
            <strong>感謝您的回饋！</strong>
            <p>回報編號：<span class="report-number">{{ reportNumber }}</span></p>
            <p>我們已收到您的意見，確認信已寄送至您的電子郵件</p>
          </div>
        </div>

        <form @submit.prevent="handleSubmit" class="feedback-form">
          <!-- Contact Name -->
          <div class="form-group">
            <label for="contactName" class="form-label required">
              <i class="bi bi-person"></i>
              聯絡人姓名
            </label>
            <input
              id="contactName"
              v-model="formData.contactName"
              type="text"
              class="form-control"
              :class="{ 'is-invalid': errors.contactName }"
              placeholder="請輸入您的姓名"
              @input="clearError('contactName')"
            />
            <div v-if="errors.contactName" class="error-message">
              <i class="bi bi-exclamation-circle"></i>
              {{ errors.contactName }}
            </div>
          </div>

          <!-- Email (Now Required) -->
          <div class="form-group">
            <label for="email" class="form-label required">
              <i class="bi bi-envelope"></i>
              電子郵件
            </label>
            <input
              id="email"
              v-model="formData.email"
              type="email"
              class="form-control"
              :class="{ 'is-invalid': errors.email }"
              placeholder="example@email.com"
              @input="clearError('email')"
            />
            <div v-if="errors.email" class="error-message">
              <i class="bi bi-exclamation-circle"></i>
              {{ errors.email }}
            </div>
            <small class="form-hint">我們將透過此信箱回覆您的問題</small>
          </div>

          <!-- Contact Phone -->
          <div class="form-group">
            <label for="contactPhone" class="form-label">
              <i class="bi bi-telephone"></i>
              連絡電話 <span class="optional-badge">選填</span>
            </label>
            <input
              id="contactPhone"
              v-model="formData.contactPhone"
              type="tel"
              class="form-control"
              :class="{ 'is-invalid': errors.contactPhone }"
              placeholder="例如：0912-345-678"
              @input="clearError('contactPhone')"
            />
            <div v-if="errors.contactPhone" class="error-message">
              <i class="bi bi-exclamation-circle"></i>
              {{ errors.contactPhone }}
            </div>
          </div>

          <!-- Issue Type -->
          <div class="form-group">
            <label for="issueType" class="form-label required">
              <i class="bi bi-tag"></i>
              反應問題種類
            </label>
            <div class="select-wrapper">
              <select
                id="issueType"
                v-model="formData.issueType"
                class="form-select"
                :class="{ 'is-invalid': errors.issueType }"
                @change="clearError('issueType')"
              >
                <option value="" disabled>請選擇問題種類</option>
                <option value="商店資訊有誤">商店資訊有誤</option>
                <option value="網站使用回饋">網站使用回饋</option>
                <option value="服務品質問題">服務品質問題</option>
                <option value="功能建議">功能建議</option>
                <option value="技術問題">技術問題</option>
                <option value="其他">其他</option>
              </select>
              <i class="bi bi-chevron-down select-icon"></i>
            </div>
            <div v-if="errors.issueType" class="error-message">
              <i class="bi bi-exclamation-circle"></i>
              {{ errors.issueType }}
            </div>
          </div>

          <!-- Feedback Content -->
          <div class="form-group">
            <label for="feedbackContent" class="form-label required">
              <i class="bi bi-chat-left-text"></i>
              回饋內容
            </label>
            <textarea
              id="feedbackContent"
              v-model="formData.feedbackContent"
              class="form-textarea"
              :class="{ 'is-invalid': errors.feedbackContent }"
              placeholder="請詳細描述您的問題或建議..."
              rows="6"
              maxlength="500"
              @input="clearError('feedbackContent')"
            ></textarea>
            <div class="textarea-footer">
              <div v-if="errors.feedbackContent" class="error-message">
                <i class="bi bi-exclamation-circle"></i>
                {{ errors.feedbackContent }}
              </div>
              <div class="char-counter" :class="{ 'text-warning': characterCount > 450, 'text-danger': characterCount >= 500 }">
                {{ characterCount }} / 500
              </div>
            </div>
          </div>

          <!-- Privacy Notice -->
          <div class="privacy-notice">
            <i class="bi bi-shield-check"></i>
            <p>您的個人資料將受到保護，僅用於處理此回饋事項</p>
          </div>

          <!-- Submit Button -->
          <div class="form-actions">
            <button type="button" @click="resetForm" class="btn btn-outline-gdg btn-reset" :disabled="isSubmitting">
              <i class="bi bi-arrow-counterclockwise"></i>
              重設表單
            </button>
            <button type="submit" class="btn-submit btn-gdg" :disabled="isSubmitting">
              <i class="bi" :class="isSubmitting ? 'bi-hourglass-split' : 'bi-send'"></i>
              {{ isSubmitting ? '送出中...' : '送出回饋' }}
            </button>
          </div>
        </form>
      </div>

      <!-- Info Cards -->
      <div class="info-section">
        <div class="info-card line-card">
          <div class="info-icon line-icon">
            <i class="bi bi-line"></i>
          </div>
          <h3>LINE@ 官方帳號</h3>
          <p class="line-id">@speedearn</p>
          <div class="qr-code-container">
            <img src="/src/assets/line_qr_code.png" alt="LINE QR Code" class="qr-code">
            <small>掃描加入好友</small>
          </div>
        </div>

        <div class="info-card">
          <div class="info-icon">
            <i class="bi bi-headset"></i>
          </div>
          <h3>客服專線</h3>
          <p>0800-123-456<br><small>週一至週五 09:00-18:00</small></p>
        </div>

        <div class="info-card">
          <div class="info-icon">
            <i class="bi bi-envelope-at"></i>
          </div>
          <h3>電子郵件</h3>
          <p>support@speedearn.com</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, computed } from 'vue';

export default {
  name: 'FeedbackView',
  setup() {
    const formData = ref({
      contactName: '',
      contactPhone: '',
      email: '',
      issueType: '',
      feedbackContent: ''
    });

    const errors = ref({});
    const isSubmitting = ref(false);
    const showSuccessMessage = ref(false);
    const reportNumber = ref('');

    const characterCount = computed(() => formData.value.feedbackContent.length);

    const validateForm = () => {
      errors.value = {};
      let isValid = true;

      // Validate contact name
      if (!formData.value.contactName.trim()) {
        errors.value.contactName = '請輸入聯絡人姓名';
        isValid = false;
      } else if (formData.value.contactName.trim().length < 2) {
        errors.value.contactName = '姓名至少需要 2 個字元';
        isValid = false;
      }

      // Validate phone (now optional)
      if (formData.value.contactPhone.trim()) {
        const phoneRegex = /^[0-9-+()]*$/;
        if (!phoneRegex.test(formData.value.contactPhone)) {
          errors.value.contactPhone = '電話格式不正確';
          isValid = false;
        }
      }

      // Validate email (now required)
      if (!formData.value.email.trim()) {
        errors.value.email = '請輸入電子郵件';
        isValid = false;
      } else {
        const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        if (!emailRegex.test(formData.value.email)) {
          errors.value.email = '電子郵件格式不正確';
          isValid = false;
        }
      }

      // Validate issue type
      if (!formData.value.issueType) {
        errors.value.issueType = '請選擇問題種類';
        isValid = false;
      }

      // Validate feedback content
      if (!formData.value.feedbackContent.trim()) {
        errors.value.feedbackContent = '請輸入回饋內容';
        isValid = false;
      } else if (formData.value.feedbackContent.trim().length < 10) {
        errors.value.feedbackContent = '回饋內容至少需要 10 個字元';
        isValid = false;
      }

      return isValid;
    };

    const clearError = (field) => {
      if (errors.value[field]) {
        delete errors.value[field];
      }
    };

    const handleSubmit = async () => {
      if (!validateForm()) {
        // Show first error field
        const firstErrorField = Object.keys(errors.value)[0];
        if (firstErrorField) {
          const element = document.getElementById(firstErrorField);
          if (element) {
            element.focus();
            element.scrollIntoView({ behavior: 'smooth', block: 'center' });
          }
        }
        return;
      }

      isSubmitting.value = true;

      try {
        // Generate report number
        const timestamp = Date.now();
        const random = Math.floor(Math.random() * 10000).toString().padStart(4, '0');
        reportNumber.value = `FB${timestamp.toString().slice(-6)}${random}`;

        // Simulate API call
        await new Promise(resolve => setTimeout(resolve, 1500));

        // Show success banner
        showSuccessMessage.value = true;

        // Reset form after showing success
        setTimeout(() => {
          resetForm();
          showSuccessMessage.value = false;
        }, 5000);

        // Scroll to top to show success message
        window.scrollTo({ top: 0, behavior: 'smooth' });

      } catch (error) {
        alert('送出失敗，請稍後再試');
      } finally {
        isSubmitting.value = false;
      }
    };

    const resetForm = () => {
      formData.value = {
        contactName: '',
        contactPhone: '',
        email: '',
        issueType: '',
        feedbackContent: ''
      };
      errors.value = {};
    };

    return {
      formData,
      errors,
      isSubmitting,
      showSuccessMessage,
      reportNumber,
      characterCount,
      handleSubmit,
      clearError,
      resetForm
    };
  }
};
</script>

<style lang="scss" scoped>
@import '@/assets/styles/custom.scss';

.feedback-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  padding: 2rem 1rem;
}

// Header Section
.feedback-header {
  max-width: 1200px;
  margin: 0 auto 2rem;
  text-align: center;

  .header-content {
    display: inline-flex;
    align-items: center;
    gap: 1.5rem;
    background: #fff;
    padding: 1.5rem 3rem;
    border-radius: 16px;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  }

  .icon-wrapper {
    width: 60px;
    height: 60px;
    background: linear-gradient(135deg, $gdg-gold 0%, $gdg-gold-dark 100%);
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;

    i {
      font-size: 2rem;
      color: #fff;
    }
  }

  .header-text {
    text-align: left;
  }

  .page-title {
    font-size: 2rem;
    font-weight: 700;
    color: #1f2937;
    margin: 0 0 0.25rem 0;
  }

  .page-subtitle {
    color: #6b7280;
    font-size: 1rem;
    margin: 0;
  }
}

// Main Container
.feedback-container {
  max-width: 1200px;
  margin: 0 auto;
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: 2rem;
  align-items: start;

  @media (max-width: 968px) {
    grid-template-columns: 1fr;
  }
}

// Form Card
.form-card {
  background: #fff;
  border-radius: 16px;
  padding: 2.5rem;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
}

// Success Banner
.success-banner {
  background: linear-gradient(135deg, #d1fae5 0%, #a7f3d0 100%);
  border-left: 4px solid #10b981;
  padding: 1.5rem;
  border-radius: 12px;
  display: flex;
  align-items: start;
  gap: 1rem;
  margin-bottom: 2rem;
  animation: slideDown 0.5s ease-out;

  i {
    font-size: 1.5rem;
    color: #065f46;
    margin-top: 0.25rem;
  }

  strong {
    display: block;
    color: #065f46;
    font-size: 1.125rem;
    margin-bottom: 0.25rem;
  }

  p {
    color: #064e3b;
    margin: 0 0 0.25rem 0;
    font-size: 0.875rem;

    &:last-child {
      margin-bottom: 0;
    }
  }

  .report-number {
    font-family: 'Courier New', monospace;
    font-weight: 700;
    color: #047857;
    background-color: rgba(255, 255, 255, 0.6);
    padding: 0.125rem 0.5rem;
    border-radius: 4px;
    font-size: 0.9375rem;
  }
}

@keyframes slideDown {
  from {
    opacity: 0;
    transform: translateY(-20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

// Form Styles
.feedback-form {
  .form-group {
    margin-bottom: 1.75rem;
  }

  .form-label {
    display: flex;
    align-items: center;
    gap: 0.5rem;
    font-size: 0.9375rem;
    font-weight: 600;
    color: #374151;
    margin-bottom: 0.625rem;

    i {
      color: $gdg-gold;
      font-size: 1.125rem;
    }

    &.required::after {
      content: '*';
      color: #ef4444;
      margin-left: 0.25rem;
    }
  }

  .optional-badge {
    display: inline-block;
    background-color: #e5e7eb;
    color: #6b7280;
    font-size: 0.75rem;
    font-weight: 500;
    padding: 0.125rem 0.5rem;
    border-radius: 4px;
    margin-left: 0.5rem;
  }

  .form-control,
  .form-select,
  .form-textarea {
    width: 100%;
    padding: 0.875rem 1rem;
    border: 2px solid #e5e7eb;
    border-radius: 10px;
    font-size: 0.9375rem;
    color: #1f2937;
    background-color: #fff;
    transition: all 0.3s ease;

    &:focus {
      outline: none;
      border-color: $gdg-gold;
      box-shadow: 0 0 0 4px rgba(159, 149, 114, 0.1);
    }

    &.is-invalid {
      border-color: #ef4444;
    }

    &::placeholder {
      color: #9ca3af;
    }
  }

  .form-textarea {
    resize: vertical;
    min-height: 120px;
    font-family: inherit;
  }

  .select-wrapper {
    position: relative;

    .form-select {
      appearance: none;
      padding-right: 3rem;
      cursor: pointer;
    }

    .select-icon {
      position: absolute;
      right: 1rem;
      top: 50%;
      transform: translateY(-50%);
      pointer-events: none;
      color: #6b7280;
      font-size: 1rem;
    }
  }

  .form-hint {
    display: block;
    margin-top: 0.5rem;
    color: #6b7280;
    font-size: 0.8125rem;
  }

  .error-message {
    display: flex;
    align-items: center;
    gap: 0.375rem;
    color: #ef4444;
    font-size: 0.8125rem;
    margin-top: 0.5rem;

    i {
      font-size: 0.875rem;
    }
  }

  .textarea-footer {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-top: 0.5rem;
  }

  .char-counter {
    font-size: 0.8125rem;
    color: #6b7280;
    font-weight: 500;

    &.text-warning {
      color: #f59e0b;
    }

    &.text-danger {
      color: #ef4444;
    }
  }
}

// Privacy Notice
.privacy-notice {
  background-color: $gdg-bg-light;
  border-radius: 10px;
  padding: 1rem 1.25rem;
  display: flex;
  align-items: center;
  gap: 0.75rem;
  margin-bottom: 2rem;

  i {
    font-size: 1.5rem;
    color: $gdg-gold;
  }

  p {
    margin: 0;
    color: #6b7280;
    font-size: 0.875rem;
  }
}

// Form Actions
.form-actions {
  display: flex;
  gap: 1rem;
  justify-content: flex-end;

  @media (max-width: 640px) {
    flex-direction: column-reverse;

    .btn {
      width: 100%;
    }
  }

  .btn {
    display: inline-flex;
    align-items: center;
    gap: 0.5rem;
    padding: 0.875rem 2rem;
    font-size: 1rem;
    font-weight: 600;
    border-radius: 10px;
    transition: all 0.3s ease;
    cursor: pointer;

    i {
      font-size: 1.125rem;
    }

    &:disabled {
      opacity: 0.6;
      cursor: not-allowed;
    }

    // Reduce reset button width by 1/3
    &.btn-reset {
      padding: 0.875rem 1.35rem; // Reduced from 2rem to 1.35rem (~33% reduction)
    }
  }

  .btn-submit {
    &:not(:disabled):hover {
      transform: translateY(-2px);
      box-shadow: 0 8px 16px rgba(159, 149, 114, 0.3);
    }
  }
}

// Info Section
.info-section {
  display: flex;
  flex-direction: column;
  gap: 1rem;
  max-width: 250px; // Reduce width by 1/3 (from ~375px to 250px)
}

.info-card {
  background: #fff;
  border-radius: 12px;
  padding: 1.25rem;
  text-align: center;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  transition: all 0.3s ease;

  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.12);
  }

  .info-icon {
    width: 48px;
    height: 48px;
    margin: 0 auto 0.75rem;
    background: linear-gradient(135deg, $gdg-gold 0%, $gdg-gold-dark 100%);
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;

    i {
      font-size: 1.5rem;
      color: #fff;
    }

    &.line-icon {
      background: linear-gradient(135deg, #00B900 0%, #00A300 100%);
    }
  }

  h3 {
    font-size: 1rem;
    font-weight: 700;
    color: #1f2937;
    margin: 0 0 0.5rem 0;
  }

  p {
    color: #6b7280;
    font-size: 0.875rem;
    margin: 0;
    line-height: 1.5;

    small {
      display: block;
      font-size: 0.75rem;
      color: #9ca3af;
      margin-top: 0.25rem;
    }
  }

  // Line Card Specific Styles
  &.line-card {
    .line-id {
      font-family: 'Courier New', monospace;
      font-weight: 600;
      color: #00B900;
      font-size: 1rem;
      margin-bottom: 1rem;
    }

    .qr-code-container {
      display: flex;
      flex-direction: column;
      align-items: center;
      gap: 0.5rem;
      margin-top: 0.75rem;

      .qr-code {
        width: 120px;
        height: 120px;
        border: 2px solid #e5e7eb;
        border-radius: 8px;
        padding: 0.5rem;
        background: #fff;
      }

      small {
        color: #6b7280;
        font-size: 0.75rem;
      }
    }
  }
}

// Responsive
@media (max-width: 768px) {
  .feedback-header {
    .header-content {
      flex-direction: column;
      text-align: center;
      padding: 1.5rem 2rem;
    }

    .header-text {
      text-align: center;
    }
  }

  .form-card {
    padding: 1.5rem;
  }
}
</style>