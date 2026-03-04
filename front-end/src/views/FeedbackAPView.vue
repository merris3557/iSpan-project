<script setup>
import { ref, computed } from 'vue';
import ReplyForm from '@/components/ReplyForm.vue';

// Mock Data
const feedbackList = ref([
    {
        id: 1,
        category: 'complaint',
        date: '2023-10-27 10:30',
        name: '王小明',
        email: 'wang@example.com',
        phone: '0912-345-678',
        content: '餐點送達時已經冷掉了，希望能改進外送保溫。',
        status: 'pending',
        reply: ''
    },
    {
        id: 2,
        category: 'suggestion',
        date: '2023-10-28 14:15',
        name: '陳美麗',
        email: 'chen@example.com',
        phone: '0988-123-456',
        content: '建議增加更多素食選項，現在的選擇有點少。',
        status: 'processed',
        reply: '感謝您的建議，我們正在研發新的素食菜單，敬請期待！'
    },
    {
        id: 3,
        category: 'other',
        date: '2023-10-29 09:00',
        name: '林志豪',
        email: 'lin@example.com',
        phone: '0911-223-344',
        content: '請問有提供包場服務嗎？大約 20 人。',
        status: 'pending',
        reply: ''
    }
]);

const filterCategory = ref('all');
const showReplyModal = ref(false);
const currentFeedback = ref(null);

const categories = {
    all: '全部',
    complaint: '客訴',
    suggestion: '建議',
    other: '其他'
};

const statusMap = {
    pending: { label: '未處理', class: 'bg-warning text-dark' },
    processed: { label: '已處理', class: 'bg-success text-white' }
};

const filteredList = computed(() => {
    if (filterCategory.value === 'all') {
        return feedbackList.value;
    }
    return feedbackList.value.filter(item => item.category === filterCategory.value);
});

const openReplyModal = (item) => {
    currentFeedback.value = item;
    showReplyModal.value = true;
};

const closeReplyModal = () => {
    showReplyModal.value = false;
    currentFeedback.value = null;
};

const handleReplySubmit = ({ id, content }) => {
    console.log(`Replying to ID: ${id}, Content: ${content}`);
    
    const index = feedbackList.value.findIndex(item => item.id === id);
    if (index !== -1) {
        feedbackList.value[index].status = 'processed';
        feedbackList.value[index].reply = content;
    }
    
    closeReplyModal();
    alert('回覆已送出！');
};

const getCategoryLabel = (key) => categories[key] || key;

</script>

<template>
    <div class="page-container-gdg p-4">
        <h2 class="page-title-gdg mb-2">客服後台管理</h2>
        <p class="page-description-gdg">管理與回覆客戶意見</p>

        <!-- Filter & Toolbar -->
        <div class="admin-card p-3 mb-4 d-flex justify-content-between align-items-center">
            <div class="d-flex align-items-center gap-3">
                <label for="categoryFilter" class="fw-bold text-admin-primary">分類篩選：</label>
                <select id="categoryFilter" v-model="filterCategory" class="admin-form-control" style="width: 200px;">
                    <option v-for="(label, key) in categories" :key="key" :value="key">
                        {{ label }}
                    </option>
                </select>
            </div>
            <div>
                <!-- Future features: Export button, Search, etc. -->
            </div>
        </div>

        <!-- Data Table -->
        <div class="admin-card overflow-hidden">
            <div class="table-responsive">
                <table class="admin-table table table-hover mb-0 align-middle">
                    <thead>
                        <tr>
                            <th scope="col" width="5%">#</th>
                            <th scope="col" width="8%">分類</th>
                            <th scope="col" width="12%">日期時間</th>
                            <th scope="col" width="10%">姓名</th>
                            <th scope="col" width="15%">聯絡方式</th>
                            <th scope="col" width="25%">內容</th>
                            <th scope="col" width="8%">狀態</th>
                            <th scope="col" width="8%">操作</th>
                        </tr>
                    </thead>
                    <tbody>
                        <tr v-for="item in filteredList" :key="item.id">
                            <td>{{ item.id }}</td>
                            <td>
                                <span class="badge bg-secondary bg-opacity-10 text-secondary border">
                                    {{ getCategoryLabel(item.category) }}
                                </span>
                            </td>
                            <td class="small text-muted">{{ item.date }}</td>
                            <td class="fw-medium">{{ item.name }}</td>
                            <td class="small">
                                <div><i class="bi bi-envelope me-1"></i>{{ item.email }}</div>
                                <div><i class="bi bi-telephone me-1"></i>{{ item.phone }}</div>
                            </td>
                            <td>
                                <div class="text-truncate-3" :title="item.content">
                                    {{ item.content }}
                                </div>
                                <div v-if="item.reply" class="mt-1 small text-success">
                                    <i class="bi bi-arrow-return-right me-1"></i>已回覆：{{ item.reply }}
                                </div>
                            </td>
                            <td>
                                <span class="badge" :class="statusMap[item.status].class">
                                    {{ statusMap[item.status].label }}
                                </span>
                            </td>
                            <td>
                                <button 
                                    class="btn btn-sm btn-admin-primary"
                                    @click="openReplyModal(item)"
                                    :disabled="item.status === 'processed'"
                                >
                                    <i class="bi bi-reply-fill"></i> 回覆
                                </button>
                            </td>
                        </tr>
                        <tr v-if="filteredList.length === 0">
                            <td colspan="8" class="text-center py-5 text-muted">
                                查無資料
                            </td>
                        </tr>
                    </tbody>
                </table>
            </div>
        </div>

        <!-- Reply Modal -->
        <div v-if="showReplyModal" class="modal-backdrop fade show"></div>
        <div v-if="showReplyModal" class="modal fade show d-block" tabindex="-1">
            <div class="modal-dialog modal-lg modal-dialog-centered">
                <div class="modal-content border-0 shadow-lg">
                    <div class="modal-header bg-admin-primary text-white">
                        <h5 class="modal-title">客戶意見回覆</h5>
                        <button type="button" class="btn-close btn-close-white" @click="closeReplyModal"></button>
                    </div>
                    <div class="modal-body bg-light">
                        <ReplyForm 
                            :feedbackData="currentFeedback" 
                            @submit="handleReplySubmit"
                            @cancel="closeReplyModal"
                        />
                    </div>
                </div>
            </div>
        </div>

    </div>
</template>

<style scoped>
.text-truncate-3 {
    display: -webkit-box;
    -webkit-line-clamp: 3;
    line-clamp: 3;
    -webkit-box-orient: vertical;
    overflow: hidden;
    white-space: normal;
}

.modal-backdrop {
    z-index: 1050;
    opacity: 0.5;
    background-color: #000;
}

.modal {
    z-index: 1055;
}
</style>