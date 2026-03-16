<script setup>
import { ref, onMounted, computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { storeAPI } from '@/api/store';
import BaseButton from '@/components/common/BaseButton.vue';
import BaseCard from '@/components/common/BaseCard.vue';
import { useAuthStore } from '@/stores/auth';
import { useProductsDepot } from '@/stores/productsDepot';
import { useCartStore } from '@/stores/cart';
import Swal from 'sweetalert2';

const router = useRouter();
const authStore = useAuthStore();
const route = useRoute();
const depot = useProductsDepot();
const cartStore = useCartStore();

// 店家資訊相關
const storeName = ref('');
const storeDescription = ref('');
const storePhone = ref('');
const storeAddress = ref('');
const myLabels = ref([]);
const coverImage = ref(''); // 儲存資料庫回傳的檔名

// 計算圖片路徑
const getImageUrl = (imgName) => {
    if (!imgName) return 'https://placehold.co/600x400?text=No+Image';
    return `/pictures/StoreProfile/${imgName}`;
};

// 獲取店家資訊(將後端資料庫值存入對應的 ref 變數)
const fetchStoreInfo = async () => {
    const id = route.params.id;
    if (!id) return;

    try {
        const response = await storeAPI.getStoreInfoById(id);
        console.log('API Response:', response);

        // 防禦性檢查：確保 response 存在且具有 success 屬性
        if (response && response.success) {
            const data = response.data;
            if (data) {
                storeName.value = data.storeName;
                storeDescription.value = data.description;
                storePhone.value = data.storePhone;
                storeAddress.value = data.address;
                coverImage.value = data.coverImage;
                myLabels.value = data.categories || [];
            } else {
                console.warn('店家資料為空');
            }
        } else {
            console.error('後端回傳失敗:', response ? response.message : '無回應');
        }
    } catch (error) {
        console.error('獲取店家資訊失敗:', error);
    }
};

// 頁面載入時獲取店家資訊與商品庫存
onMounted(async () => {
    fetchStoreInfo();
    if (depot.products.length === 0) {
        await depot.fetchProducts();
    }
});

// 計算屬性：篩選專屬商品
const storeProducts = computed(() => {
    const storeKw = storeName.value.trim();
    if (!storeKw || depot.products.length === 0) return [];
    
    return depot.products.filter(p => 
        p.productName.includes(storeKw) || storeKw.includes(p.productName)
    );
});

// 跳轉到商品詳細頁面
const goToDetail = (id) => {
    router.push({ name: 'productsDetail', params: { id } });
};

// 前往訂位頁面
const goToReservation = () => {
    if (!authStore.isLoggedIn) {
        // 如果沒登入，跳轉到登入頁，並帶上當前路徑以便登入後跳回來
        router.push({ name: 'Login', query: { redirect: route.fullPath } });
        return;
    }
    // 已登入，跳轉到訂位頁
    router.push({ name: 'Reservation', params: { id: route.params.id } });
};

// 加入購物車功能 (延用 storeCard.vue 的邏輯)
const addToCart = async (item) => {
    if (!authStore.isLoggedIn) {
        const result = await Swal.fire({
            title: '請先登入',
            text: '登入後即可將商品加入購物車',
            icon: 'info',
            showCancelButton: true,
            confirmButtonText: '前往登入',
            cancelButtonText: '取消'
        });

        if (result.isConfirmed) {
            localStorage.setItem('shopRedirectPath', router.currentRoute.value.fullPath);
            router.push('/login');
        }
        return;
    }

    try {
        await cartStore.addToCart({
            id: item.id,
            name: item.productName,
            price: item.price,
            image: item.image,
            description: item.description,
            stock: item.stock ?? 99,
            quantity: 1
        });
    
        Swal.fire({
            icon: 'success',
            title: '成功加入購物車',
            text: `已選購  ${item.productName}`,
            timer: 1500,
            showConfirmButton: false
        });
    } catch (error) {
        Swal.fire('錯誤', '加入購物車失敗', 'error');
    }
};

</script>

<template>
    <div class="container py-4">
        <div v-if="storeName" class="mb-4">
            <div class="card shadow-sm mb-4">
                <div class="row g-0">
                    <div class="col-md-5">
                        <img :src="getImageUrl(coverImage)" class="img-fluid rounded-start h-100 object-fit-contain"
                            :alt="storeName">
                    </div>
                    <div class="col-md-7">
                        <div class="card-body p-4">
                            <h1 class="card-title text-gdg h2 mb-3">{{ storeName }}</h1>

                            <div class="mb-3">
                                <span v-for="cat in myLabels" :key="cat.categoryId" class="badge bg-secondary me-2">
                                    {{ cat.categoryName }}
                                </span>
                            </div>

                            <div class="mb-4 border-bottom pb-3">
                                <p class="card-text text-muted">{{ storeDescription || '暫無描述' }}</p>
                            </div>

                            <div class="row g-3">
                                <div class="col-12">
                                    <i class="bi bi-geo-alt-fill text-gdg me-2"></i>
                                    <strong>地址：</strong> {{ storeAddress }}
                                </div>
                                <div class="col-12">
                                    <i class="bi bi-telephone-fill text-gdg me-2"></i>
                                    <strong>電話：</strong> {{ storePhone }}
                                </div>
                            </div>

                            <div class="mt-4 pt-3">
                                <div>
                                    <i class="bi bi-calendar-check-fill text-gdg me-2"> *預約功能需先登入</i>
                                    <br>
                                    <div class="d-flex gap-3 mt-3">
                                        <BaseButton color="gdg" @click="goToReservation" class="px-5">
                                            我要訂位
                                        </BaseButton>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <div v-else class="text-center py-5">
            <div class="spinner-border text-gdg mb-3" role="status">
                <span class="visually-hidden">Loading...</span>
            </div>
            <p class="text-muted">正在載入專屬店家資訊...</p>
        </div>

        <!-- 本店商品區塊 -->
        <div v-if="storeProducts.length > 0" class="mt-5">
            <h3 class="mb-4 text-gdg border-start border-4 border-gdg ps-3">本店專屬商品</h3>
            <div class="shop-grid">
                <BaseCard 
                    v-for="item in storeProducts" 
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
    </div>
</template>

<style scoped>
.shop-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));  
    gap: 24px;
    align-items: start;
}

.product-card {
    cursor: pointer;
    width: 100%;
}

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
</style>