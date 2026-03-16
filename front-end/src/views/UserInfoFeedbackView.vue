<template>
  <div class="user-info-content">
    <h4 class="mb-3 border-bottom pb-2">我的回饋</h4>
    
    <div class="mb-4">
      <div v-if="loading" class="text-center py-5">
        <div class="spinner-border text-gdg" role="status">
          <span class="visually-hidden">載入中...</span>
        </div>
      </div>
      <div v-else-if="feedbackList.length === 0" class="text-center py-5 text-muted">
        目前無回饋紀錄
      </div>
      <div v-else class="card shadow-sm border-0">
        <div class="card-header bg-white border-bottom-0 pt-4 pb-2">
          <h5 class="mb-0 text-gdg fw-bold"><i class="bi bi-chat-text-fill me-2"></i>回饋列表</h5>
        </div>
        <div class="card-body p-0">
          <div class="table-responsive">
            <table class="table table-hover align-middle mb-0">
              <thead class="table-light">
                <tr>
                  <th>日期</th>
                  <th>類別</th>
                  <th style="min-width: 150px;">回饋內容</th>
                  <th style="min-width: 150px;">客服回覆</th>
                  <th class="text-center">操作</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="(item, index) in feedbackList" :key="index">
                  <td class="text-nowrap">{{ formatDate(item.createdAt) }}</td>
                  <td class="text-nowrap">{{ item.typeName }}</td>
                  <td>{{ truncateText(item.contents, 20) }}</td>
                  <td>
                    <span v-if="item.reply">{{ truncateText(item.reply, 20) }}</span>
                    <span v-else class="text-muted fst-italic">尚未回覆</span>
                  </td>
                  <td class="text-center">
                    <button class="btn btn-sm btn-outline-gdg text-nowrap" @click="showDetail(item)">
                      詳細 <i class="bi bi-file-text"></i>
                    </button>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </div>

    <!-- Modal -->
    <div class="modal fade" id="feedbackDetailModal" tabindex="-1" aria-labelledby="feedbackDetailModalLabel" aria-hidden="true" ref="modalRef">
      <div class="modal-dialog modal-dialog-centered modal-dialog-scrollable">
        <div class="modal-content border-0 shadow">
          <div class="modal-header bg-gdg text-white">
            <h5 class="modal-title" id="feedbackDetailModalLabel"><i class="bi bi-info-circle me-2"></i>回饋詳細內容</h5>
            <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal" aria-label="Close"></button>
          </div>
          <div class="modal-body p-4" v-if="selectedFeedback">
            <div class="mb-3 d-flex align-items-center justify-content-between">
              <div>
                 <span class="badge bg-secondary me-2">{{ selectedFeedback.typeName }}</span>
              </div>
              <small class="text-muted">{{ formatDate(selectedFeedback.createdAt) }}</small>
            </div>
            
            <h6 class="fw-bold text-dark mb-2 mt-4 border-bottom pb-1">您的回饋</h6>
            <div class="p-3 bg-light rounded text-break" style="white-space: pre-wrap;">{{ selectedFeedback.contents }}</div>
            
            <template v-if="selectedFeedback.reply">
              <h6 class="fw-bold text-gdg mb-2 mt-4 border-bottom pb-1">客服回覆</h6>
              <div class="p-3 bg-gdg bg-opacity-10 rounded text-break" style="white-space: pre-wrap;">{{ selectedFeedback.reply }}</div>
              <div class="text-end mt-2"><small class="text-muted">回覆時間：{{ formatDate(selectedFeedback.repliedAt) }}</small></div>
            </template>
            <template v-else>
               <div class="text-center text-muted mt-4 p-4 bg-light rounded">
                   <i class="bi bi-hourglass-split fs-4 mb-2 d-block"></i>
                   饗島客服團隊處理中，謝謝您的耐心等待。
               </div>
            </template>
          </div>
          <div class="modal-footer border-top-0">
            <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">關閉</button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue';
import { getUserInfoList } from '@/api/feedback';
import Modal from 'bootstrap/js/dist/modal';

const feedbackList = ref([]);
const loading = ref(true);
const modalRef = ref(null);
const detailModal = ref(null);
const selectedFeedback = ref(null);

const fetchFeedbackList = async () => {
    loading.value = true;
    try {
        // config.js interceptor already returns response.data, so the result is the array directly
        const data = await getUserInfoList();
        feedbackList.value = Array.isArray(data) ? data : [];
    } catch (error) {
        console.error('Error fetching feedback list:', error);
    } finally {
        loading.value = false;
    }
};

const getOrInitModal = () => {
    if (!detailModal.value && modalRef.value) {
        detailModal.value = new Modal(modalRef.value);
    }
    return detailModal.value;
};

onMounted(async () => {
    fetchFeedbackList();
    // Wait for DOM to render before initialising Bootstrap Modal
    await nextTick();
    if (modalRef.value) {
        detailModal.value = new Modal(modalRef.value);
    }
});

const truncateText = (text, maxLength) => {
    if (!text) return '';
    if (text.length <= maxLength) return text;
    return text.substring(0, maxLength) + '...';
};

const formatDate = (dateString) => {
    if (!dateString) return '';
    const date = new Date(dateString);
    if (isNaN(date.getTime())) return dateString;
    return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`;
};

const getStatusBadgeClass = (status) => {
    if (!status) return 'bg-secondary';
    
    // Custom logic based on typical statuses
    if (status.includes('已回覆') || status.includes('完成') || status.includes('DONE')) {
        return 'bg-success';
    } else if (status.includes('處理中') || status.includes('PROGRESS')) {
        return 'bg-warning text-dark';
    } else if (status.includes('未處理') || status.includes('待處理') || status.includes('PENDING')) {
        return 'bg-danger';
    }
    return 'bg-secondary';
};

const showDetail = (item) => {
    selectedFeedback.value = item;
    const modal = getOrInitModal();
    if (modal) {
        modal.show();
    }
};
</script>

<style scoped>
.text-gdg {
  color: #9f9572 !important;
}

.bg-gdg {
  background-color: #9f9572 !important;
}

.btn-outline-gdg {
  color: #9f9572;
  border-color: #9f9572;
}

.btn-outline-gdg:hover {
  color: #fff;
  background-color: #9f9572;
  border-color: #9f9572;
}

.badge {
  font-weight: 500;
}

.table th {
  font-weight: 600;
  color: #495057;
}

.modal-header .btn-close-white {
  filter: invert(1) grayscale(100%) brightness(200%);
}
</style>
