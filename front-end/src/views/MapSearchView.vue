<script setup>
import { onMounted } from 'vue';
import { useMapSearchStore } from '@/stores/mapSearchStore';
import MapContainer from '@/components/MapContainer.vue';
import MapCardContainer from '@/components/MapCardContainer.vue';
import SearchBar from '@/components/SearchBar.vue';

const mapSearchStore = useMapSearchStore();

onMounted(() => {
    // 預設載入全部店家(findAll)
    mapSearchStore.searchStores();
});
</script>

<template>
    
<div class="page-container-gdg">
        <div class="page-title-gdg">美味座標</div>
        <div class="page-description-gdg">在城市的經緯間，標記你的美味時刻。</div>
        <div><SearchBar></SearchBar></div>
        
        <div class="map-layout-gdg">
            <div class="map-main-wrapper">
                <MapContainer></MapContainer>
            </div>
            
            <div class="map-sidebar-wrapper">
                <MapCardContainer></MapCardContainer>
            </div>
        </div>
    </div> 
        

</template>

<style scoped>

</style>