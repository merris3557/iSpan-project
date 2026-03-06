<script setup>
import { ref, computed, onMounted } from 'vue';
import ReplyForm from '@/components/ReplyForm.vue';
import { getFeedbackList, getStatusList, replyFeedback } from '@/api/feedbackAP';
import { useAdminAuthStore } from '@/stores/adminAuth';
import Swal from 'sweetalert2';

const adminAuthStore = useAdminAuthStore();

// ─── 狀態 ────────────────────────────────────────────
const feedbackList    = ref([]);
const totalPages      = ref(0);
const currentPage     = ref(0);
const filterStatus    = ref('');       // '' = 全部，否則為 statusName
const statusOptions   = ref([]);       // 從 API 取得的 feedback_status 清單

const showReplyModal  = ref(false);
const currentFeedback = ref(null);
const isLoading       = ref(false);
const isSubmitting    = ref(false);

// ─── 初始化 ──────────────────────────────────────────
onMounted(async () => {
    await loadStatuses();
    await loadFeedbacks();
});

// 取得 feedback_status 選項清單
const loadStatuses = async () => {
    try {
        const data = await getStatusList();
        statusOptions.value = data;
    } catch (err) {
        console.error('Failed to load status list:', err);
    }
};

// 取得 feedback 清單（帶 status 篩選與分頁）
const loadFeedbacks = async (page = 0) => {
    isLoading.value = true;
    try {
        const data = await getFeedbackList(filterStatus.value || null, page);

        // --- 這裡加入 Log 檢查 ---
        console.log("API 回傳原始資料:", data);
        console.log("Content 陣列長度:", data.content ? data.content.length : '無內容');

        feedbackList.value = data.content;
        totalPages.value   = data.totalPages;
        currentPage.value  = data.number;
    } catch (err) {
        console.error('Failed to load feedbacks:', err);
        Swal.fire({ icon: 'error', title: '載入失敗', text: '無法取得 feedback 清單，請稍後再試。' });
    } finally {
        isLoading.value = false;
    }
};

// ─── 篩選 ────────────────────────────────────────────
const onFilterChange = () => {
    loadFeedbacks(0);
};

// ─── 分頁 ────────────────────────────────────────────
const goToPage = (page) => {
    if (page >= 0 && page < totalPages.value) {
        loadFeedbacks(page);
    }
};

// 計算要顯示的頁碼陣列（最多顯示 7 個，超過用省略號）
const pageNumbers = computed(() => {
    const total = totalPages.value;
    const current = currentPage.value;
    if (total <= 7) {
        return Array.from({ length: total }, (_, i) => i);
    }
    const pages = [];
    pages.push(0);
    let start = Math.max(1, current - 2);
    let end   = Math.min(total - 2, current + 2);
    if (start > 1)  pages.push('...');
    for (let i = start; i <= end; i++) pages.push(i);
    if (end < total - 2) pages.push('...');
    pages.push(total - 1);
    return pages;
});

// ─── 回覆 Modal ──────────────────────────────────────
const openReplyModal = (item) => {
    currentFeedback.value = item;   // 快取完整 feedback 物件
    showReplyModal.value  = true;
};

const closeReplyModal = () => {
    showReplyModal.value  = false;
    currentFeedback.value = null;
};

// 回覆送出
const handleReplySubmit = async ({ feedbackId, reply, statusId }) => {
    const adminId = adminAuthStore.admin?.id ?? null;
    isSubmitting.value = true;
    try {
        await replyFeedback({ feedbackId, reply, adminId, statusId });
        closeReplyModal();
        await loadFeedbacks(currentPage.value);
        Swal.fire({ icon: 'success', title: '回覆成功', text: '已送出回覆並通知使用者。', timer: 2000, showConfirmButton: false });
    } catch (err) {
        console.error('Reply failed:', err);
        Swal.fire({ icon: 'error', title: '回覆失敗', text: '送出失敗，請稍後再試。' });
    } finally {
        isSubmitting.value = false;
    }
};

// ─── 輔助函式 ────────────────────────────────────────
const truncate = (str, len = 20) => {
    if (!str) return '';
    return str.length > len ? str.slice(0, len) + '...' : str;
};

const formatDate = (dateStr) => {
    if (!dateStr) return '';
    return dateStr.replace('T', ' ').slice(0, 16);
};
</script>

