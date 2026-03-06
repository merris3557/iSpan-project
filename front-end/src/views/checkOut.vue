<script setup>
import { useCartStore } from '@/stores/cart'
import { useRouter } from 'vue-router'
import { ref, computed, onMounted  } from 'vue'
import Swal from 'sweetalert2'
import { useOrderDepot } from '@/stores/orderDepot.js'
import TwCitySelector from 'tw-city-selector'
import api from '@/api/config'

const cartStore = useCartStore()
const router = useRouter()
const orderDepot = useOrderDepot()



// 收件人資料表單
const orderForm = ref({
    name: '',
    phone: '',
    email: '',
    city: '',      // 縣市
    district: '',  // 區域
    zipcode: '',   // 郵遞區號
    street: '',    // 路段手輸
    deliveryMethod: 'home',
    paymentMethod: 'ECpay', 
    note: ''
})

onMounted(() => {
    new TwCitySelector({
        el: '#twzipcode',
        elCounty: '.county',
        elDistrict: '.district',
        elZipcode: '.zipcode',
        onCountyChange: (val) => { orderForm.value.city = val },
        onDistrictChange: (val) => { orderForm.value.district = val },
        onZipcodeChange: (val) => { orderForm.value.zipcode = val }
    });

    setTimeout(() => {
        const countyEl = document.querySelector('#twzipcode .county')
        const districtEl = document.querySelector('#twzipcode .district')
        const zipcodeEl = document.querySelector('#twzipcode .zipcode')
        
        if (countyEl?.value) orderForm.value.city = countyEl.value
        if (districtEl?.value) orderForm.value.district = districtEl.value
        if (zipcodeEl?.value) orderForm.value.zipcode = zipcodeEl.value
    }, 500)
})



const shippingFee = computed(() => {
    if (!cartStore.items || cartStore.items.length === 0) return 0;
    if (cartStore.totalPrice >= 2000) return 0;
    
    return orderForm.value.deliveryMethod === 'home' ? 100 : 60;
});

const finalTotalPrice = computed(() => cartStore.totalPrice + shippingFee.value);

const isValidEmail = (email) => {
    return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email);
};

const handleCheckout = async () => {
    console.log('表單內容：', orderForm.value) 
    // 簡單表單驗證
    

    if (!orderForm.value.name || !orderForm.value.phone ||  !orderForm.value.street) {
        Swal.fire('錯誤', '請填寫完整的收件人資訊', 'error')
        return
    }

    

    if (orderForm.value.email && !isValidEmail(orderForm.value.email)) {
        Swal.fire('錯誤', 'Email 格式有誤', 'error');
        return;
    }

    const orderNumber = 'ORD' + Date.now();



    if (cartStore.items.length === 0) {
        Swal.fire('錯誤', '購物車是空的', 'error')
        return
    }

    //建立訂單狀態為待付款、已付款、出貨中、已完成
    const getOrderStatus = () =>{
        if(orderForm.value.paymentMethod === 'ECpay') {
            return '待付款'
        } else if (orderForm.value.paymentMethod === 'cod') {
            return '待出貨'
        } else {
            return '待付款' // 預設
        }
    } 

    const currentStatus= getOrderStatus();


    // 這裡模擬送出訂單
    const result = await Swal.fire({
        title: '確認送出訂單？',
        text: `總金額為 NT$ ${cartStore.totalPrice + shippingFee.value}`,
        icon: 'question',
        showCancelButton: true,
        confirmButtonText: '確定下單',
        cancelButtonText: '再檢查一下'
    })
    
    
    if (result.isConfirmed) {
        try {
            const response = await api.post('/orders/checkout', {
                name: orderForm.value.name,
                phone: orderForm.value.phone,
                city: orderForm.value.city,
                district: orderForm.value.district,
                street: orderForm.value.street,
                deliveryMethod: orderForm.value.deliveryMethod,
                paymentMethod: orderForm.value.paymentMethod,
                note: orderForm.value.note,
                shippingFee: shippingFee.value
            })

            orderDepot.addOrder({
                customer: {...orderForm.value},
                totalPrice: cartStore.totalPrice,
                status: currentStatus,
                items: cartStore.items.map(item => ({
                    id: item.id,
                    name: item.name,
                    price: item.price,
                    quantity: item.quantity
                })),
            })

            console.log('目前的訂單總數：', orderDepot.orders.length)

            await cartStore.fetchCart()

            // 判斷付款方式
            if (orderForm.value.paymentMethod === 'ECpay') {
                // 信用卡 → 詢問是否前往綠界付款
                const payResult = await Swal.fire({
                    icon: 'success',
                    title: '訂單建立成功！',
                    html: `狀態：${currentStatus}<br>訂單編號：${orderNumber}`,
                    showCancelButton: true,
                    confirmButtonText: '前往付款',
                    cancelButtonText: '稍後再付'
                })
                
                if (payResult.isConfirmed) {
                    // 從後端拿到參數
                    const payResponse = await api.get(`/ecpay/pay/${response.orderId}`)
                    
                    // 動態建立 form 並 submit
                    const div = document.createElement('div')
                    div.innerHTML = payResponse
                    const form = div.querySelector('form')
                    document.body.appendChild(form)
                    form.submit()
                }
                // } else {
                    
                //     router.push('/shopStore')
                // }

            } else {
                // 貨到付款 → 直接顯示成功
                await Swal.fire({
                    icon: 'success',
                    title: '成功',
                    text: `訂單已建立！狀態為：${currentStatus}`,
                    footer: `<p style="font-weight: bold; font-size: 16px; color: 198754;">訂單編號為: ${orderNumber}</p>`
                })
                router.push('/shopStore')
            }

        } catch (error) {
            Swal.fire('錯誤', error.response?.data?.error || '結帳失敗', 'error')
        }
    }
console.log('city:', orderForm.value.city, 'district:', orderForm.value.district)

}
</script>

