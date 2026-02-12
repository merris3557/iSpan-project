<script setup>
import { ref } from 'vue';
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
        <h2 class="text-gdg h4 mb-3">
            {{ role === 'shop' ? '門店預約管理' : '我的訂位紀錄' }}
        </h2>
        <table class="table table-hover gdg-table">
            <thead>
                <tr>
                    <th style="width: 100px;">訂位編號</th>
                    <th v-if="role === 'user'" style="width: 150px;">餐廳</th>
                    <th>姓名</th>
                    <th>電話</th>
                    <th style="width: 160px;">日期</th>
                    <th style="width: 130px;">時間</th>
                    <th style="width: 80px;">人數</th>
                    <th style="width: 180px;">操作</th>
                </tr>
            </thead>
            <tbody>
                <tr v-for="item in bookings" :key="item.id">
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
                            <div class="d-flex gap-2">
                                <BaseButton color="gdg" size="sm" @click="startEdit(item)">修改</BaseButton>
                                <BaseButton color="danger" size="sm" @click="$emit('delete', item)">
                                    刪除
                                </BaseButton>
                            </div>
                        </td>
                    </template>
                </tr>
            </tbody>
        </table>
    </div>
</template>

<style scoped>
.gdg-table {
    table-layout: fixed;
    /* 強制固定列寬，防止內容撐開 */
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
</style>