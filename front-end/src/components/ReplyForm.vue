<script setup>
import { ref } from 'vue';

const props = defineProps({
    feedbackData: {
        type: Object,
        default: null
    },
    statusOptions: {
        type: Array,
        default: () => []
    },
    isSubmitting: {
        type: Boolean,
        default: false
    }
});

const emit = defineEmits(['submit', 'cancel']);

const replyContent  = ref('');
const selectedStatusId = ref(null);

const handleSubmit = () => {
    if (!replyContent.value.trim()) {
        alert('請輸入回覆內容');
        return;
    }
    if (!selectedStatusId.value) {
        alert('請選擇處理狀態');
        return;
    }

    emit('submit', {
        feedbackId: props.feedbackData?.id,
        reply:      replyContent.value,
        statusId:   selectedStatusId.value
    });

    replyContent.value     = '';
    selectedStatusId.value = null;
};
</script>

<template>
    <div class="reply-form-wrap p-4">
        <!-- 標題 -->
        <h5 class="text-admin-primary mb-4">
            <i class="bi bi-reply-fill me-2"></i>回覆意見
        </h5>

        <!-- 快取資訊：姓名 + 完整內容 -->
        <div v-if="feedbackData" class="mb-4 p-3 bg-white rounded border">
            <div class="row g-2 text-secondary small">
                <div class="col-md-6">
                    <strong>使用者姓名：</strong>{{ feedbackData.name }}
                </div>
                <div class="col-md-6">
                    <strong>送出時間：</strong>{{ feedbackData.createdAt ? feedbackData.createdAt.replace('T', ' ').slice(0, 16) : '—' }}
                </div>
                <div class="col-12">
                    <strong>意見全文：</strong>
                    <p class="mt-1 mb-0 text-dark" style="white-space: pre-wrap;">{{ feedbackData.contents }}</p>
                </div>
            </div>
        </div>

        <form @submit.prevent="handleSubmit">
            <!-- 處理狀態下拉選單 -->
            <div class="mb-3">
                <label for="statusSelect" class="form-label fw-bold text-admin-primary">
                    處理狀態 <span class="text-danger">*</span>
                </label>
                <select
                    id="statusSelect"
                    v-model="selectedStatusId"
                    class="admin-form-control"
                    required
                >
                    <option :value="null" disabled>請選擇狀態</option>
                    <option
                        v-for="s in statusOptions"
                        :key="s.statusId"
                        :value="s.statusId"
                    >
                        {{ s.statusName }}
                    </option>
                </select>
            </div>

            <!-- 回覆內容 -->
            <div class="mb-3">
                <label for="replyContent" class="form-label fw-bold text-admin-primary">
                    回覆內容 <span class="text-danger">*</span>
                </label>
                <textarea
                    id="replyContent"
                    v-model="replyContent"
                    class="admin-form-control"
                    rows="5"
                    placeholder="請輸入欲回覆的內容..."
                    required
                ></textarea>
            </div>

            <!-- 按鈕 -->
            <div class="d-flex justify-content-end gap-2">
                <button type="button" class="btn btn-admin-outline" @click="$emit('cancel')" :disabled="isSubmitting">
                    取消
                </button>
                <button type="submit" class="btn btn-admin-primary" :disabled="isSubmitting">
                    <span v-if="isSubmitting" class="spinner-border spinner-border-sm me-1"></span>
                    <i v-else class="bi bi-send-fill me-1"></i>
                    {{ isSubmitting ? '送出中...' : '送出回覆' }}
                </button>
            </div>
        </form>
    </div>
</template>

<style scoped>
.reply-form-wrap {
    max-width: 800px;
    margin: 0 auto;
}

textarea {
    resize: none;
}
</style>
