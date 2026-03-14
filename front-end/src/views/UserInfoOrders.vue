<script setup>
import api from '@/api/config'
import { ref, onMounted } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { shopLog } from '@/utils/shopLogger'


const authStore = useAuthStore()
const orders = ref([])
const loading = ref(false)
const expandedOrderId = ref(null)

const fetchUserOrders = async () => {
    loading.value = true
    try {
        // 等待登入狀態同步
        if (!authStore.isLoggedIn) {
            const wasLoggedIn = localStorage.getItem('isUserLoggedIn') === 'true'
            if (wasLoggedIn) {
                await authStore.syncUserProfile()
            }
        }
        const res = await api.get('/orders/my')
        shopLog('回傳資料：', res)
        orders.value = (Array.isArray(res) ? res : res.data || res).map(order => ({
            ...order,
            orderDetails: order.orderDetails || []
        }))
        
    } catch (err) {
        shopLog('取得訂單失敗', err)
    } finally {
        loading.value = false
    }
}

onMounted(async () => {
    await fetchUserOrders()
})

const toggleDetail = (id) => {
    expandedOrderId.value = expandedOrderId.value === id ? null : id
}

const formatDate = (dateStr) => {
    if (!dateStr) return '尚未付款'
    return new Date(dateStr).toLocaleString('zh-TW')
}

const formatPayMethod = (payMethod) => {
    if (!payMethod) return '尚未付款'
    if (payMethod === 'cod') return '貨到付款'
    if (payMethod === 'ECpay') return '線上付款'
    if (payMethod.startsWith('線上付款-')) return payMethod
    return payMethod
}

const getStatusClass = (status) => {
    const map = {
        '待付款': 'badge bg-danger bg-opacity-10 text-danger',
        '待出貨': 'badge bg-warning bg-opacity-10 text-warning',
        '處理中': 'badge bg-primary bg-opacity-10 text-primary',
        '已完成': 'badge bg-success bg-opacity-10 text-success',
    }
    return map[status] || 'badge bg-secondary'
}


const goToPay = async (orderId) => {
    try {
        const payResponse = await api.get(`/ecpay/pay/${orderId}`)
        const div = document.createElement('div')
        div.innerHTML = payResponse
        const form = div.querySelector('form')
        document.body.appendChild(form)
        form.submit()
    } catch (err) {
        shopLog('前往付款失敗', err)
    }
}

import { useRouter } from 'vue-router'
const router = useRouter()

const goToCustomerService = () =>{
    //點擊進入客服頁面
    router.push('/feedback');
};


</script>

<template>
    <div class="user-bookings-view">
        <h4 class="mb-3 border-bottom pb-2">我的訂單</h4>

        <div v-if="loading" class="text-center py-4">
            <div class="spinner-border" role="status"></div>
        </div>

        <div v-else-if="orders.length === 0" class="text-muted text-center py-4">
            目前沒有任何訂單
        </div>

        <div v-else>
            <div v-for="order in orders" :key="order.id" class="card shadow-sm border-0 mb-3">
                <div class="card-body">
                    <div class="d-flex justify-content-between align-items-center" 
                        style="cursor:pointer"
                        @click="toggleDetail(order.id)">
                        <div>
                            <span class="fw-bold me-3">訂單 #{{ order.merchantTradeNo || order.id }}</span>
                            
                            <span class="text-muted small">{{ formatDate(order.createdAt) }}</span>
                        </div>
                        <div class="d-flex align-items-center gap-3">
                            <span class="fw-bold" style="color:#9f9572">NT$ {{ order.totalPrice }}</span>
                            <span :class="getStatusClass(order.status)">{{ order.status }}</span>
                            <i :class="expandedOrderId === order.id ? 'bi bi-chevron-up' : 'bi bi-chevron-down'"></i>
                        </div>
                    </div>

                    <div class="mt-2 small text-muted">
                        付款方式：{{ formatPayMethod(order.payMethod) }}
                    </div>

                    <div  style="display: flex; justify-content: flex-end;" v-if="order.status === '待付款'" class="mt-2">
                        <button @click.stop="goToPay(order.id)" class="btn-pay">
                            去付款
                        </button>
                    </div>

                    <!-- 展開的訂單明細 -->
                    <div v-if="expandedOrderId === order.id" class="mt-3 pt-3 border-top">
                        <div class="row mb-2">
                            <div class="col-sm-3 text-muted">收件人</div>
                            <div class="col-sm-9">{{ order.receiverName }}</div>
                        </div>
                        <div class="row mb-2">
                            <div class="col-sm-3 text-muted">電話</div>
                            <div class="col-sm-9">{{ order.receiverPhone }}</div>
                        </div>
                        <div class="row mb-2">
                            <div class="col-sm-3 text-muted">地址</div>
                            <div class="col-sm-9">{{ order.receiverAddress }}</div>
                        </div>
                        <div class="row mb-2">
                            <div class="col-sm-3 text-muted">付款時間</div>
                            <div class="col-sm-9">{{ formatDate(order.paymentDate) }}</div>
                        </div>

                        <div class="row mb-2">
                            <div class="col-sm-3 text-muted">備註</div>
                            <div class="col-sm-9">{{ order.note || '無' }}</div>
                        </div>



                        <table class="table table-sm mt-3">
                            <thead>
                                <tr>
                                    <th>商品名稱</th>
                                    <th>數量</th>
                                    <th>單價</th>
                                    <th>小計</th>
                                </tr>
                            </thead>
                            <tbody>
                                <tr v-for="detail in (order.orderDetails || [])" :key="detail.id">
                                    <td>{{ detail.productNameSnapshot }}</td>
                                    <td>{{ detail.orderQuantity }}</td>
                                    <td>NT$ {{ detail.priceSnapshot }}</td>
                                    <td>NT$ {{ detail.subtotal }}</td>
                                </tr>
                            </tbody>
                        </table>

                        <div  style=" justify-content: flex-end;"class="mt-2">
                            <button @click.stop="goToCustomerService" class="btn-CS">
                                聯絡客服
                            </button>
                        </div>
                        
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>



<style scoped>
.btn-pay {
    background-color: #efa73a ;
    color: white;
    border: none;
    padding: 6px 70px;
    border-radius: 20px;
    font-size: 0.85rem;
    cursor: pointer;
    transition: 0.2s;
    
}

.btn-pay:hover {
    background-color: #e8bb79;
}

.btn-CS {
    background-color: #9f9572;
    color: white;
    border: none;
    padding: 6px 70px;
    border-radius: 20px;
    font-size: 0.85rem;
    cursor: pointer;
    transition: 0.2s;
    
}

.btn-pay:hover {
    background-color: #8f8562;
}
</style>