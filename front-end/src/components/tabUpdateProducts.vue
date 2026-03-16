<script setup>
import { ref, watch } from 'vue';
import { useProductsDepot } from '@/stores/productsDepot';
import Swal from 'sweetalert2';
import apiWrapper from '@/api/config';

const depot = useProductsDepot();
const selectedId = ref('');
const editForm = ref(null);

// 當選擇商品 ID 變動時，自動帶入該商品原始資料
watch(selectedId, (newId) => {
    const product = depot.products.find(p => p.id === newId);
    if (product) {
        editForm.value = { ...product }; // 使用解構賦值避免直接修改原始物件
    } else {
        editForm.value = null;
    }
});

const handleUpdate = async () => {
    try{
        const response = await apiWrapper.put(`/products/${editForm.value.id}`, {
                productName: editForm.value.productName,
                price: editForm.value.price,
                image: editForm.value.image,
                productDescription: editForm.value.description,
                
                stock: editForm.value.stock
            
        });

        const index = depot.products.findIndex(p => p.id === editForm.value.id);
            if (index !== -1) {
                // 更新 Store 裡的資料
                depot.products[index] = { ...editForm.value };
                
                Swal.fire({
                    icon: 'success',
                    title: '商品更新成功',
                    timer: 1500
                });

                selectedId.value = ''
                editForm.value = null
            }
    } catch (error) {
        console.error("更新失敗", error);

        const errorMessage = error.response?.data || "更新失敗，請稍再試"

        Swal.fire({
            icon: 'error',
            title: '更新出錯',
            text: errorMessage,
            confirmButtonText: '確定'
        }).then((result) => {
            if (result.isConfirmed) {
                if(error.response?.status === 404) {
                    window.location.reload();
                }
            }
        })
    }
};


// 執行刪除
const confirmDelete = async (item) => {
    const result = await Swal.fire({
        title: '確定要刪除嗎？',
        text: `商品「${item.productName}」刪除後將無法還原！`,
        icon: 'warning',
        showCancelButton: true,
        confirmButtonColor: '#d33',
        cancelButtonColor: '#3085d6',
        confirmButtonText: '是的，刪除它',
        cancelButtonText: '取消'
    });
    
    if (result.isConfirmed) {
        try {
            await depot.deleteProduct(item.id);

            // 刪除成功，顯示成功訊息
            await Swal.fire({
                icon: 'success',
                title: '已刪除',
                text: '該商品已從資料庫清單中移除',
                timer: 1500
            });
            
            // 清空表單
            selectedId.value = '';
            editForm.value = null;
            
        } catch (error) {
            console.error('刪除商品失敗:', error);
            
            Swal.fire({
                icon: 'error',
                title: '刪除失敗',
                text: '此商品可能已被預訂、無法刪除',
                timer: 2000
            });
        }
    }
};

const handleImageUpload = (event) => {
    const file = event.target.files[0]
    if (!file) return

    const reader = new FileReader()
    reader.onload = (e) => {
        editForm.value.image = e.target.result // Base64 字串
    }
    reader.readAsDataURL(file)
}

</script>

<template>
    <div class="content-card">
        <div class="card-header"><h2 class="card-title">編輯現有商品</h2></div>
        <br/>
        <div class="form-container">
            <div class="form-group">
                <label>選擇要編輯的商品: &nbsp;&nbsp;</label>
                <select v-model="selectedId" class="select-input">
                    <option value="" disabled>-- 請選擇商品 --</option>
                    <option v-for="p in depot.products" :key="p.id" :value="p.id">
                        [{{ p.id }}] {{ p.productName }}
                    </option>
                </select>
            </div>

            <hr v-if="editForm" />

            <div v-if="editForm" class="edit-fields">
                <div class="form-group">
                    <label>商品名稱: &nbsp;&nbsp;</label>
                    <input v-model="editForm.productName" type="text" />
                </div>
                
                <div class="form-row">
                    <div class="form-group">
                        <label>售價 (NT$): &nbsp;&nbsp; </label>
                        <input v-model.number="editForm.price" type="number" />
                    </div>
                    <br/>
                    <div class="form-group">
                        <label>庫存數量: &nbsp;&nbsp;</label>
                        <input v-model.number="editForm.stock" type="number" />
                    </div>
                    <br/>
                </div>
                <div class="form-group">
                    <label>商品描述: &nbsp;&nbsp;</label>
                    <textarea v-model="editForm.description" rows="4"></textarea>
                </div>
                <br/>
                <div class="form-group">
                    <label>商品圖片: &nbsp;&nbsp;</label>
                    <input 
                        type="file" 
                        accept="image/*" 
                        @change="handleImageUpload" 
                        style="margin-bottom: 8px;"
                    />
                    <div v-if="editForm.image" style="margin-top: 8px;">
                        <img :src="editForm.image" style="width: 120px; height: 120px; object-fit: contain; border: 1px solid #ddd; border-radius: 6px; background: #f8f7f4;" />
                    </div>
                </div>
                <br/>
                <button @click="handleUpdate" class="btn-update">儲存修改</button>
                <button @click="confirmDelete(editForm)" class="btn-delete" title="刪除商品">
                                    <i class="bi bi-trash"></i>
                                </button>
            </div>
        </div>
    </div>
</template>

<style scoped>

.select-input { 
    padding: 0.75rem; 
    border: 1px solid #ddd; 
    border-radius: 6px; 
    background: white; 
}

.btn-update { 
    background-color: #059669; 
    color: white; 
    padding: 1rem; 
    border: none; 
    border-radius: 6px; 
    cursor: pointer; 
    font-weight: bold; 
    margin-top: 1rem; 
}
.edit-fields { 
    display: flex; 
    flex-direction: column; 
    gap: 1rem; 
    }

.btn-delete { 
    background-color: #d7d9d8f1; 
    color: white; 
    padding: 1rem; 
    border: none; 
    border-radius: 6px; 
    cursor: pointer; 
    font-weight: bold; 
    margin-top: 1rem; 
}

input, textarea { 
    padding: 0.75rem; 
    border: 1px solid #ddd; 
    border-radius: 6px; 
    field-sizing: content;
}
</style>