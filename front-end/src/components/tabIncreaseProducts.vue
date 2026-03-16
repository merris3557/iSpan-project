<script setup>
import { ref } from 'vue';
import { useProductsDepot } from '@/stores/productsDepot';
import Swal from 'sweetalert2';
import apiWrapper from '@/api/config';

const depot = useProductsDepot();
const fileInputRef = ref(null);
const isSubmitting = ref(false);

// 初始化表單資料
const form = ref({
    productName: '',
    price: 0,
    stock: 0,
    description: '',
    image: '' // 預設圖片
});

const handleAddProduct =async () => {
    if (!form.value.productName || form.value.price <= 0) {
        Swal.fire('錯誤', '請填寫完整的商品名稱與正確價格', 'error');
        return;
    }

    isSubmitting.value = true;

    try{
        const response = await apiWrapper.post('/products/add', {
            productName:form.value.productName,
            price: form.value.price,
            stock: form.value.stock,
            productDescription: form.value.description,
            image: form.value.image,
        });

        await Swal.fire({
            icon: 'success',
            title: '新增成功',
            text: '商品已存入資料庫並同步至庫存',
            timer: 1500
        });

        //新增成功後呼叫store的方法，重新從資料庫抓資料
        await depot.fetchProducts();

        form.value= { productName: '', price: 0, stock: 0 , description: '', image: ''};

        if (fileInputRef.value) {
            fileInputRef.value.value = "";
        }


    } catch (error) {
        console.error("存檔失敗", error);
        Swal.fire('錯誤', '後端連線失敗，請檢察API是否啟動', 'error')
    } finally {
        isSubmitting.value = false; 
    }

};

const handleImageUpload = (event) => {
    const file = event.target.files[0]; //抓選取的檔案
    if (!file) return;

    if (file.size > 4 *1024 *1024) {
        Swal.fire('錯誤', '圖片大小不能超過4MB', 'error');
        return;
    }

    //fileReader讀取圖片
    const reader = new FileReader();
    reader.onload = (e) =>{
        form.value.image = e.target.result;
    };
    reader.readAsDataURL(file);
}   



</script>

<template>
    <div class="content-card">
        <div class="card-header"><h2 class="card-title">新增商品項目</h2></div>
        <div class="form-container">
            <div class="form-group">
                <label>商品名稱</label>
                <input v-model="form.productName" type="text" placeholder="請輸入商品名稱" />
            </div>

            <div class = "from-group">
                <label>商品照片</label>
                <div class="image-upload-wrapper">
                    <div class = "image-preview" v-if="form.image">
                        <img :src="form.image" alt="預覽圖" />
                    </div>
                    <div class="image-placeholder" v-else>
                        <span>尚未選擇圖片</span>
                    </div>
                    <input type="file"
                            accept="image/*"
                            @change="handleImageUpload"
                            id="file-input"
                            ref="fileInputRef"
                            />
                </div>
            </div>

            <div class="form-row">
                <div class="form-group">
                    <label>售價 (NT$)</label>
                    <input v-model.number="form.price" type="number" />
                </div>
                <div class="form-group">
                    <label>初始庫存</label>
                    <input v-model.number="form.stock" type="number" />
                </div>
            </div>
            <div class="form-group">
                <label>商品描述</label>
                <textarea v-model="form.description" rows="4" placeholder="請輸入詳細描述..."></textarea>
            </div>
            <button @click="handleAddProduct" :disabled="isSubmitting" class="btn-submit">{{ isSubmitting ? '處理中...，請勿重複點選按鈕' : '確認新增商品'}}</button>
        </div>
    </div>
</template>

<style scoped>
.form-container { 
    display: flex; 
    flex-direction: column; 
    gap: 1.5rem; 
    padding: 1rem; 
}

.form-row { 
    display: flex; 
    gap: 1rem; 
}

.form-group { 
    flex: 1; display: flex; 
    flex-direction: column; 
    gap: 0.5rem; 
}

input, textarea { 
    padding: 0.75rem; 
    border: 1px solid #ddd; 
    border-radius: 6px; 
}

.btn-submit { 
    background-color: #1e3c72; 
    color: white; 
    padding: 1rem; 
    border: none; 
    border-radius: 6px; 
    cursor: pointer; 
    font-weight: bold; 
}
.btn-submit:hover { 
    background-color: #2a5298; 
    }
    

.image-upload-wrapper {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 1rem;
    border: 2px dashed #ccc;
    padding: 1rem;
    border-radius: 8px;
    background-color: #fafafa;
}

.image-preview img {
    max-width: 200px;
    max-height: 200px;
    border-radius: 4px;
    object-fit: cover;
    box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}

.image-placeholder {
    width: 200px;
    height: 120px;
    display: flex;
    align-items: center;
    justify-content: center;
    color: #999;
    background: #eee;
    border-radius: 4px;
}

#file-input {
    cursor: pointer;
    width: 100%;
}
</style>