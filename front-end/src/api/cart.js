import api from '../api/config';


export const cartAPI = {
    //取得購物車所有項目

    getAll() {
        return api.get('/cart/all');
    },


    /**
     * 加入商品到購物車
     * @param {Object} data  - { productId, quantity }
     */


    add(data) {
        return api.post('/cart/add', data);
    },

    //刪除購物車項目
    delete(cartDetailsId) {
        return api.delete(`/cart/${cartDetailsId}`);
    }

};

export default cartAPI;