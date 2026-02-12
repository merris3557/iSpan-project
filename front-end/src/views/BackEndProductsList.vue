<script setup>

import { ref, computed } from 'vue'
import tabStock from '@/components/tabStock.vue'
import tabIncreaseProducts from '@/components/tabIncreaseProducts.vue'
import tabUpdateProducts from '@/components/tabUpdateProducts.vue'

// 1. 定義頁籤資料
const tabs = [
    { label: '庫存列表', component: 'tabStock' },
    { label: '新增商品', component: 'tabIncreaseProducts' },
    { label: '編輯商品', component: 'tabUpdateProducts' }
    
]

// 2. 當前選中的組件名稱
const currentTab = ref('tabStock')

// 3. 映射組件物件 (如果是 Vite 環境，建議直接存組件物件)
const components = {
    tabStock,
    tabIncreaseProducts,
    tabUpdateProducts
}

const currentTabComponent = computed(() => components[currentTab.value])

</script>

<template>


<br/>
<br/>
<div class="erp-transfer-page">
    <div class="page-header">
        <h1 class="page-title">商品管理</h1>
        <p class="page-subtitle">管理庫存與商品資訊</p>
        
    </div>



    






    <!-- Tabs -->
    
            <div class="tabs-container">
                
                <ul class="nav-tabs">
                    <li v-for="(tab, index) in tabs" 
                    :key="index"
                    class="tab-item">
                        <a 
                        :class="['tab-link',{'active-tab': currentTab == tab.component}]"
                        @click.prevent="currentTab = tab.component">
                            {{ tab.label }}
                            <button class="close-btn" ></button>
                        </a>
                    </li>
                </ul>
            </div>

                    <div class="tabs-content">
                        <keep-alive>
                            <component :is="currentTabComponent"></component>
                        </keep-alive>
                    </div>
                    
                
            
</div>

</template>





<style lang="scss" scoped>
@import '@/assets/styles/custom.scss';

.erp-transfer-page {
    max-width: 1200px;
    margin: 0 auto;
}

    .page-header {
        margin-bottom: 1.5rem;

    .page-title {
        font-size: 1.875rem;
        font-weight: 700;
        color: #1f2937;
        margin: 0 0 0.5rem 0;
    }

    .page-subtitle {
        color: #6b7280;
        font-size: 1rem;
        margin: 0;
    }
}

/* Tabs */
.tabs-container {
    margin-bottom: 1.5rem;
    border-bottom: 2px solid #e5e7eb;
    }
    

    .nav-tabs {
    display: flex;
    list-style: none;
    padding: 0;
    margin: 0;
    gap: 0.5rem;
    }

    .tab-item {
        
        .tab-link {
            display: flex;
            align-items: center;
            gap: 0.75rem;
            padding: 0.75rem 1.5rem;
            background-color: #f3f4f6;
            color: #6b7280;
            text-decoration: none;
            border-radius: 8px 8px 0 0;
            font-size: 0.875rem;
            font-weight: 500;
            transition: all 0.2s ease;

            &:hover {
            background-color: #e5e7eb;
            }

            &.active-tab {
            background-color: #fff;
            color: #1e3c72;
            border-color:#e5e7eb;
            border-bottom: 3px solid #1e3c72;
            }

            .close-btn {
            background: none;
            border: none;
            padding: 0;
            color: #9ca3af;
            cursor: pointer;
            font-size: 1.125rem;
            line-height: 1;
            transition: color 0.2s ease;

            &:hover {
                color: #ef4444;
            }
            }
        }
    }


.tabs-content {
    margin-bottom: 0px;
}
</style>