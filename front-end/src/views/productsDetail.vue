<script setup>

import { ref, computed } from 'vue';
import { useRoute } from 'vue-router';
import { useCartStore } from '@/stores/cart';

import Swal from 'sweetalert2';
import { useProductsDepot } from '@/stores/productsDepot';


const route = useRoute();
const cartStore = useCartStore();
const depot = useProductsDepot();
const buyQuantity = ref(1);

const products = computed(() => {
    const routedId = route.params.id;
    // 使用 find 尋找時，確保雙方都是字串，避免類型錯誤 (如 1 !== "1")
    const found = depot.products.find(p => String(p.id) === String(routedId));
    
    if (found) {
        console.log(`[詳情頁動態追蹤] 商品: ${found.productName}, 當前最新庫存: ${found.stock}`);
    }
    
    return found;
});

const updateQuantity = (val) => {
    const nextQty = buyQuantity.value + val;
    // 連動 JSON 中的 stock 庫存
    if (nextQty >= 1 && nextQty <= products.value.stock) {
        buyQuantity.value = nextQty;
    }
};

const handleAddToCart = async () => {
    try{
        await cartStore.addToCart({
        id: products.value.id,
        name: products.value.productName,
        price: products.value.price,
        image: products.value.image,
        stock: products.value.stock, // 傳遞庫存給購物車
        quantity: buyQuantity.value // 傳遞選購數量
        });

        Swal.fire({
            icon: 'success',
            title: '成功加入購物車',
            text: `已加入 ${buyQuantity.value} 件商品`,
            timer: 1500,
            showConfirmButton: false
        });

    } catch (error) {
        Swal.fire({
            icon: 'error',
            title: '加入失敗',
            text: '請檢察網路連線或登入狀態'
        });
    } 
};
</script>



<template>
    <div v-if="products" class="products-detail-container">
        <div class="products-layout">
        <div class="products-image-section">
            <img :src="products.image" :alt="products.productName" class="main-image" />
        </div>

        <div class="products-info-section">
            <nav class="breadcrumb">首頁 / 質感選物 / {{ products.productName }}</nav>
            
            <h1 class="products-title">{{ products.productName }}</h1>
            <p class="products-price">NT$ {{ products.price }}</p>
            
            <hr class="divider" />

            <div class="products-description">
            <h3>商品描述</h3>
            <p>{{ products.description }}</p>
            </div>

            <div class="purchase-section">
            <div class="stock-status">
                庫存狀態：
                <span :class="products.stock > 0 ? 'in-stock' : 'out-of-stock'">
                {{ products.stock > 0 ? `剩餘 ${products.stock} 件` : '補貨中' }}
                </span>
            </div>

            <div class="action-row">
                <div class="stepper-container">
                    <button type="button" class="square-btn left-round" @click="updateQuantity(-1)" :disabled="buyQuantity <= 1">
                        <i class="bi bi-dash"></i>
                    </button>
                    <div class="square-num">{{ buyQuantity }}</div>
                    <button 
                        type="button" 
                        class="square-btn right-round" 
                        @click="updateQuantity(1)"
                        :disabled="buyQuantity >= products.stock"
                    >
                        <i class="bi bi-plus-lg"></i>
                    </button>
                </div>
                <small v-if="buyQuantity >= products.stock" class="text-danger">已達庫存上限</small>

                <button 
                class="add-to-cart-btn" 
                @click="handleAddToCart"
                :disabled="products.stock <= 0"
                >
                加入購物車
                </button>
            </div>
            </div>
        </div>
        </div>

        <div class="description-bottom-section">
            <ul class="nav nav-tabs">
                <li class="nav-item">
                    <span class="nav-link active">商品描述</span>
                </li>
            </ul>
            <div class="description-content">
                <p>{{ products.description }}</p>
                <div class="extra-info">
                    <p><strong>【 溫馨提示 】</strong></p>
                    <ul>
                        <li>全館消費滿 NT$ 2,000 免運費。</li>
                        <li>響應環保，出貨將使用減塑包裝。</li>
                        <li>本商品享 7 天鑑賞期（拆封後除瑕疵外恕不退換）。</li>
                    </ul>
                </div>
            </div>
        </div>



    </div>

    
