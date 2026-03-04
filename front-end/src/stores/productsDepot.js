import { defineStore } from 'pinia';
import axios from 'axios';

export const useProductsDepot = defineStore('productDepot', {
    state: () => ({
        products: []
    }),

    actions: {
        //更新庫存統一邏輯
        async fetchProducts() {

            try {
                const response = await axios.get(`http://localhost:8080/api/products/all`);
                console.log("API Response:", response.data)


                this.products = response.data.map(p => ({
                    id: p.productId,
                    productName: p.productName,
                    price: p.price,
                    description: p.productDescription,
                    image: p.image,
                    stock: p.stock ? p.stock.availableQuantity : 0,
                    added: p.added,
                    lastUpdate: p.stock ? p.stock.updateAt : null
                }));
            } catch (error) {
                console.error("獲取資料失敗", error);
            }
        },

        async updateStock(id, type, amount) {

            try {
                await axios.get(`http://localhost:8080/api/products/updateStock`, {
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
                await axios.delete(`http://localhost:8080/api/products/${id}`);

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