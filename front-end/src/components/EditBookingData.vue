<script setup>
import { ref, computed, watch } from 'vue';
import BaseButton from '@/components/common/BaseButton.vue';

// 接收父層傳來的資料(dataList)
const props = defineProps({
    bookings: { type: Array, default: () => [] },
    role: { type: String, default: 'shop' }
});

// 紀錄哪一行正在編輯
const editingId = ref(null);

const tempEditItem = ref({}); // 用於存放編輯中的暫存資料

// 新增 update 事件
const emit = defineEmits(['delete', 'update']);

const startEdit = (item) => {
    editingId.value = item.id;
    // 製作副本，避免直接修改到 props (這會導致取消編輯時資料回不去)
    tempEditItem.value = { ...item };
};

const saveEdit = () => {
    // 將修改後的副本送給父層處理 (未來對接後端)
    emit('update', tempEditItem.value);
    editingId.value = null;
};

const cancelEdit = () => {
    editingId.value = null;
};

// 分頁邏輯
const currentPage = ref(1);
const itemsPerPage = 10;

const totalPages = computed(() => {
    return Math.max(1, Math.ceil(props.bookings.length / itemsPerPage));
});

const paginatedBookings = computed(() => {
    const start = (currentPage.value - 1) * itemsPerPage;
    return props.bookings.slice(start, start + itemsPerPage);
});

const changePage = (page) => {
    if (page >= 1 && page <= totalPages.value) {
        currentPage.value = page;
    }
};

watch(() => props.bookings.length, (newLen) => {
    if (currentPage.value > totalPages.value) {
        currentPage.value = Math.max(1, Math.ceil(newLen / itemsPerPage) || 1);
    }
});

// 判斷是否為過往紀錄
const isPastBooking = (dateStr, timeStr) => {
    if (!dateStr || !timeStr) return false;
    const now = new Date();
    const bDateTime = new Date(`${dateStr}T${timeStr}:00`);
    return bDateTime < now;
};


// 日期選擇限制
const tomorrow = new Date();
tomorrow.setDate(tomorrow.getDate());
// 使用在地時間格式化 (YYYY-MM-DD)
const year = tomorrow.getFullYear();
const month = String(tomorrow.getMonth() + 1).padStart(2, '0');
const day = String(tomorrow.getDate()).padStart(2, '0');

const minDate = `${year}-${month}-${day}`;

</script>

<template>
    <div class="booking-table-container">
        <div class="table-responsive">
            <table class="table table-hover gdg-table">
            <thead>
                <tr>
                    <th style="min-width: 80px;">訂位編號</th>
                    <th v-if="role === 'user'" style="min-width: 150px;">餐廳</th>
                    <th style="min-width: 100px;">姓名</th>
                    <th style="min-width: 120px;">電話</th>
                    <th style="min-width: 160px;">日期</th>
                    <th style="min-width: 140px;">時間</th>
                    <th style="min-width: 80px;">人數</th>
                    <th style="min-width: 160px;">操作</th>
                </tr>
            </thead>
            <tbody>
                <tr v-for="item in paginatedBookings" :key="item.id" :class="{ 'past-booking': isPastBooking(item.date, item.time) }">
                    <template v-if="editingId === item.id">
                        <td>{{ item.id }}</td>

                        <td v-if="role === 'user'">{{ item.restaurant }}</td>

                        <td>
                            <input v-if="role === 'user'" v-model="tempEditItem.name"
                                class="form-control form-control-sm table-input" />
                            <span v-else>{{ item.name }}</span>
                        </td>

                        <td>
                            <input v-if="role === 'user'" v-model="tempEditItem.phone"
                                class="form-control form-control-sm table-input" />
                            <span v-else>{{ item.phone }}</span>
                        </td>

                        <td>
                            <input v-if="role === 'shop'" type="date" v-model="tempEditItem.date"
                                class="form-control form-control-sm table-input" :min="minDate"
                                @change="resetTime" />
                            <span v-else>{{ item.date }}</span>
                        </td>
                        <td>
                            <input v-if="role === 'shop'" type="time" v-model="tempEditItem.time"
                                class="form-control form-control-sm table-input" />
                            <span v-else>{{ item.time }}</span>
                        </td>

                        <td>{{ item.people }}</td>

                        <td>
                            <div class="d-flex gap-2">
                                <BaseButton color="outline-gdg" size="sm" @click="saveEdit">儲存</BaseButton>
                                <BaseButton color="outline-danger" size="sm" @click="cancelEdit">取消</BaseButton>
                            </div>
                        </td>
                    </template>

                    <template v-else>
                        <td>{{ item.id }}</td>
                        <td v-if="role === 'user'">{{ item.restaurant }}</td>
                        <td>{{ item.name }}</td>
                        <td>{{ item.phone }}</td>
                        <td>{{ item.date }}</td>
                        <td>{{ item.time }}</td>
                        <td>{{ item.people }}</td>
                        <td>
                            <div class="d-flex gap-2" v-if="!isPastBooking(item.date, item.time)">
                                <BaseButton color="gdg" size="sm" @click="startEdit(item)">修改</BaseButton>
                                <BaseButton color="danger" size="sm" @click="$emit('delete', item)">
                                    刪除
                                </BaseButton>
                            </div>
                            <span v-else class="text-muted small">已結束</span>
                        </td>
                    </template>
                </tr>
            </tbody>
            </table>
        </div>

        <!-- 分頁控制 -->
        <nav v-if="totalPages > 1" class="mt-4" aria-label="Page navigation">
            <ul class="pagination justify-content-center mb-0">
                <li class="page-item" :class="{ disabled: currentPage === 1 }">
                    <a class="page-link text-gdg" href="#" @click.prevent="changePage(currentPage - 1)">上一頁</a>
                </li>
                <li class="page-item" v-for="p in totalPages" :key="p" :class="{ active: p === currentPage }">
                    <a class="page-link" :class="p === currentPage ? 'bg-gdg text-white border-gdg' : 'text-gdg'" href="#" @click.prevent="changePage(p)">{{ p }}</a>
                </li>
                <li class="page-item" :class="{ disabled: currentPage === totalPages }">
                    <a class="page-link text-gdg" href="#" @click.prevent="changePage(currentPage + 1)">下一頁</a>
                </li>
            </ul>
        </nav>
    </div>
</template>

<style scoped>
.gdg-table {
    /* 移除 table-layout: fixed 讓表格能根據內容伸縮 */
    border-collapse: collapse;
    width: 100%;
}

.gdg-table th {
    background-color: #f8f7f2;
    color: #9f9572;
    border-bottom: 2px solid #9f9572;
    padding: 12px 8px;
}

.gdg-table td {
    vertical-align: middle;
    padding: 8px;
    border-bottom: 1px solid rgba(160, 150, 115, 0.2);
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
}

/* 過往訂位樣式 */
.past-booking {
    background-color: #fcfcfc;
}
.past-booking td {
    color: #b0b0b0;
}

/* 讓 input 在單元格內不產生額外邊距 */
.table-input {
    border-radius: 0;
    border: 1px solid #9f9572;
    height: 31px;
    /* 配合文字高度避免撐高 tr */
    padding: 2px 5px;
}

.table-input:focus {
    box-shadow: none;
    border-color: #776f54;
}

.bg-gdg {
    background-color: #9f9572 !important;
}

.border-gdg {
    border-color: #9f9572 !important;
}

.text-gdg {
    color: #9f9572 !important;
}

.page-link:focus, .page-link:hover {
    color: #8f8562;
    background-color: #f8f7f2;
}
</style>