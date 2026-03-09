<script setup>
import { ref, onMounted, computed } from 'vue';
import SelectLabel from '@/components/SelectLabel.vue';
import BaseButton from '@/components/common/BaseButton.vue';
import Swal from 'sweetalert2';
import storeAPI from '@/api/store';

// 編輯狀態(預設為 false，表示非編輯模式)
const isEditing = ref(false);

// 店家資訊相關
const storeName = ref('');
const storeDescription = ref('');
const storePhone = ref('');
const storeAddress = ref('');
const myLabels = ref([]);
const coverImage = ref(''); // 儲存資料庫回傳的檔名

// 圖片上傳相關
const selectedFile = ref(null);
const imagePreview = ref(null);
const fileInput = ref(null);
const removeImageFlag = ref(false); // 標記是否刪除圖片

// 用於儲存編輯前的原始資料
const originalData = ref({
    storeName: '',
    description: '',
    storePhone: '',
    address: '',
    coverImage: ''
});

// 計算圖片路徑
const getImageUrl = (imgName) => {
    if (!imgName || removeImageFlag.value) return 'https://placehold.co/600x400?text=No+Image';
    // 圖片現在存放在 front-end/public/pictures/StoreProfile
    // 在 Vite 中，public 資料夾的內容會映射到服務器的根目錄 /
    return `/pictures/StoreProfile/${imgName}`;
};

// 獲取店家資訊(將後端資料庫值存入對應的 ref 變數)
const fetchStoreInfo = async () => {
    try {
        const response = await storeAPI.getMyStoreInfo();
        if (response && response.success) {
            const data = response.data;
            storeName.value = data.storeName || '';
            storeDescription.value = data.description || '';
            storePhone.value = data.storePhone || '';
            storeAddress.value = data.address || '';
            coverImage.value = data.coverImage || '';
            myLabels.value = data.categories || []; // 取得現有標籤備份

            // 重設上傳與刪除相關狀態
            selectedFile.value = null;
            imagePreview.value = null;
            removeImageFlag.value = false;
        }
    } catch (error) {
        console.error('獲取店家資訊失敗:', error);
        if (error.response && error.response.status === 403) {
            Swal.fire('權限不足', '您目前不具備店家身分', 'error');
        }
    }
};

// 頁面載入時獲取店家資訊
onMounted(() => {
    fetchStoreInfo();
});

// 處理標籤新增與刪除
const addLabel = (newLabel) => {
    // 檢查是否已存在 (根據 ID)
    if (!myLabels.value.some(tag => tag.categoryId === newLabel.categoryId)) {
        myLabels.value.push(newLabel);
    }
};

const removeLabel = (index) => {
    myLabels.value.splice(index, 1);
};

// 處理編輯相關
const startEditing = () => {
    // 備份原始資料
    originalData.value = {
        storeName: storeName.value,
        description: storeDescription.value,
        storePhone: storePhone.value,
        address: storeAddress.value,
        coverImage: coverImage.value,
        categories: [...myLabels.value]
    };
    isEditing.value = true;
};

const handleCancel = () => {
    Swal.fire({
        title: '確定要取消編輯嗎？',
        text: '所有變更將不會被儲存',
        icon: 'warning',
        showCancelButton: true,
        confirmButtonColor: '#9f9572',
        cancelButtonColor: '#d33',
        confirmButtonText: '確定取消',
        cancelButtonText: '繼續編輯'
    }).then((result) => {
        if (result.isConfirmed) {
            // 還原原始資料
            storeName.value = originalData.value.storeName;
            storeDescription.value = originalData.value.description;
            storePhone.value = originalData.value.storePhone;
            storeAddress.value = originalData.value.address;
            coverImage.value = originalData.value.coverImage;

            // 清除預覽與旗標
            selectedFile.value = null;
            imagePreview.value = null;
            removeImageFlag.value = false;
            myLabels.value = [...originalData.value.categories];
            isEditing.value = false;
        }
    });
};

// 處理圖片選擇
const handleFileChange = (event) => {
    const file = event.target.files[0];
    if (file) {
        // 檢查格式
        if (!file.type.startsWith('image/')) {
            Swal.fire('錯誤', '只能上傳圖片檔案', 'error');
            event.target.value = '';
            return;
        }

        selectedFile.value = file;
        removeImageFlag.value = false; // 若選擇了新圖，則取消刪除標記
        // 建立預覽圖
        const reader = new FileReader();
        reader.onload = (e) => {
            imagePreview.value = e.target.result;
        };
        reader.readAsDataURL(file);
    }
};

const triggerFileInput = () => {
    fileInput.value.click();
};

const cancelImageUpload = () => {
    selectedFile.value = null;
    imagePreview.value = null;
    if (fileInput.value) fileInput.value.value = '';
};

// 刪除目前圖片
const removeCurrentImage = () => {
    Swal.fire({
        title: '確定要移除目前圖片嗎？',
        text: '這將會在儲存後永久刪除圖片檔案',
        icon: 'question',
        showCancelButton: true,
        confirmButtonColor: '#d33',
        confirmButtonText: '預定移除',
        cancelButtonText: '保留'
    }).then((result) => {
        if (result.isConfirmed) {
            removeImageFlag.value = true;
            selectedFile.value = null;
            imagePreview.value = null;
        }
    });
};

