<script setup>
import { onMounted, ref } from 'vue';
import { useProductsDepot } from '@/stores/productsDepot.js';
import InputNumber from 'primevue/inputnumber';
import Select from 'primevue/select';
import Button from 'primevue/button';
import Message from 'primevue/message';
import Swal from 'sweetalert2';


const props = defineProps({
    product: {
        type: Object,
        required: true
    }
});

const depot = useProductsDepot();
const emit = defineEmits(['close']);

// 響應式狀態
const selectedProduct = ref(null);
const editMode = ref('set'); // 'add', 'reduce', 'set'
const editValue = ref(0);



// 修改模式選項
const modes = [
    { label: '新增庫存 (+)', value: 'add' },
    { label: '調整總數 (=)', value: 'set' }
];

onMounted(() => {
    editValue.value = props.product.stock;
})



// 處理更新邏輯
const handleConfirm =async () => {

    // 呼叫 store 的 action 更新庫存
    await depot.updateStock(props.product.id,editMode.value, editValue.value);

    await Swal.fire({
        icon: 'success',
        title: '庫存更新成功',
        text: `產品 ${props.product.productName} 的庫存已更新為 ${props.product.stock} 件`,
        timer: 1500,
        showConfirmButton: false
    });

    // 重設輸入框
    editValue.value = 0;
    emit('close')
};
</script>

<template>

    <div class="edit-inner-container">
        <div class="flex items-center justify-between mb-4">
            <h3 class="text-lg font-bold text-gray-700">
                編輯商品：<span class="text-blue-600">{{ props.product.productName }}</span>
            </h3>
            <span class="text-sm text-gray-500">商品編號: {{ props.product.id }}</span>
        </div>

            <transition name="fade">
                <div  class="mt-4 border-t pt-4">
                    <Message severity="secondary" :closable="false" class="mb-4">
                        目前庫存狀態：<span class="font-bold text-lg text-primary">{{ props.product.stock }}</span> 件
                    </Message>

                    <div class="flex flex-wrap gap-4 items-end">
                        <div class="flex flex-col gap-2">
                            <span class="text-sm text-gray-500" >2. 修改方式 &nbsp;&nbsp; </span>
                            
                            <Select
                                v-model="editMode" 
                                :options="modes" 
                                optionLabel="label" 
                                optionValue="value" 
                                class="w-44"
                                >
                            </Select>
                        </div>

                        <br/>
                        <div class="flex flex-col gap-2">
                            <span class="text-sm text-gray-500">3. 輸入數量 &nbsp;&nbsp;</span>
                            <InputNumber 
                                v-model="editValue"
                                :min="0"
                                showButtons 
                                class="w-40"
                                buttonLayout="horizontal"
                                >
                            </InputNumber>
                        </div>

                        <br/>
                        <div class="w-full flex gap-2 justify-content:center" >
                        <Button
                            label="確認修改" 
                            @click="handleConfirm" 
                            severity="success"
                            class="mb-1" 
                        />
                    
                        <Button
                            label="取消"
                        @click="emit('close')"
                        severity="secondary"
                        outlined
                        variant="text"
                        class="mb-1 w-[120px]"
                        />
                        </div>
                    </div>
                </div>
            </transition>
    </div> 
</template>

<style scoped>
/* 簡單的淡入動畫 */
.fade-enter-active, .fade-leave-active {
    transition: opacity 0.3s ease, transform 0.3s ease;
}
.fade-enter-from, .fade-leave-to {
    opacity: 0;
    transform: translateY(-10px);
}
</style>