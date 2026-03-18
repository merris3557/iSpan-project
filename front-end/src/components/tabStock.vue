
<script setup>

import { ref, computed, onMounted} from 'vue';
import {useProductsDepot} from '@/stores/productsDepot';


import TabStockCRUD from '@/components/TabStockCRUD.vue';




const expandedId = ref(null);
const depot = useProductsDepot();
const products = computed(() => depot.products);

onMounted(() => {
    depot.fetchProducts();
})


const toggleEdit = (id) =>{
    if (expandedId.value === id) {
        expandedId.value = null;    // 點擊同一行則收起

    }else {
        expandedId.value = id;   // 點擊不同的行則展開
    }
}




//上架顯示上架時間，如果有更新庫存顯示更新庫存時間
const displayTime = (item) => {
    const timeToDisplay = item.lastUpdate || item.added;
    if (!timeToDisplay) return '---';

    const date = new Date(timeToDisplay);
    return date.toLocaleString('zh-TW', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour12: false
    })
}




</script>

<template>



    <div class="content-card">
        <div class="card-header">
            <h2 class="card-title">所有商品</h2>
            
        </div>
        <div class="table-responsive">
            <table class="data-table">
            <thead>
                <tr>
                <th>商品編號</th>
                <th>商品</th>
                <th>售價</th>
                <th>庫存數量</th>
                <th>日期</th>
                <th>編輯庫存</th>
                </tr>
            </thead>
            <tbody>
                <template v-for="item in products" :key="item.id" >
                    <tr :class="{'is-editing': expandedId === item.id}" >
                        <td class="order-id">{{ item.id }}</td>
                        <td>{{ item.productName }}</td>
                        <td class="amount">NT$ {{ item.price }}</td>
                        <td class="stock">{{ item.stock }}</td>
                        
                        <td class="date">{{ displayTime(item) }}</td>
                        <td>
                            <div class ="action-buttons">
                                <button @click="toggleEdit(item.id)" class="btn-action btn-edit" title="編輯">
                                    <i class="bi bi-pencil"></i>
                                </button>
                                
                            </div>
                        </td>
                    </tr>

                <tr v-if="expandedId === item.id" class="edit-row">
                    <td colspan="6">
                        <div class="edit-container">
                            <TabStockCRUD :product="item" @close="expandedId = null"/>
                        </div>
                    </td>
                </tr>
                </template>
            </tbody>
            </table>
            <div v-if="products.length === 0" class="empty-state">
                目前沒有任何商品，請前往「新增商品」頁籤。
            </div>
        </div>
    </div>







</template>


<style>

/* Content Card */
.content-card {
    background: #fff;
    border-radius: 12px;
    box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
    padding: 2rem;
}

    .card-header {
        padding: 1.5rem;
        border-bottom: 1px solid #e5e7eb;
        display: flex;
        justify-content: space-between;
        align-items: center;

    .card-title {
        font-size: 1.25rem;
        font-weight: 600;
        color: #1f2937;
        margin: 0;
    }

    .view-all-link {
        color: #1e3c72;
        text-decoration: none;
        font-size: 0.875rem;
        font-weight: 500;
        display: flex;
        align-items: center;
        gap: 0.25rem;
        transition: color 0.2s ease;

        &:hover {
            color: #2a5298;
        }
    }
}


/* Table Styles */
.table-responsive {
    overflow-x: auto;
}

.data-table {
    width: 100%;
    border-collapse: collapse;

    thead {
        background-color: #f9fafb;

    th {
        padding: 1rem 1.5rem;
        text-align: left;
        font-size: 0.875rem;
        font-weight: 600;
        color: #6b7280;
        text-transform: uppercase;
        letter-spacing: 0.05em;
        border-bottom: 2px solid #e5e7eb;
        }
    }

    tbody {
        tr {
        border-bottom: 1px solid #e5e7eb;
        transition: background-color 0.2s ease;

        &:hover {
            background-color: #f9fafb;
        }

        &:last-child {
            border-bottom: none;
        }
        }

        td {
        padding: 1rem 1.5rem;
        font-size: 0.875rem;
        color: #374151;

        &.order-id {
            font-family: 'Courier New', monospace;
            font-weight: 600;
            color: #1e3c72;
        }

        &.amount {
            font-weight: 600;
            color: #059669;
        }

        &.date {
            color: #6b7280;
        }
        }
    }
}

.action-buttons {
    display: flex;
    gap: 0.5rem;
}



.btn-action {
    width: 32px;
    height: 32px;
    border: none;
    border-radius: 6px;
    display: flex;
    align-items: center;
    justify-content: center;
    cursor: pointer;
    transition: all 0.2s ease;
    font-size: 0.875rem;

    &.btn-view {
        background-color: #e0e7ff;
        color: #4338ca;

        &:hover {
        background-color: #c7d2fe;
        }
    }

    &.btn-edit {
        background-color: #dbeafe;
        color: #1e40af;

        &:hover {
        background-color: #bfdbfe;
        }
    }
}


/* 下拉展開樣式 */
.edit-row {
    background-color: #f8fafc; 
    animation: fadeIn 0.3s ease; 
}

.edit-container {
    padding: 1.5rem;
    border: 1px inset #e5e7eb;
}


tr.is-editing {
    background-color: #eff6ff !important;
}

@keyframes fadeIn {
    from { opacity: 0; transform: translateY(-10px); }
    to { opacity: 1; transform: translateY(0); }
}


.btn-action {
    border: none;
    outline: none;
}
</style>
