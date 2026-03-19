<script setup>
import { onMounted, watch, onUnmounted } from 'vue';
import { useRouter } from 'vue-router';
import { useMapSearchStore } from '@/stores/mapSearchStore';
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

const router = useRouter();
const mapSearchStore = useMapSearchStore();

let map = null;
const markersGroup = L.layerGroup();

/**
 * 依搜尋結果更新地圖圖釘
 * 每筆資料需有 latitude / longitude 欄位
 */
const updateMarkers = (data) => {
    if (!map) return;

    markersGroup.clearLayers();

    if (!data || data.length === 0) return;

    data.forEach(store => {
        if (!store.latitude || !store.longitude) return;
        const marker = L.marker([store.latitude, store.longitude])
            .bindPopup(
                `<a class="store-popup-link" data-id="${store.storeId}" style="font-weight:bold;cursor:pointer;color:#9f9572;text-decoration:underline;">` +
                `${store.storeName}</a><br>` +
                `<small>${store.address || ''}</small>`
            );
        markersGroup.addLayer(marker);
    });

    markersGroup.addTo(map);

    // 自動調整地圖視野以涵蓋所有圖釘
    if (markersGroup.getLayers().length > 0) {
        const group = L.featureGroup(markersGroup.getLayers());
        map.fitBounds(group.getBounds().pad(0.2));
    }
};

onMounted(() => {
    map = L.map('leaflet-map').setView([25.0339, 121.5644], 13);

    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
        attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
    }).addTo(map);

    // 監聽 popup 開啟事件，綁定店名連結的點擊跳轉
    map.on('popupopen', () => {
        const link = document.querySelector('.store-popup-link');
        if (link) {
            link.addEventListener('click', () => {
                const storeId = link.getAttribute('data-id');
                if (storeId) {
                    router.push({ name: 'StoreInfo', params: { id: storeId } });
                }
            });
        }
    });
});

// 監聽 store 中搜尋結果的變化
watch(
    () => mapSearchStore.results,
    (newData) => updateMarkers(newData),
    { deep: true }
);

onUnmounted(() => {
    if (map) map.remove();
});
</script>

<template>
    <div id="leaflet-map" class="map-view"></div>
</template>

<style scoped>
/* Styles are handled in custom.scss as requested */
</style>