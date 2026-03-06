<script setup>
import { ref, computed, onMounted } from 'vue'
import Swal from 'sweetalert2'
import axios from 'axios'

const orders = ref([])
const filterStatus = ref('全部')
const sortOrder = ref('desc') // 新增排序
const editingOrder = ref(null)
const selectedOrder = ref(null)

const filteredOrders = computed(() => {
    let result = filterStatus.value === '全部' 
        ? orders.value 
        : orders.value.filter(o => o.status === filterStatus.value)
    
    // 排序
    return [...result].sort((a, b) => {
        if (sortOrder.value === 'desc') return b.id - a.id
        return a.id - b.id
    })
})

// 付款方式顯示
const formatPayMethod = (payMethod) => {
    if (!payMethod) return '尚未付款'
    if (payMethod === 'cod') return '貨到付款'
    if (payMethod.startsWith('線上付款-')) return payMethod
    return payMethod
}

// 付款時間格式化
const formatDate = (dateStr) => {
    if (!dateStr) return '--'
    return new Date(dateStr).toLocaleString('zh-TW')
}

onMounted(async () => {
    await fetchOrders()
})

const fetchOrders = async () => {
    try {
        const res = await axios.get('http://localhost:8080/api/orders/all')
        orders.value = res.data
    } catch (err) {
        console.error('無法取得訂單資料', err)
    }
}

const startEdit = (order) => {
    editingOrder.value = JSON.parse(JSON.stringify(order))
}

const saveEdit = async () => {
    try {
        await axios.put(`http://localhost:8080/api/orders/${editingOrder.value.id}`, editingOrder.value)
        await fetchOrders()
        editingOrder.value = null
        Swal.fire('更新成功', '訂單資訊已更新', 'success')
    } catch (error) {
        Swal.fire('錯誤', '更新失敗', 'error')
    }
}

const deleteOrder = (id) => {
    Swal.fire({
        title: '確定刪除？',
        icon: 'warning',
        showCancelButton: true
    }).then(async res => {
        if (res.isConfirmed) {
            await axios.delete(`http://localhost:8080/api/orders/${id}`)
            await fetchOrders()
        }
    })
}

const viewDetail = (order) => {
    selectedOrder.value = order
}


</script>

<template>
    
    <div class="order-management">
        <div class="header-row">
            <h2>訂單管理系統</h2>
            <div class="filter-box">
                <label>訂單狀態篩選：</label>
                <select v-model="filterStatus" class="select-filter">
                    <option>全部</option>
                    <option>待付款</option>
                    <option>待出貨</option>
                    <option>處理中</option>
                    <option>已完成</option>
                </select>
                <label style="margin-left:15px">排序：</label>
                <select v-model="sortOrder" class="select-filter">
                    <option value="desc">最新一筆</option>
                    <option value="asc">最舊一筆</option>
                </select>
            </div>
        </div>

        <table class="order-table">
            <thead>
                <tr>
                    <th>訂單編號</th>
                    <th>客戶姓名</th>
                    <th>總金額</th>
                    <th>付款方式</th>
                    <th>狀態</th>
                    <th>訂單日期</th>
                    <th>操作</th>
                </tr>
            </thead>
            <tbody>
                <tr v-for="order in filteredOrders" :key="order.id">
                    <td>{{ order.id }}</td>
                    <td>{{ order.receiverName }}</td>
                    <td>NT$ {{ order.totalPrice }}</td>
                    <td>{{ formatPayMethod(order.payMethod) }}</td>
                    <td>
                        <span :class="['status-badge', order.status]">{{ order.status }}</span>
                    </td>
                    <td>{{ formatDate(order.createdAt) }}</td>
                    <td>
                        <button @click="viewDetail(order)" class="btn-view">明細</button>
                        <button @click="startEdit(order)" class="btn-edit">編輯</button>
                        <button @click="deleteOrder(order.id)" class="btn-del">刪除</button>
                    </td>
                </tr>
            </tbody>
        </table>

        <!-- 訂單明細視窗 -->
        <div v-if="selectedOrder" class="edit-modal-overlay">
            <div class="edit-modal" style="max-height: 80vh; overflow-y: auto;">
                <h3>訂單明細：#{{ selectedOrder.id }}</h3>
                <div class="detail-info">
                    <p><strong>收件人：</strong>{{ selectedOrder.receiverName }}</p>
                    <p><strong>電話：</strong>{{ selectedOrder.receiverPhone }}</p>
                    <p><strong>地址：</strong>{{ selectedOrder.receiverAddress }}</p>
                    <p><strong>付款方式：</strong>{{ formatPayMethod(selectedOrder.payMethod) }}</p>
                    <p><strong>付款時間：</strong>{{ formatDate(selectedOrder.paymentDate) }}</p>
                    <p><strong>狀態：</strong>{{ selectedOrder.status }}</p>
                    <p><strong>總金額：</strong>NT$ {{ selectedOrder.totalPrice }}</p>
                    <p><strong>建立時間：</strong>{{ formatDate(selectedOrder.createdAt) }}</p>
                </div>
                <hr>
                <h4>商品明細</h4>
                <table class="order-table">
                    <thead>
                        <tr>
                            <th>商品名稱</th>
                            <th>數量</th>
                            <th>單價</th>
                            <th>小計</th>
                        </tr>
                    </thead>
                    <tbody>
                        <tr v-for="detail in selectedOrder.orderDetails" :key="detail.id">
                            <td>{{ detail.productNameSnapshot }}</td>
                            <td>{{ detail.orderQuantity }}</td>
                            <td>NT$ {{ detail.priceSnapshot }}</td>
                            <td>NT$ {{ detail.subtotal }}</td>
                        </tr>
                    </tbody>
                </table>
                <div class="modal-btns">
                    <button @click="selectedOrder = null" class="btn-cancel">關閉</button>
                </div>
            </div>
        </div>

        <!-- 編輯視窗 -->
        <div v-if="editingOrder" class="edit-modal-overlay">
            <div class="edit-modal">
                <h3>編輯訂單：#{{ editingOrder.id }}</h3>
                <div class="form-grid">
                    <div class="form-item">
                        <label>姓名</label>
                        <input v-model="editingOrder.receiverName" type="text">
                    </div>
                    <div class="form-item">
                        <label>電話</label>
                        <input v-model="editingOrder.receiverPhone" type="text">
                    </div>
                    <div class="form-item full-width">
                        <label>地址</label>
                        <input v-model="editingOrder.receiverAddress" type="text">
                    </div>
                    <div class="form-item">
                        <label>付款方式</label>
                        <select v-model="editingOrder.payMethod">
                            <option value="線上付款">線上付款</option>
                            <option value="cod">貨到付款</option>
                        </select>
                    </div>
                    <div class="form-item">
                        <label>訂單狀態</label>
                        <select v-model="editingOrder.status">
                            <option>待付款</option>
                            <option>待出貨</option>
                            <option>處理中</option>
                            <option>已完成</option>
                        </select>
                    </div>
                </div>
                <div class="modal-info">
                    <p>建立時間：{{ formatDate(editingOrder.createdAt) }}</p>
                </div>
                <div class="modal-btns">
                    <button @click="saveEdit" class="btn-save">儲存變更</button>
                    <button @click="editingOrder = null" class="btn-cancel">取消</button>
                </div>
            </div>
        </div>
    </div>
