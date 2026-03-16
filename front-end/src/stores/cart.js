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

        async fetchCart() {
            try {
                const response = await cartAPI.getAll();
                this.items = response || [];
                console.log('購物車同步成功', this.items)
            } catch (error) {
                console.error('獲取購物車失敗', error)
                this.items = [];
            }
            console.log('fetchCart 完成，目前購物車：', this.items)

        },

        //加入購物車 對應/api/cart/add
        async addToCart(product) {

            const existing = this.items.find(i => String(i.productId) === String(product.id))
            const alreadyInCart = existing ? existing.quantity : 0
            console.log('購物車已有：', alreadyInCart, '庫存：', product.stock)


            if (product.quantity > 0 && alreadyInCart + product.quantity > product.stock) {
                Swal.fire({
                    icon: 'warning',
                    title: '庫存不足',
                    text: `購物車已有 ${alreadyInCart} 件，庫存僅剩 ${product.stock} 件，無法再加入`,
                    confirmButtonColor: '#9f9572'
                })
                return;
            }


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
            if (item) {
                // 檢查庫存上限
                if (item.quantity >= item.stock) {
                    Swal.fire({
                        icon: 'warning',
                        title: '已達庫存上限',
                        text: `此商品僅剩 ${item.stock} 件`,
                        timer: 1500,
                        showConfirmButton: false
                    })
                    return
                }
                await this.addToCart({
                    id: item.productId,
                    quantity: 1,
                    stock: item.stock
                }) // 傳入庫存資訊以供檢查
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

        async updateQuantityToStock(productName, remainStock) {
            try {
                await cartAPI.sync()       // 呼叫後端同步庫存
                await this.fetchCart()     // 重新載入購物車
            } catch (error) {
                console.error('同步庫存失敗', error)
            }
        },


        clearCart() {
            this.items = [];
        }

    }
})




