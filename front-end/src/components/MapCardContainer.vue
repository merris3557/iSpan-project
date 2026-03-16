<script setup>
import { computed } from 'vue';
import { useMapSearchStore } from '@/stores/mapSearchStore';
import MapStoreCard from './MapStoreCard.vue';

const mapSearchStore = useMapSearchStore();

// 直接使用 store 的搜尋結果
const stores = computed(() => mapSearchStore.results);
</script>

<template>
    <div class="map-card-container">
        <div class="p-2">
            <!-- 載入中狀態 -->
            <div v-if="mapSearchStore.loading" class="text-center py-4 text-secondary">
                <div class="spinner-border spinner-border-sm me-2" role="status"></div>
                搜尋中...
            </div>

            <!-- 錯誤狀態 -->
            <div v-else-if="mapSearchStore.error" class="alert alert-warning py-2 small">
                {{ mapSearchStore.error }}
            </div>

            <!-- 無結果 -->
            <div v-else-if="stores.length === 0" class="text-center py-4 text-secondary small">
                <i class="bi bi-search me-1"></i>請輸入關鍵字或選擇標籤進行搜尋
            </div>

            <!-- 搜尋結果 -->
            <template v-else>
                <MapStoreCard v-for="store in stores" :key="store.storeId" :store="store" />
            </template>
        </div>
    </div>
</template>

<style scoped>
.map-card-container {
    height: 100%;
    overflow-y: auto;
}

/* Custom scrollbar for Webkit (Chrome, Safari, Edge) */
.map-card-container::-webkit-scrollbar {
    width: 6px;
}

.map-card-container::-webkit-scrollbar-track {
    background: #f1f1f1;
}

.map-card-container::-webkit-scrollbar-thumb {
    background: #c1c1c1;
    border-radius: 4px;
}

.map-card-container::-webkit-scrollbar-thumb:hover {
    background: #a8a8a8;
}
</style>