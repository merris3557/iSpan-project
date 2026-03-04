<script setup>
// import { computed } from 'vue';
// import { useRoute } from 'vue-router';
// import DefaultLayout from '@/layouts/default.vue';
// import BlankLayout from '@/layouts/blank.vue';
import Navbar from '@/layouts/navbar.vue';
import Footer from '@/layouts/footer.vue';
import { onMounted } from 'vue';
import { useProductsDepot } from '@/stores/productsDepot';
import { useCartStore } from '@/stores/cart';
import { useAuthStore } from '@/stores/auth';
import { useAdminAuthStore } from '@/stores/adminAuth';


const depot = useProductsDepot();

onMounted(async () => {
    const authStore = useAuthStore()
    const adminAuthStore = useAdminAuthStore();
    const cartStore = useCartStore()
    
    if (authStore.isLoggedIn) {
        await cartStore.fetchCart()
        authStore.syncUserProfile();
    }
    if (adminAuthStore.isLoggedIn) {
        adminAuthStore.syncAdminProfile();
    }
})
// const route = useRoute();
</script>

<template>
  <RouterView />
</template>

<style scoped></style>
