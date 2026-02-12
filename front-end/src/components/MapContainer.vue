<script setup>
import { onMounted, ref, watch, onUnmounted } from 'vue';
import L from 'leaflet';
import 'leaflet/dist/leaflet.css';

// Fix for Leaflet marker icons in Vite/Webpack
import markerIcon from 'leaflet/dist/images/marker-icon.png';
import markerIcon2x from 'leaflet/dist/images/marker-icon-2x.png';
import markerShadow from 'leaflet/dist/images/marker-shadow.png';

delete L.Icon.Default.prototype._getIconUrl;
L.Icon.Default.mergeOptions({
    iconRetinaUrl: markerIcon2x,
    iconUrl: markerIcon,
    shadowUrl: markerShadow
});

let map = null;
const markersGroup = L.layerGroup(); // 建立一個圖層群組，方便統一管理標記

// 1. 模擬動態接收的 API 資料 (這部分未來會從後端取得)
const searchResults = ref([
    { id: 1, title: '台北 101', lat: 25.0336, lng: 121.5648 },
    { id: 2, title: '國父紀念館', lat: 25.0394, lng: 121.5595 }
]);

// 2. 動態循環：將資料陣列轉換成地圖上的圖釘
const updateMarkers = (data) => {
    if (!map) return;
    
    markersGroup.clearLayers(); // 先清除舊的圖釘，避免搜尋後圖釘疊在一起

    data.forEach(item => {
        const marker = L.marker([item.lat, item.lng])
            .bindPopup(`<b>${item.title}</b><br>座標: ${item.lat}, ${item.lng}`);
        
        markersGroup.addLayer(marker); // 將個別圖釘加入群組
    });

    markersGroup.addTo(map); // 將整組圖釘放上地圖
};

onMounted(() => {
    // Initialize map
    map = L.map('leaflet-map').setView([25.0339, 121.5644], 13);
    
    // Add tile layer (OpenStreetMap)
    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
        attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
    }).addTo(map);

    // 初始執行一次畫圖釘
    updateMarkers(searchResults.value);
});

// 3. 監聽資料變化：當 API 回傳新資料時，地圖會自動更新
watch(searchResults, (newData) => {
    updateMarkers(newData);
}, { deep: true });

onUnmounted(() => {
    if (map) {
        map.remove();
    }
});
</script>

<template>
    <div id="leaflet-map" class="map-view"></div>
</template>

<style scoped>
/* Styles are handled in custom.scss as requested */
</style>