</template>

<style scoped>
.order-management { 
    padding: 20px; 
}

.header-row { 
    display: flex; 
    justify-content: space-between; 
    align-items: center; 
    margin-bottom: 20px; 
}

.order-table { 
    width: 100%; 
    border-collapse: collapse; 
    background: white; 
    border-radius: 8px; 
    overflow: hidden; 
}

.order-table th, .order-table td { 
    padding: 15px; 
    border-bottom: 1px solid #eee; 
    text-align: left; 
}

.order-table th { 
    background: #f8f9fa; 
}

.status-badge { 
    padding: 4px 10px; 
    border-radius: 20px; 
    font-size: 0.8rem; 
}
.status-badge.待付款 { 
    background: #ffeaf8; 
    color: #f13434; 
}

.status-badge.待出貨 { 
    background: #fff3cd; 
    color: #856404; 
}

.status-badge.處理中 { 
    background: #cfe2ff; 
    color: #084298; 
}

.status-badge.已完成 { 
    background: #d1e7dd; 
    color: #0f5132; 
}

.btn-edit { 
    background: #198754; 
    color: white; 
    border: none; 
    padding: 5px 12px; 
    border-radius: 4px; 
    cursor: pointer; 
    margin-right: 5px; 
}

.btn-del { 
    background: #dc3545; 
    color: white; 
    border: none; 
    padding: 5px 12px; 
    border-radius: 4px; 
    cursor: pointer; 
}


/* 編輯視窗樣式 */
.edit-modal-overlay { 
    position: fixed; 
    top: 0; 
    left: 0; 
    width: 100%; 
    height: 100%; 
    background: rgba(0,0,0,0.5); 
    display: flex; 
    justify-content: center; 
    align-items: center; 
    z-index: 1000; 
}

.edit-modal { 
    background: white; 
    padding: 30px; 
    border-radius: 12px; 
    width: 600px; 
    box-shadow: 0 10px 25px rgba(0,0,0,0.2); 
}

.form-grid { 
    display: grid; 
    grid-template-columns: 1fr 1fr; 
    gap: 15px; 
}


.full-width { 
    grid-column: span 2; 
}

.form-item label { 
    display: block; 
    font-size: 0.85rem; 
    color: #666; 
    margin-bottom: 5px; 
}

.form-item input, .form-item select { 
    width: 100%; 
    padding: 8px; 
    border: 1px solid #ccc; 
    border-radius: 4px; 
}

.modal-info { 
    margin-top: 20px; 
    padding-top: 15px; 
    border-top: 1px solid #eee; 
    font-size: 0.85rem; 
}

.highlight { 
    color: #1e3c72; 
    font-weight: bold; 
}

.modal-btns { 
    margin-top: 20px; 
    display: flex; 
    gap: 10px; 
}

.btn-save { 
    flex: 2; 
    background: #198754; 
    color: white; 
    border: none; 
    padding: 10px; 
    border-radius: 6px; 
    cursor: pointer; 
}

.btn-cancel { 
    flex: 1; 
    background: #eee; 
    border: none; 
    padding: 10px; 
    border-radius: 6px; 
    cursor: pointer; 
}

.btn-view {
    background: #0d6efd;
    color: white;
    border: none;
    padding: 5px 12px;
    border-radius: 4px;
    cursor: pointer;
    margin-right: 5px;
}

.detail-info p {
    margin: 8px 0;
    font-size: 0.95rem;
}

</style>