<template>
    <div class="checkout-container">
        <h2 class="section-title">結帳確認</h2>

        <div class="checkout-layout">
            <div class="form-section">
                <div class="card">
                    <div class="card-header">收件人資訊</div>
                    <div class="card-body">
                        <div class="form-group">
                            <label>姓名 *</label>
                            <input v-model="orderForm.name" type="text" class="form-control"  >
                            <span style="color: #cdbabab4;">請輸入收件人姓名</span>
                        </div>
                        <br>
                        <div class="form-group">
                            <label>手機號碼 *</label>
                            <input v-model="orderForm.phone" type="tel" class="form-control" >
                            <span style="color: #cdbabab4;">ex:0912345678</span>

                        </div>
                        <br>
                        <div class="form-group mt-3">
                            <label>電子信箱</label>
                            <input v-model="orderForm.email" type="email" class="form-control" >
                            <span style="color: #cdbabab4;">example@mail.com</span>
                            <small v-if="orderForm.email && !isValidEmail(orderForm.email)" class="text-danger">格式有誤</small>
                        </div>
                        <br>
                        <div class="form-group mt-3">
                            <label>收件地址 *</label>
                            <div id="twzipcode" class="address-select-group">
                                <select class="county form-control"></select>
                                <br>
                                <select class="district form-control"></select>
                                <br>
                                <input class="zipcode form-control" readonly placeholder="郵遞區號">
                            </div>
                            <br>
                            <input v-model="orderForm.street" type="text" class="form-control mt-2" placeholder="路段、門牌號碼">
                        </div>
                        <br>
                        <div class="form-group">
                            <label>備註</label>
                            <textarea v-model="orderForm.note" class="form-control" rows="2"></textarea>
                        </div>
                        </div>
                        </div>
                        
                        <div class="mb-3">
                            <label class="d-block mb-2">配送方式</label>
                            <div class="payment-options">
                                <label class="payment-radio">
                                    <input type="radio" v-model="orderForm.deliveryMethod" value="home">
                                    <span class="radio-label" >宅配 <span style="color: gray;">(金額未滿2000元運費 NT$ 100)</span></span>
                                </label>
                                <label class="payment-radio">
                                    <input type="radio" v-model="orderForm.deliveryMethod" value="store">
                                    <span class="radio-label" >超商取貨 <span style="color: gray;">(金額未滿2000元運費 NT$ 60)</span></span>
                                </label>
                            </div>
                        </div>
                    
                

                <div class="card mt-4">
                    <div class="card-header">付款方式</div>
                    <div class="card-body">
                        <div class="payment-options">
                            <label class="payment-radio">
                                <input type="radio" v-model="orderForm.paymentMethod" value="ECpay">
                                <span class="radio-label">
                                    <i class="bi bi-ECpay"></i> 線上付款
                                </span>
                            </label>
                            <label class="payment-radio">
                                <input type="radio" v-model="orderForm.paymentMethod" value="cod">
                                <span class="radio-label">
                                    <i class="bi bi-truck"></i> 貨到付款
                                </span>
                            </label>
                        </div>
                    </div>
                </div>
            </div>

            <div class="summary-section">
                <div class="card">
                    <div class="card-header">訂單明細</div>
                    <div class="card-body p-0">
                        <ul class="order-list">
                            <li v-for="item in cartStore.items" :key="item.id" class="order-item">
                                <img 
                                    :src="item.image || 'https://placehold.jp/50x50.png?text=無圖'" 
                                    class="item-thumb" 
                                    @error="(e) => e.target.src = 'https://placehold.jp/24/cccccc/ffffff/50x50.png?text=無圖'"
                                >
                                
                                <div class="item-info">
                                    <p class="item-name">{{ item.name }}</p>
                                    <p class="item-price">NT$ {{ item.price }} x {{ item.quantity }}</p>
                                </div>
                                <div class="item-subtotal">
                                    NT$ {{ item.price * item.quantity }}
                                </div>
                            </li>
                        </ul>
                    </div>
                    <div class="card-footer">
                        <div class="total-row">
                            <span>商品小計</span>
                            <span>NT$ {{ cartStore.totalPrice }}</span>
                        </div>
                        <div class="total-row">
                            <span>運費</span>
                            <span :class="{ 'text-success': shippingFee === 0 && cartStore.totalPrice >= 2000 }">
                                <template v-if="cartStore.items.length === 0">NT$ 0</template>
                                <template v-else-if="shippingFee === 0">免運</template>
                                <template v-else>NT$ {{ shippingFee }}</template>
                            </span>
                        </div>
                        
                        <div v-if="cartStore.totalPrice < 2000" class="free-shipping-tip">
                            <small class="text-muted">
                                再買 NT$ {{ 2000 - cartStore.totalPrice }} 即可享免運優惠
                            </small>
                        </div>
                        <hr>
                        <div class="total-row final-price">
                            <span>總計</span>
                            <span>NT$ {{ finalTotalPrice }}</span>
                        </div>
                        <button @click="handleCheckout" class="btn-submit-order">確認送出訂單</button>
                        <button @click="router.push('/cart')" class="btn-back">返回購物車</button>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<style scoped>
