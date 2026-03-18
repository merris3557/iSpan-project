<script setup>
import { ref } from 'vue';
import { useCartStore } from '@/stores/cart'
import { useRouter } from 'vue-router';
import BaseButton from '@/components/common/BaseButton.vue';
import { onMounted } from 'vue';
import Swal from 'sweetalert2';
import { cartAPI } from '@/api/cart';


const router = useRouter();
const cartStore = useCartStore()
const outOfStockItem = ref('')


//確保進入畫面就拿最新的資料
onMounted( async () => {
    await cartStore.fetchCart();
    // 讀取庫存不足的商品名稱
    const item = sessionStorage.getItem('outOfStockItem')
    if (item) {
        outOfStockItem.value = item
        sessionStorage.removeItem('outOfStockItem')  // 讀完就清掉
    }
})

const goToCheckout = async () => {
    try {
        await cartAPI.checkStock()
        router.push('/checkout')
    } catch (error) {
        const msg = error.response?.data?.error || '庫存檢查失敗'
        if (msg.includes('庫存不足')) {
            const parts = msg.split('：')
            const productName = parts[1] || ''
            const remainStock = parseInt(parts[2]) || 0
            await Swal.fire('庫存不足', 
                `「${productName}」庫存僅剩 ${remainStock} 件，請調整數量`, 
                'warning')
            // 同步更新購物車數量
            await cartStore.updateQuantityToStock(productName, remainStock)
        } else {
            Swal.fire('錯誤', msg, 'error')
        }
    }
}

const backToShop = () =>{
    //點擊返回選購商品
    router.push({name:'ShopStore'});
};
</script>

<template>

    <table class="table"  v-if="cartStore.items.length" style="vertical-align: middle ; margin-left:25px ; margin-right:25px"  >
        <thead>
        <tr >
            <th style="font-size: 20px; padding-bottom: 30px; padding-top: 30px">商品</th>
            <th style="font-size: 20px; padding-bottom: 30px; padding-top: 30px">價格</th>
            <th style="font-size: 20px; padding-bottom: 30px; padding-top: 30px">數量</th>
            <th></th>
            <th style="font-size: 20px; padding-bottom: 30px; padding-top: 30px">小計</th>
        </tr>
        </thead>

        <tbody >
        <tr  v-for="item in cartStore.items"  :key="item.id" :class="{ 'out-of-stock-row': item.outOfStock }">
            <td ><img :src="item.image " width=200px height="200px" class="card-img-left" :alt="item.productName" /></td>
            <td >{{ item.productName }}
                <span v-if="item.outOfStock"  
                style="color: #dc3545; font-size: 0.8rem; display: block;">
                ⚠ 庫存不足，請刪除或調整數量
                </span>
            </td>
            <td>NT$ {{ item.price }}</td>
            
                <td>
                    <div class="stepper-container" >
                        <BaseButton 
                            class="square-btn left-round" 
                            size="sm"
                            @click="cartStore.decrease(item.id)"
                        >
                            <i class="bi bi-dash"></i>
                        </BaseButton>
                        <div class="square-num" >
                            {{ item.quantity }}
                        </div>
                        <BaseButton 
                            class="square-btn right-round" 
                            size="sm"
                            @click="cartStore.increase(item.id)"
                            :disabled="item.outOfStock"
                        >
                            <i class="bi bi-plus-lg"></i>
                        </BaseButton>
                    </div>
                </td>

            


            <td>NT$ {{ item.price * item.quantity }}</td>
            <td>
            <BaseButton 
                color="outline-gdg"
                size="sm"
                label="刪除"
                @click="cartStore.remove(item.id)"
            />
            </td>
        </tr>
        </tbody>

    </table>

    <div style="text-align: center;" v-if="!cartStore.items.length">
        <h1 style="padding-top: 30px; padding-bottom: 30px;">🛒購物車是空的 </h1> 
    </div>


    <div class="text-right" style="margin-right:60px">
        <p style="font-size: 16px;">共:  {{ cartStore.totalQuantity }} 項商品 </p>

        <p style="font-size: 20px;">總金額：NT$ {{ cartStore.totalPrice }}</p>
    </div>

    

    <hr/>
    <div class="action-buttons-row" style="margin-right:30px">
        <div class="button-group-right" v-if="cartStore.items.length">
            <BaseButton 
                color="outline-gdg"
                label="&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp繼續選購&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp"
                @click="backToShop"
                
            />
        </div>
        <div class="button-group-right" v-if="cartStore.items.length">
            <BaseButton 
                color="gdg"
                label="&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp結帳&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp"
                @click="goToCheckout"
            />
        </div>
    </div>



    
</template>

<style scoped>
.stepper-container {
    display: flex;
    align-items: center;
    gap: 0;
}

.square-btn {
    width: 36px;
    height: 36px;
    padding: 0;
    display: flex;
    align-items: center;
    justify-content: center;
    border-radius: 0;
}

.left-round {
    border-top-left-radius: 4px;
    border-bottom-left-radius: 4px;
}

.right-round {
    border-top-right-radius: 4px;
    border-bottom-right-radius: 4px;
}

.square-num {
    width: 50px;
    height: 36px;
    display: flex;
    align-items: center;
    justify-content: center;
    border-top: 1px solid #dee2e6;
    border-bottom: 1px solid #dee2e6;
    background-color: #fff;
    font-weight: 500;
}

.action-buttons-row {
    display: flex;
    justify-content: flex-end;
    gap: 15px;
    margin-top: 60px;
    margin-bottom: 60px;
}

.button-group-right {
    display: flex;
    align-items: center; 
}

.text-right {
    text-align: right;
    margin-top: 20px;
    margin-bottom: 20px;
}   

.out-of-stock-row {
    opacity: 0.4;
    background-color: #f5f5f5;
    position: relative;
}

.out-of-stock-row td,
.out-of-stock-row img {
    filter: grayscale(50%);
}
.out-of-stock-row {
    opacity: 0.45;
    background-color: #f5f5f5;
}

.out-of-stock-row img {
    filter: grayscale(60%);
}
</style>

