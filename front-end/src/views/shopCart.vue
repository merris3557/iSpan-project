<script setup>
import { useCartStore } from '@/stores/cart'
import { useRouter } from 'vue-router';
import BaseButton from '@/components/common/BaseButton.vue';
import { onMounted } from 'vue';

const router = useRouter();
const cartStore = useCartStore()

//確保進入畫面就拿最新的資料
onMounted(() => {
    cartStore.fetchCart();
})

const goToCheckOut = () =>{
    //點擊進入結帳
    router.push({name:'checkOut'});
};

const backToShop = () =>{
    //點擊返回選購商品
    router.push({name:'ShopStore'});
};
</script>

<template>

    <table class="table"  v-if="cartStore.items.length" style="vertical-align: middle ; margin-left:25px ; margin-right:25px"  >
        <thead>
        <tr>
            <th style="font-size: 20px; padding-bottom: 30px; padding-top: 30px">商品</th>
            <th style="font-size: 20px; padding-bottom: 30px; padding-top: 30px">價格</th>
            <th style="font-size: 20px; padding-bottom: 30px; padding-top: 30px">數量</th>
            <th></th>
            <th style="font-size: 20px; padding-bottom: 30px; padding-top: 30px">小計</th>
        </tr>
        </thead>

        <tbody >
        <tr  v-for="item in cartStore.items"  :key="item.id">
            <img :src="item.image " width=200px height="200px" class="card-img-left" :alt="item.productName" />
            <td >{{ item.productName }}</td>
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
                @click="goToCheckOut"
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
    align-items: center; /* 防止按鈕高度被拉伸 */
}

.text-right {
    text-align: right;
    margin-top: 20px;
    margin-bottom: 20px;
}   
</style>



<!-- 
<script setup>
import { useCartStore } from '@/stores/cart'
import { useRouter } from 'vue-router';
import BaseButton from '@/components/common/BaseButton.vue';
import { onMounted } from 'vue';

const router = useRouter();
const cartStore = useCartStore()

//確保進入畫面就拿最新的資料
onMounted(() => {
    cartStore.fetchCart();
})
console.log('[購物車頁面] 目前購物車內容:', cartStore.items);

const goToCheckOut = () =>{
    //點擊進入結帳
    router.push({name:'checkOut'});
};

const backToShop = () =>{
    //點擊返回選購商品
    router.push({name:'ShopStore'});
};
</script>

<template>

    <table class="table"  v-if="cartStore.items.length" style="vertical-align: middle ; margin-left:25px ; margin-right:25px"  >
        <thead>
        <tr>
            <th style="font-size: 20px; padding-bottom: 30px; padding-top: 30px">商品</th>
            <th style="font-size: 20px; padding-bottom: 30px; padding-top: 30px">價格</th>
            <th style="font-size: 20px; padding-bottom: 30px; padding-top: 30px">數量</th>
            <th></th>
            <th style="font-size: 20px; padding-bottom: 30px; padding-top: 30px">小計</th>
        </tr>
        </thead>

        <tbody >
        <tr  v-for="item in cartStore.items"  :key="item.id">
            <img :src="item.image || 'https://placehold.jp/24/cccccc/ffffff/50x50.png?text=無圖'" width=200px height="200px" class="card-img-left" :alt="item.productName" />
            <td >{{ item.productName }}</td>
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
                @click="goToCheckOut"
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
    align-items: center; /* 防止按鈕高度被拉伸 */
}

.text-right {
    text-align: right;
    margin-top: 20px;
    margin-bottom: 20px;
}   
</style> -->