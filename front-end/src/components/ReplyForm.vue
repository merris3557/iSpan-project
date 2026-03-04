<script setup>
import { ref } from 'vue';

const props = defineProps({
    feedbackData: {
        type: Object,
        default: null
    }
});

const emit = defineEmits(['submit', 'cancel']);
const replyContent = ref('');

const handleSubmit = () => {
    if (!replyContent.value.trim()) {
        alert('請輸入回覆內容');
        return;
    }
    
    emit('submit', {
        id: props.feedbackData?.id,
        content: replyContent.value
    });
    
    replyContent.value = '';
};
</script>

<template>
    <div class="admin-card p-4">
        <h5 class="text-admin-primary mb-4">
            <i class="bi bi-reply-fill me-2"></i>
        </h5>
        
        <div v-if="feedbackData" class="mb-4 p-3 bg-light rounded border">
            <div class="row g-2 text-secondary small">
                <div class="col-md-6">
                    <strong>日期：</strong> {{ feedbackData.date }}
                </div>
                <div class="col-md-6">
                    <strong>姓名：</strong> {{ feedbackData.name }}
                </div>
                <div class="col-12">
                    <strong>內容：</strong> {{ feedbackData.content }}
                </div>
            </div>
        </div>

        <form @submit.prevent="handleSubmit">
            <div class="mb-3">
                <textarea 
                    id="replyContent" 
                    v-model="replyContent" 
                    class="admin-form-control" 
                    rows="5" 
                    placeholder="請輸入欲回覆的內容..."
                    required
                ></textarea>
            </div>
            <div class="d-flex justify-content-end gap-2">
                <button type="button" class="btn btn-admin-outline" @click="$emit('cancel')">
                    取消
                </button>
                <button type="submit" class="btn btn-admin-primary">
                    <i class="bi bi-send-fill me-2"></i>送出回覆
                </button>
            </div>
        </form>
    </div>
</template>

<style scoped>
.admin-card {
    max-width: 800px;
    margin: 0 auto;
}

textarea {
    resize: none;
}
</style>
