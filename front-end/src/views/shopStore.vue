

<script setup>

import storeCard from '../components/storeCard.vue'
import { useRouter} from 'vue-router'
// import { useCartStore } from '@/stores/cart';
import banner from '../components/common/banner.vue'
import {onMounted} from 'vue';
import { useProductsDepot } from '@/stores/productsDepot';
import OrderToast from '@/components/OrderToast.vue'

const depot = useProductsDepot();
const router =useRouter();
const goToCart = () => {
  // 括號內填寫在 router/index.js 設定的 path
    router.push('/cart') 
}


onMounted(() => {
    depot.fetchProducts();
})



</script>

<template>
<OrderToast />

<banner />





<storeCard />







    
</template>

<style scoped>
    
</style>