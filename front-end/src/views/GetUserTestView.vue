<script setup>
import { ref, onMounted } from 'vue';
import { useAuthStore } from '@/stores/auth';
import { storeToRefs } from 'pinia';
import userAPI from '@/api/user';

const authStore = useAuthStore();
const { user, isLoggedIn } = storeToRefs(authStore);

// 用於存取後端回傳資料
const backendUser = ref(null);
const loading = ref(false);
const error = ref(null);

// 呼叫後端 API 取得資料
const fetchFromBackend = async () => {
    loading.value = true;
    error.value = null;
    try {
        const response = await userAPI.getProfile();
        // 假設後端回傳格式為 { success: true, data: { ... } }
        backendUser.value = response.data;
    } catch (err) {
        console.error('Fetch error:', err);
        error.value = '無法從後端取得資料，請確認是否已登入';
    } finally {
        loading.value = false;
    }
};

onMounted(() => {
    if (isLoggedIn.value) {
        fetchFromBackend();
    }
});
</script>

<template>
    <div class="container mt-5">
        <div class="card shadow">
            <div class="card-header bg-primary text-white">
                <h3 class="mb-0">登入者資料存取範例 (測試頁面)</h3>
            </div>
            <div class="card-body">
                <div v-if="!isLoggedIn" class="alert alert-warning">
                    您尚未登入，請先 <router-link to="/login">登入</router-link> 後再進行測試。
                </div>

                <div v-else>
                    <!-- 第 1 部份：從 Front-end Pinia Store 取得 -->
                    <div class="section mb-5">
                        <h4 class="text-secondary border-bottom pb-2">1. 從 Pinia Store (前端) 取得</h4>
                        <p class="text-muted small">適用場景：導覽列顯示姓名、判斷頁面權限。</p>
                        <div class="p-3 bg-light rounded">
                            <ul class="list-unstyled mb-0">
                                <li><strong>姓名 (Name):</strong> <span class="text-primary">{{ authStore.userName }}</span></li>
                                <li><strong>Email:</strong> {{ user?.email }}</li>
                                <li><strong>是否為商家 (isStore):</strong> 
                                    <span :class="user?.isStore ? 'text-success' : 'text-danger'">
                                        {{ user?.isStore ? '是 (True)' : '否 (False)' }}
                                    </span>
                                </li>
                            </ul>
                        </div>
                        <div class="mt-2 text-muted x-small">
                            代碼參考：<code>const authStore = useAuthStore(); console.log(authStore.user.name);</code>
                        </div>
                    </div>

                    <!-- 第 2 部份：從 Back-end API 取得 -->
                    <div class="section">
                        <h4 class="text-secondary border-bottom pb-2">2. 從 後端 API (資料庫) 取得</h4>
                        <p class="text-muted small">適用場景：需要最新資料、執行重要操作前的驗證。</p>
                        
                        <div v-if="loading" class="text-center py-3">
                            <div class="spinner-border text-primary" role="status"></div>
                            <p>載入中...</p>
                        </div>

                        <div v-else-if="error" class="alert alert-danger">
                            {{ error }}
                        </div>

                        <div v-else-if="backendUser" class="p-3 border rounded">
                            <pre class="bg-dark text-info p-3 rounded"><code>{{ JSON.stringify(backendUser, null, 2) }}</code></pre>
                            <p class="mt-3">
                                <strong>後端回傳實體欄位：</strong><br>
                                Email: {{ backendUser.email }} <br>
                                ID: {{ backendUser.id }} <br>
                                商家狀態: {{ backendUser.isStore }}
                            </p>
                        </div>

                        <button @click="fetchFromBackend" class="btn btn-outline-primary mt-3">
                            重新從後端抓取資料
                        </button>
                    </div>
                </div>
            </div>
            <div class="card-footer text-center">
                <router-link to="/" class="btn btn-secondary btn-sm">回首頁</router-link>
            </div>
        </div>
    </div>
</template>

<style scoped>
pre {
    max-height: 400px;
    overflow-y: auto;
}
.x-small {
    font-size: 0.75rem;
}
</style>