.checkout-container {
    max-width: 1000px; /* 因為是直排，寬度縮小一點比較好看 */
    margin: 40px auto;
    padding: 0 20px;
}

/* 關鍵修改：改為垂直排列 */
.checkout-layout {
    display: flex;
    flex-direction: column; 
    gap: 30px;
    align-items: center;
}

.form-section, .summary-section {
    width: 100%; /* 寬度佔滿 */
}

/* 卡片樣式 */
.card {
    border: 3px solid #eee;
    width: 1000px;
    height: auto;
    border-radius: 12px;
    overflow: hidden;
    box-shadow: 0 4px 12px rgba(0,0,0,0.05);
}

.card-header {
    background: #f8f9fa;
    padding: 15px 20px;
    font-weight: bold;
    border-bottom: 1px solid #eee;
}

.card-body { padding: 20px; }

/* 表單樣式 */
.form-group {  margin-bottom: 15px; text-align: left;}
.form-group label { display: block; margin-bottom: 5px; font-size: 0.9rem; color: #666; }
.form-control {
    width: 100%;
    padding: 10px;
    border: 1px solid #cdbabab4;
    border-radius: 6px;
}

/* 付款方式 */
.payment-radio {
    display: flex;
    align-items: center;
    padding: 12px;
    border: 1px solid #eee;
    border-radius: 8px;
    margin-bottom: 10px;
    cursor: pointer;
    transition: 0.3s;
}

.payment-radio:hover { background: #f0fdf4; }
.payment-radio input { margin-right: 15px; }
.radio-label i { margin-right: 8px; color: #198754; }

/* 訂單明細清單 */
.order-list { list-style: none; padding: 0; margin: 0; }
.order-item {
    display: flex;
    align-items: center;
    padding: 15px 20px;
    border-bottom: 1px solid #f9f9f9;
}

.item-thumb { width: 50px; height: 50px; border-radius: 6px; object-fit: cover; margin-right: 15px; }
.item-info { flex-grow: 1; }
.item-name { font-size: 0.85rem; margin: 0; font-weight: 500; }
.item-price { font-size: 0.8rem; color: #888; margin: 0; }
.item-subtotal { font-weight: bold; font-size: 0.9rem; }

/* 總計區塊 */
.total-row { display: flex; justify-content: space-between; margin-bottom: 10px; }
.final-price { font-size: 1.2rem; font-weight: bold; color: #198754; }

.btn-submit-order {
    width: 100%;
    background: #198754;
    color: white;
    border: none;
    padding: 15px;
    border-radius: 8px;
    font-size: 1.1rem;
    font-weight: bold;
    cursor: pointer;
    margin-top: 20px;
    transition: 0.3s;
}

.btn-submit-order:hover { background: #146c43; }
.btn-back {
    width: 100%;
    background: transparent;
    border: 1px solid #ccc;
    color: #666;
    padding: 10px;
    border-radius: 8px;
    margin-top: 10px;
    cursor: pointer;
}

@media (max-width: 768px) {
    .checkout-layout { display: flex;
    flex-direction: column; 
    gap: 30px; }
    .summary-section { position: static; width: 100%; }
}
</style>