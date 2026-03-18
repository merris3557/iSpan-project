

<script setup>

import storeCard from '@/components/StoreCard.vue'
import { useRouter} from 'vue-router'
// import { useCartStore } from '@/stores/cart';
import banner from '@/components/common/Banner.vue'
import {onMounted} from 'vue';
import { useProductsDepot } from '@/stores/productsDepot';
import OrderToast from '@/components/OrderToast.vue'

const depot = useProductsDepot();
const router =useRouter();
const goToCart = () => {
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