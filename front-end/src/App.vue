<script setup>
import { onMounted } from 'vue';
import { useAuthStore } from '@/stores/auth';
import { useAdminAuthStore } from '@/stores/adminAuth';

const authStore = useAuthStore();
const adminAuthStore = useAdminAuthStore();

onMounted(() => {
    // 每次載入應用程式時，如果在 localStorage 有登入紀錄，就去跟後端拿最新資料
    if (authStore.isLoggedIn) {
        authStore.syncUserProfile();
    }
    
    if (adminAuthStore.isLoggedIn) {
        adminAuthStore.syncAdminProfile();
    }
});
</script><template>
  <RouterView />
</template>

<style scoped></style>