<template>
    <div class="page-container-gdg p-4">
        <h2 class="page-title-gdg mb-2">客服後台管理</h2>
        <p class="page-description-gdg">管理與回覆客戶意見</p>

        <!-- Filter & Toolbar -->
        <div class="admin-card p-3 mb-4 d-flex justify-content-between align-items-center">
            <div class="d-flex align-items-center gap-3">
                <label for="statusFilter" class="fw-bold text-admin-primary">狀態篩選：</label>
                <select
                    id="statusFilter"
                    v-model="filterStatus"
                    @change="onFilterChange"
                    class="admin-form-control"
                    style="width: 200px;"
                >
                    <option value="">全部</option>
                    <option
                        v-for="s in statusOptions"
                        :key="s.statusId"
                        :value="s.statusName"
                    >
                        {{ s.statusName }}
                    </option>
                </select>
            </div>
        </div>

        <!-- Data Table -->
        <div class="admin-card">
            <div class="table-responsive" style="overflow-x: auto;">
                <table class="admin-table table table-hover mb-0 align-middle" style="min-width: 900px;">
                    <thead>
                        <tr>
                            <th scope="col" style="width:90px">類型</th>
                            <th scope="col" style="width:130px">日期時間</th>
                            <th scope="col" style="width:80px">姓名</th>
                            <th scope="col" style="width:200px">聯絡方式</th>
                            <th scope="col">內容</th>
                            <th scope="col" style="width:80px">狀態</th>
                            <th scope="col" style="width:80px">操作</th>
                        </tr>
                    </thead>
                    <tbody>
                        <!-- Loading row -->
                        <tr v-if="isLoading">
                            <td colspan="8" class="text-center py-5 text-muted">
                                <span class="spinner-border spinner-border-sm me-2"></span>載入中...
                            </td>
                        </tr>

                        <template v-else>
                            <tr v-for="item in feedbackList" :key="item.id">
                                <td>
                                    <span class="badge bg-secondary bg-opacity-10 text-secondary border">
                                        {{ item.typeName || '—' }}
                                    </span>
                                </td>
                                <td class="small text-muted">{{ formatDate(item.createdAt) }}</td>
                                <td class="fw-medium">{{ item.name }}</td>
                                <td class="small" style="word-break: break-all;">
                                    <div><i class="bi bi-envelope me-1"></i>{{ item.email }}</div>
                                    <div v-if="item.phone"><i class="bi bi-telephone me-1"></i>{{ item.phone }}</div>
                                </td>
                                <td>
                                    <div :title="item.contents">
                                        {{ truncate(item.contents, 20) }}
                                    </div>
                                    <div v-if="item.reply" class="mt-1 small text-success">
                                        <i class="bi bi-arrow-return-right me-1"></i>已回覆：{{ truncate(item.reply, 20) }}
                                    </div>
                                </td>
                                <td>
                                    <span
                                        class="badge"
                                        :class="item.statusName === '已處理' ? 'bg-success text-white' : 'bg-warning text-dark'"
                                    >
                                        {{ item.statusName || '未知' }}
                                    </span>
                                </td>
                                <td>
                                    <button
                                        class="btn btn-sm btn-admin-primary"
                                        @click="openReplyModal(item)"
                                    >
                                        <i class="bi bi-reply-fill"></i> 回覆
                                    </button>
                                </td>
                            </tr>

                            <tr v-if="feedbackList.length === 0">
                                <td colspan="8" class="text-center py-5 text-muted">查無資料</td>
                            </tr>
                        </template>
                    </tbody>
                </table>
            </div>
        </div>

        <!-- Pagination -->
        <div v-if="totalPages > 0" class="d-flex justify-content-center align-items-center flex-wrap gap-1 mt-4">
            <!-- 上一頁 -->
            <button
                class="btn btn-sm btn-admin-outline px-3"
                :disabled="currentPage === 0"
                @click="goToPage(currentPage - 1)"
            >
                <i class="bi bi-chevron-left"></i>
            </button>

            <!-- 頁碼 -->
            <template v-for="(p, idx) in pageNumbers" :key="idx">
                <span v-if="p === '...'" class="px-1 text-muted" style="line-height:2;">…</span>
                <button
                    v-else
                    class="btn btn-sm"
                    :class="p === currentPage ? 'btn-admin-primary text-white' : 'btn-admin-outline'"
                    style="min-width: 36px;"
                    @click="goToPage(p)"
                >
                    {{ p + 1 }}
                </button>
            </template>

            <!-- 下一頁 -->
            <button
                class="btn btn-sm btn-admin-outline px-3"
                :disabled="currentPage >= totalPages - 1"
                @click="goToPage(currentPage + 1)"
            >
                <i class="bi bi-chevron-right"></i>
            </button>

            <span class="text-muted small ms-2">共 {{ totalPages }} 頁，每頁 10 筆</span>
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
                            :statusOptions="statusOptions"
                            :isSubmitting="isSubmitting"
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
.modal-backdrop {
    z-index: 1050;
    opacity: 0.5;
    background-color: #000;
}

.modal {
    z-index: 1055;
}
</style>