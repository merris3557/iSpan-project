<script setup>
import {useCartStore} from '@/stores/cart'
import { useProductsDepot } from '@/stores/productsDepot';
import Swal from 'sweetalert2';

import { ref, computed, onMounted } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import BaseCard from '@/components/common/BaseCard.vue';
import BaseButton from '@/components/common/BaseButton.vue';
import { useAuthStore } from '@/stores/auth';

const router = useRouter();
const route = useRoute();
const cartStore = useCartStore()
const depot = useProductsDepot();
const searchKeyword = ref(route.query.search || '')
const activeCategory = ref('全部')
const categories = ['全部', '生鮮', '食品', '日常用品']


const filteredProducts = computed(() => {
    return depot.products
        .filter(x => activeCategory.value === '全部' || x.category === activeCategory.value)
        .filter(x => x.productName.includes(searchKeyword.value))
        .map(x => ({
            id: x.id,
            productName: x.productName,
            price: x.price,
            description: x.description,
            image: x.image,
            stock: x.stock,
            category: x.category
        }))
})


const goToDetail = (id) =>{
    //點擊進入商品詳情
    router.push({name:'productsDetail', params:{id}});
};

const addToCart = async (item) => {

    const authStore = useAuthStore();

    if (!authStore.isLoggedIn) {
        const result = await Swal.fire({
            title: '請先登入',
            text: '登入後即可將商品加入購物車',
            icon: 'info',
            showCancelButton: true,
            confirmButtonText: '前往登入',
            cancelButtonText: '取消'
        });

        if(result.isConfirmed) {
            console.log('儲存跳轉路徑：', router.currentRoute.value.fullPath)
            localStorage.setItem('shopRedirectPath', router.currentRoute.value.fullPath);  // 改這行
            router.push('/login');
        }
        return;
    }


    try{
        //檢查購物車已有的數量
        const cartItem = cartStore.items.find(i => String(i.productId) === String(item.id))
        const alreadyInCart = cartItem ? cartItem.quantity : 0

        if (alreadyInCart + 1 > item.stock) {
            Swal.fire({
                icon: 'warning',
                title: '庫存不足',
                text: `購物車已有 ${alreadyInCart} 件，已達庫存上限`
            })
            return
        }


        await cartStore.addToCart({
            id: item.id,
            name: item.productName,
            price: item.price,
            image: item.image,
            description: item.description,
            stock:item.stock ?? 99,
            quantity: 1
        })
    
        Swal.fire({
            icon: 'success',
            title: '成功加入購物車',
            text: `已選購  ${item.productName}`,
            timer: 1500,
            showConfirmButton: false
        });
    } catch (error) {
        Swal.fire('錯誤', '加入購物車失敗', 'error')
    }

}





const productsList = computed(() => {
    return depot.products.map(x =>({
        id: x.id,
        productName: x.productName,
        price:x.price,
        description: x.description,
        image: x.image,
        stock: x.stock 
    }));
});


</script>


<template>
    <div class="shop-container">
        <!-- 搜尋 bar -->
        <div class="search-bar">
            <input v-model="searchKeyword" type="text" placeholder="🔍 搜尋商品..." class="search-input" />
        </div>
        

        <!-- 分類 tab -->
        <div class="category-tabs">
            <button 
                v-for="cat in categories" 
                :key="cat"
                :class="['cat-btn', { active: activeCategory === cat }]"
                @click="activeCategory = cat"
            >
                {{ cat }}
            </button>
        </div>

    <div class="shop-grid">

    <!-- 卡片  -->
    <BaseCard 
        v-for="item in filteredProducts" 
        :key="item.id" 
        :hover-effect="true"
        max-width="450px"
        class="product-card"
        @click="goToDetail(item.id)"
    >
        <template #header>
            <img :src="item.image" width="450" height="450" class="card-img-top" :alt="item.productName" />
        </template>
        
        <div class="text-center">
            <h5 class="card-title">{{item.productName}}</h5>
            
            <div class="price text-success">NT$ {{item.price}}</div>
        </div>
        
        <template #footer>
            <div class="text-center">
                <div v-if="item.stock <= 0" 
                    style="width:90%; margin: 0 auto; padding: 8px; background:#eee; color:#999; border-radius:6px; text-align:center; font-size:0.9rem;">
                    補貨中
                </div>
                <BaseButton 
                    v-else
                    color="gdg" 
                    label="加入購物車"
                    @click.stop="addToCart(item)"
                    width="90%"
                />
            </div>
        </template>
    </BaseCard>
    </div>
</div>
</template>

<style scoped>
.shop-container {
    padding: 40px 20px;
}

.shop-grid {
    display: grid;
    grid-template-columns: repeat(3, 1fr);  
    gap: 20px;
    align-items: start;
}

.product-card {
    cursor: pointer;
    width: 100%;
}

/* aspect-ratio 讓圖片等比縮放*/
.product-card img {
    width: 100%;
    aspect-ratio: 1 / 1;
    object-fit: cover;
    height: auto;
    display: block;
}

.card-title {
    margin: 0 0 8px;
    font-size: 1.2rem;      
    font-weight: 700;
}

.price {
    font-size: 1.1rem;      
    font-weight: 600;
    margin-top: 8px;
}

.search-bar {
    display: flex;
    justify-content: center;
    margin-bottom: 20px;
}

.search-input {
    width: 100%;
    max-width: 500px;
    padding: 10px 20px;
    border: 1px solid #c3ba97;
    border-radius: 25px;
    font-size: 1rem;
    outline: none;
}

.search-input:focus {
    border-color: #9f9572;
    box-shadow: 0 0 0 3px rgba(159, 149, 114, 0.15);
}

.category-tabs {
    display: flex;
    justify-content: center;
    gap: 12px;
    margin-bottom: 30px;
    flex-wrap: wrap;
}

.cat-btn {
    padding: 8px 24px;
    border: 1px solid #c3ba97;
    border-radius: 25px;
    background: white;
    color: #9f9572;
    cursor: pointer;
    font-size: 0.95rem;
    transition: 0.2s;
}

.cat-btn:hover {
    background: #f5f2ea;
}

.cat-btn.active {
    background: #9f9572;
    color: white;
    border-color: #9f9572;
}
</style>