</template>



<style scoped>
/* 佈局樣式 */
.products-detail-container {
    max-width: 1200px;
    margin: 40px auto;
    padding: 0 20px;
}

.products-layout {
    display: flex;
    gap: 60px;
    flex-wrap: wrap;
}

.products-image-section {
    flex: 1;
    min-width: 300px;
}

.main-image {
    width: 100%;
    border-radius: 12px;
    box-shadow: 0 4px 20px rgba(0,0,0,0.05);
}

.products-info-section {
    flex: 1;
    min-width: 300px;
    text-align: left;
}

/* 文字樣式 */
.breadcrumb {
    font-size: 0.9rem;
    color: #c3ba97;
    margin-bottom: 15px;
}

.products-title {
    font-size: 2rem;
    font-weight: bold;
    margin-bottom: 10px;
    color: #198754;
}

.products-price {
    font-size: 1.5rem;
    color: #555; 
    font-weight: bold;
}c3ba97

.divider {
    margin: 25px 0;
    border: 0;
    border-top: 1px solid #eee;
}

.products-description h3 {
    font-size: 1.1rem;
    margin-bottom: 10px;
}

.products-description p {
    color: #555;
    line-height: 1.6;
}

/* 庫存與按鈕區 */
.purchase-section {
    margin-top: 30px;
    background: #f9f9f9;
    padding: 20px;
    border-radius: 8px;
}

.stock-status {
    margin-bottom: 15px;
    font-size: 0.9rem;
}

.in-stock { color: #c3ba97; }
.out-of-stock { color: #dc3545; }

.action-row {
    display: flex;
    align-items: center;
    gap: 20px;
}

.add-to-cart-btn {
    flex-grow: 1;
    height: 40px;
    background-color: #c3ba97;
    color: white;
    border: none;
    border-radius: 8px;
    font-weight: bold;
    cursor: pointer;
    transition: 0.3s;
}

.add-to-cart-btn:hover {
    background-color: #e4e0d3;
    }


.stepper-container {
    display: inline-flex;
    gap: 0;
    align-items: center;
}

.square-btn {
    width: 40px;
    height: 40px;
    background-color: #c3ba97; /* 配合綠色理念 */
    color: white;
    border: none;
    cursor: pointer;
    display: flex;
    justify-content: center;
    align-items: center;
}
.square-btn:hover {
    background-color: #e4e0d3;
    }

.square-num {
    width: 50px;
    height: 40px;
    background-color: white;
    color: #68634ea7;
    display: flex;
    justify-content: center;
    align-items: center;
    font-weight: bold;
    border-top: 1px solid #c3ba97;
    border-bottom: 1px solid #c3ba97;
}

.left-round { border-radius: 8px 0 0 8px; }
.right-round { border-radius: 0 8px 8px 0; }

button:disabled {
    background-color: #ccc;
    cursor: not-allowed;
}

/* 下方描述 */
.description-bottom-section {
    margin-top: 60px;
    border-top: 1px solid #eee;
    padding-top: 40px;
}

.nav-tabs {
    border-bottom: 2px solid #c3ba97;
    margin-bottom: 25px;
}

.nav-link.active {
    background-color: #c3ba97;
    color: white !important;
    border: none;
    border-radius: 8px 8px 0 0;
    padding: 10px 25px;
    display: inline-block;
    font-weight: bold;
}

.description-content {
    padding: 20px 0;
    color: #444;
    line-height: 1.8;
    font-size: 1.05rem;
    max-width: 800px; /* 限制寬度讓長文字易於閱讀 */
}

.extra-info {
    margin-top: 40px;
    padding: 20px;
    background-color: #f8f9fa;
    border-radius: 8px;
    font-size: 0.9rem;
    color: #666;
}

.extra-info ul {
    padding-left: 20px;
    margin-top: 10px;
}

/* 響應式微調 */
@media (max-width: 768px) {
    .products-layout {
        gap: 30px;
    }
    .description-content {
        font-size: 1rem;
    }
}
</style>