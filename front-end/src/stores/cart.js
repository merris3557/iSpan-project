import { defineStore } from 'pinia'
import Swal from 'sweetalert2'
import cartAPI from '../api/cart'

export const useCartStore = defineStore('cart', {
    state: () => ({
        // 從 localStorage 讀取，若無則為空陣列
        items: [] //存放購物車內容(DTO)
    }),

    getters: {
        totalQuantity(state) {
            return state.items.reduce((sum, item) => sum + item.quantity, 0)
        },
        totalPrice(state) {
            return state.items.reduce((sum, item) => sum + item.price * item.quantity, 0)
        }
    },

    actions: {
        // 所有的 function 必須都在 actions 的這個大括號裡面！

        async fetchCart() {
            try {
                const response = await cartAPI.getAll();
                this.items = response || [];
                console.log('購物車同步成功', this.items)
            } catch (error) {
                console.error('獲取購物車失敗', error)
                this.items = [];
            }
        },

        //加入購物車 對應/api/cart/add
        async addToCart(product) {

            try {
                await cartAPI.add({
                    productId: product.id,
                    quantity: product.quantity || 1
                })
                await this.fetchCart(); //加入商品後自動更新購物車
            } catch (error) {
                console.error('加入購物車失敗', error)
                throw error
            }
        },


        async increase(cartDetailsId) {
            const item = this.items.find(i => i.id === cartDetailsId)

            // 注意：確保傳入的 item 物件裡有 stock 屬性
            if (item) {
                await this.addToCart({ id: item.productId, quantity: 1 })
            }
        },


        async decrease(cartDetailsId) {
            const item = this.items.find(i => i.id === cartDetailsId)
            if (item) {
                if (item.quantity > 1) {
                    await this.addToCart({ id: item.productId, quantity: -1 })
                } else {
                    await this.remove(cartDetailsId)
                }
            }
        },



        async remove(cartDetailsId) {

            try {
                await cartAPI.delete(cartDetailsId)

                const index = this.items.findIndex(i => i.id === cartDetailsId)
                if (index !== -1) {
                    this.items.splice(index, 1);
                    console.log(`商品ID ${cartDetailsId}已成功從資料庫與前端刪除`)
                }
            } catch (error) {
                console.error("刪除失敗", error)
                Swal.fire('錯誤', '無法移除商品', 'error')
            }
        },

        clearCart() {
            this.items = [];
        }

    }
})





// import { defineStore } from 'pinia';
// import axios from 'axios'; // 💡 確保有這行

// export const useCartStore = defineStore('cart', {
//     state: () => ({
//         items: []
//     }),

//     getters: {
//         totalPrice: (state) => {
//             return state.items.reduce((total, item) => total + (item.price * item.quantity), 0);
//         }
//     },

//     actions: {
//         // 取得購物車清單
//         async fetchCart() {
//             try {
//                 const response = await axios.get('http://localhost:8080/api/cart/all');
//                 // 💡 這裡要把後端的 DTO 欄位轉為前端習慣的欄位
//                 this.items = response.data.map(item => ({
//                     id: item.id,               // 購物車明細 ID
//                     productId: item.productId, // 商品 ID
//                     name: item.productName,    // 💡 對應後端 CartDTO 的 productName
//                     price: item.price,
//                     quantity: item.quantity,
//                     image: item.image          // 💡 對應後端 CartDTO 的 image
//                 }));
//             } catch (error) {
//                 console.error("抓取購物車失敗", error);
//             }
//         },

//         // 加入購物車 (一次修好 400 錯誤)
//         async addToCart(productId, quantity) {
//             try {
//                 const payload = {
//                     productId: Number(productId),
//                     quantity: Number(quantity)
//                 };

//                 const response = await axios.post('http://localhost:8080/api/cart/add', payload, {
//                     withCredentials: true // 💡 確保瀏覽器會自動帶上 Session Cookie
//                 });



//                 await this.fetchCart();
//                 return response.data;
//             } catch (error) {
//                 // ...
//             }
//         },

//         // 清空購物車
//         clearCart() {
//             this.items = [];
//         }
//     }
// });