const handleSave = async () => {
    try {
        // 使用 FormData 以支援檔案上傳
        const formData = new FormData();
        formData.append('storeName', storeName.value);
        formData.append('description', storeDescription.value);
        formData.append('storePhone', storePhone.value);
        formData.append('address', storeAddress.value);
        formData.append('removeImage', removeImageFlag.value);

        if (selectedFile.value) {
            formData.append('imageFile', selectedFile.value);
        }

        // 加入標籤 ID 列表
        myLabels.value.forEach(tag => {
            formData.append('categoryIds', tag.categoryId);
        });

        // 標示需要更新標籤 (即使是空列表)
        formData.append('updateCategories', 'true');

        await storeAPI.updateMyStoreInfo(formData);

        await Swal.fire({
            icon: 'success',
            title: '成功儲存資訊！',
            confirmButtonText: '確定',
        });

        // 重新取得資訊以更新圖片路徑 (檔名會變)
        await fetchStoreInfo();
        isEditing.value = false;
    } catch (error) {
        console.error('儲存店家資訊失敗:', error);
        Swal.fire('儲存失敗', error.response?.data?.message || '發生錯誤，請確認資料格式是否正確。', 'error');
    }
};
</script>

<template>
    <div class="container py-4">
        <h1 class="text-gdg mb-4">店家資訊編輯頁面</h1>

        <!-- 店家封面圖片區域 -->
        <div class="store-image-section mb-4">
            <label class="form-label text-gdg fw-bold">店家封面圖片：</label>
            <div class="image-container position-relative mb-2">
                <img :src="imagePreview || getImageUrl(coverImage)" class="img-fluid border rounded" alt="Store Cover"
                    style="max-height: 400px; width: 100%; object-fit: contain; background-color: #f8f9fa;">

                <div v-if="isEditing" class="mt-2 d-flex gap-2">
                    <BaseButton color="gdg" @click="triggerFileInput">選擇圖片</BaseButton>
                    <input type="file" ref="fileInput" @change="handleFileChange" accept="image/*" class="d-none">

                    <BaseButton v-if="selectedFile" color="info" @click="cancelImageUpload">取消選擇</BaseButton>
                    <BaseButton v-if="coverImage && !removeImageFlag" color="danger" @click="removeCurrentImage">刪除目前圖片
                    </BaseButton>
                    <BaseButton v-if="removeImageFlag" color="gdg" @click="removeImageFlag = false">復原圖片</BaseButton>
                </div>
            </div>
            <p v-if="selectedFile" class="text-muted small">已選擇檔案: {{ selectedFile.name }}</p>
            <p v-if="removeImageFlag" class="text-danger small fw-bold">目前已標記為刪除，儲存後將生效。</p>
        </div>

        <div class="store-name-section mb-4">
            <h2 class="h3 fw-bold text-gdg">{{ storeName }}</h2>
        </div>

        <div class="info-section mb-4">
            <div v-if="!isEditing">
                <label class="form-label text-gdg fw-bold">店家簡介：</label>
                <p class="p-3 border bg-light">{{ storeDescription }}</p>
            </div>
            <div v-else>
                <label class="form-label text-gdg fw-bold">編輯簡介：</label>
                <textarea v-model="storeDescription" class="form-control border-gdg" rows="3"></textarea>
            </div>
        </div>

        <div class="info-section mb-4">
            <label class="form-label text-gdg fw-bold">連絡電話：</label>
            <p class="p-3 border bg-light">{{ storePhone }}</p>
        </div>

        <div class="info-section mb-4">
            <label class="form-label text-gdg fw-bold">店家地址：</label>
            <p class="p-3 border bg-light">{{ storeAddress }}</p>
        </div>

        <div class="label-section mb-4">
            <h3 class="text-gdg h5 mb-3">店舖標籤：</h3>
            <div class="tag-list d-flex flex-wrap gap-2">
                <span v-for="(tag, index) in myLabels" :key="tag.categoryId"
                    class="badge rounded-0 border border-gdg text-gdg p-2 d-flex align-items-center">
                    {{ tag.categoryName }}
                    <button v-if="isEditing" @click="removeLabel(index)" class="btn-close ms-2"
                        style="font-size: 0.5rem;"></button>
                </span>
            </div>

            <div v-if="isEditing" class="edit-box mt-3 p-3 border bg-gdg-light">
                <SelectLabel @add="addLabel" />
            </div>
        </div>

        <div class="mb-5 d-flex gap-2">
            <BaseButton v-if="!isEditing" color="gdg" @click="startEditing">編輯資訊</BaseButton>
            <template v-else>
                <BaseButton color="gdg" @click="handleSave">儲存編輯</BaseButton>
                <BaseButton color="danger" @click="handleCancel">取消編輯</BaseButton>
            </template>
        </div>

        <hr class="my-5">

        <div class="mt-4">
            <h2 class="text-gdg h4 mb-3">訂位編輯系統</h2>
            <RouterLink :to="{ name: 'Seats' }" v-slot="{ navigate }">
                <BaseButton color="gdg" @click="navigate">點擊進入系統</BaseButton>
            </RouterLink>
        </div>
    </div>
</template>

<style scoped>
.border-gdg {
    border-color: #9f9572 !important;
}

.image-container img {
    border: 4px solid #9f9572;
}
</style>
