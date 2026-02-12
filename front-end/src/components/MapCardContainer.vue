<script setup>
import { ref } from 'vue';
import MapStoreCard from './MapStoreCard.vue';
import storeData from '@/data/stores.json';

const stores = ref(storeData);

</script>

<template>
    <div class="map-card-container">
        <div class="p-2">
            <h5 class="mb-3 ps-1 fw-bold text-secondary">搜尋結果 ({{ stores.length }})</h5>
            <MapStoreCard v-for="(store, index) in stores" :key="index" :store="store" />
        </div>
    </div>
</template>

<style scoped>
.map-card-container {
    height: 100%; /* Fill parent flex container */
    overflow-y: auto;
    /* background-color: #f8f9fa; Light gray background */
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