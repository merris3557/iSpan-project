import api from '../api/config';


export const cartAPI = {

    getAll() {
        return api.get('/cart/all');
    },




    add(data) {
        return api.post('/cart/add', data);
    },


    delete(cartDetailsId) {
        return api.delete(`/cart/${cartDetailsId}`);
    },


    checkStock() {
        return api.get('/cart/check-stock')
    },

    sync() {
        return api.post('/cart/sync');
    },

};

export default cartAPI;