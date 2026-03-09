import { defineStore } from 'pinia';
import apiWrapper from '@/api/config';

export const useProductsDepot = defineStore('productDepot', {
    state: () => ({
        products: []
    }),

    actions: {
        //更新庫存統一邏輯
        async fetchProducts() {
            const getCategory = (name) => {
                if (!name) return '其他'
                if (['鮮蔬', '鮮果', '蔬菜', '水果', '柿', '果禮盒'].some(k => name.includes(k))) return '生鮮'
                if (['果糖', '辛香料', '香料'].some(k => name.includes(k))) return '食品'
                if (['餐具', '護理', '運動瓶', '髮蠟', '紙袋'].some(k => name.includes(k))) return '日常用品'
                return '其他'
            }

            try {
                const response = await apiWrapper.get('/products/all')  // ← 這行不見了

                // apiWrapper 已經把 response.data 取出來了，所以 response 本身就是陣列
                const data = Array.isArray(response) ? response : (response.data || [])
                
                this.products = data.map(p => ({
                    id: p.productId,
                    productName: p.productName,
                    price: p.price,
                    description: p.productDescription,
                    image: p.image,
                    stock: p.stock ? p.stock.availableQuantity : 0,
                    added: p.added,
                    productCode: p.productCode,
                    lastUpdate: p.stock ? p.stock.updateAt : null,
                    category: getCategory(p.productName)
                }))
            } catch (error) {
                console.error("獲取資料失敗", error)
            }
        },

        async updateStock(id, type, amount) {
            try {
                await apiWrapper.put(`/products/updateStock`, {
                    productId: id,
                    type: type,
                    amount: amount
                });
                await this.fetchProducts();
            } catch (error) {
                console.error("更新庫存失敗", error)
            }
        },


        async deleteProduct(id) {

            try {
                await apiWrapper.delete(`/products/${id}`);

                const index = this.products.findIndex(p => String(p.id) === String(id));
                if (index !== -1) {
                    this.products.splice(index, 1);
                    console.log(`商品ID ${id}已成功從資料庫與前端刪除`)
                }
            } catch (error) {
                console.error("刪除失敗", error)
                throw error;
            }


        }
    }

})