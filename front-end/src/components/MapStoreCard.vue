<script setup>
import { RouterLink } from 'vue-router';

const props = defineProps({
    store: {
        type: Object,
        required: true,
        default: () => ({
            storeId: null,
            storeName: '店家名稱N/A',
            coverImage: null,
            openHoursSummary: '營業時間N/A',
            address: '無地址資訊',
            description: '暫無簡介'
        })
    }
})
const getImageUrl = (imgName) => {
    if (!imgName) return 'https://placehold.co/150';
    return `/pictures/StoreProfile/${imgName}`;
};
</script>

<template>
    <div class="card mb-3 card-gdg rounded-4 overflow-hidden">
        <div class="row g-0">
            <div class="col-md-4">
                <img :src="getImageUrl(props.store.coverImage)"
                    class="img-fluid rounded-start h-100 object-fit-contain"
                    :alt="props.store.storeName"
                    style="min-height: 150px;">
            </div>
            <div class="col-md-8">
                <div class="card-body">
                    <!-- 店家名稱：可點擊，連結至店家介紹頁 -->
                    <h5 class="card-title fw-bold">
                        <RouterLink
                            v-if="props.store.storeId"
                            :to="`/storeInfo/${props.store.storeId}`"
                            class="store-link text-gdg"
                        >
                            {{ props.store.storeName }}
                        </RouterLink>
                        <span v-else class="text-gdg">{{ props.store.storeName }}</span>
                    </h5>
                    <p class="card-text mb-1">
                        <small class="text-body-secondary">
                            <i class="bi bi-clock me-1"></i>{{ props.store.openHoursSummary || '營業時間未提供' }}
                        </small>
                    </p>
                    <p class="card-text mb-1">
                        <small class="text-body-secondary">
                            <i class="bi bi-geo-alt me-1"></i>{{ props.store.address || '地址未提供' }}
                        </small>
                    </p>
                    <p class="card-text description-text">{{ props.store.description }}</p>
                    <!-- 標籤 -->
                    <div v-if="props.store.categories && props.store.categories.length" class="mt-1">
                        <span
                            v-for="tag in props.store.categories"
                            :key="tag"
                            class="badge rounded-pill me-1 tag-badge"
                        >{{ tag }}</span>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<style scoped>
.object-fit-contain {
    object-fit: contain;
    width: 100%;
}
.description-text {
    display: -webkit-box;
    -webkit-line-clamp: 2;
    line-clamp: 2;
    -webkit-box-orient: vertical;
    overflow: hidden;
    font-size: 0.9rem;
}
/* 店家名稱連結樣式：去除底線，hover 時底線出現 */
.store-link {
    text-decoration: none;
    transition: opacity 0.2s;
}
.store-link:hover {
    text-decoration: underline;
    opacity: 0.8;
}
/* 標籤樣式 */
.tag-badge {
    background-color: #f0ece0;
    color: #776f54;
    font-size: 0.72rem;
    font-weight: 500;
    border: 1px solid #c8c0a0;
}
</style>