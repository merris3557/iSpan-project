import { createRouter, createWebHistory } from 'vue-router';



// 定義router
const routes = [
  {
    path: '/',
    name: 'Home',
    component: () => import('@/views/HomeView.vue'),
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/LoginView.vue'),
  },
  {
    path: '/forgot-password',
    name: 'ForgetPassword',
    component: () => import('@/views/ForgetPasswordView.vue'),
    meta: { layout: 'blank' }
  },
  {
    path: '/reset-password',
    name: 'ResetPassword',
    component: () => import('@/views/ResetPasswordView.vue'),
    meta: { layout: 'blank' }
  },
  {
    path: '/userInfo',
    name: 'UserInfo',
    component: () => import('@/views/UserInfoView.vue'),
  },
  {
    path: '/storeRegistration',
    name: 'StoreRegistration',
    component: () => import('@/views/StoreRegistrationView.vue'),
  },
  {
    path: '/shopStore',
    component: () => import('@/views/shopStore.vue'),
    name: 'ShopStore'
  },
  {
    path: '/Cart',
    component: () => import('@/views/shopCart.vue'),
    name: 'ShopCart'
  },
  {
    path: '/storeInfo',
    name: 'StoreInfo',
    component: () => import('@/views/StoreInfoView.vue'),
  },
  {
    path: '/storeInfo/reservation',
    name: 'Reservation',
    component: () => import('@/views/ReservationView.vue'),
  },
  {
    path: '/user/bookings',
    name: 'UserBookings',
    component: () => import('@/views/UserBookingsView.vue')
  },
  {
    path: '/owner/storeInfo',
    name: 'OwnerStoreInfo',
    component: () => import('@/views/OwnerProfileView.vue')
  },
  {
    path: '/owner/bookings/seats',
    name: 'Seats',
    component: () => import('@/views/SeatsAndTimeView.vue')
  },
  {
    path: '/owner/bookings/data',
    name: 'Data',
    component: () => import('@/views/BookingDataView.vue')
  },
  // {
  //   path: '/home',
  //   component: () => import('@/views/HomeView.vue'),
  //   name: 'Home'
  // },
  {
    path: '/about',
    component: () => import('@/views/AboutView.vue'),
    name: 'About'
  },
  {
    path: '/mapSearch',
    component: () => import('@/views/MapSearchView.vue'),
    name: 'MapSearch'
  },
  {
    path: '/feedback',
    component: () => import('@/views/FeedbackView.vue'),
    name: 'Feedback'
  },
  {
    path: '/feedbackAP',
    component: () => import('@/views/FeedbackAPView.vue'),
    name: 'FeedbackAP'
  },
  {
    path: '/register',
    component: () => import('@/views/RegisterView.vue'),
    name: 'Register',
    meta: { layout: 'blank' }
  },
  {
    path: '/productsDetail/:id',
    component: () => import('@/views/productsDetail.vue'),
    name: 'productsDetail',
  },
  {
    path: '/checkOut',
    component: () => import('@/views/checkOut.vue'),
    name: 'checkOut',
  },
  // Admin Routes
  {
    path: '/admin',
    component: () => import('@/layouts/admin.vue'),
    children: [
      {
        path: '',
        name: 'AdminDashboard',
        component: () => import('@/views/ERPTransferView.vue')
      },
      {
        path: 'frontend/banners',
        name: 'AdminFrontendBanners',
        component: () => import('@/views/AdminDashboard.vue') // placeholder
      },
      {
        path: 'frontend/content',
        name: 'AdminFrontendContent',
        component: () => import('@/views/AdminDashboard.vue') // placeholder
      },
      {
        path: 'news/list',
        name: 'AdminNewsList',
        component: () => import('@/views/AdminDashboard.vue') // placeholder
      },
      {
        path: 'news/create',
        name: 'AdminNewsCreate',
        component: () => import('@/views/AdminDashboard.vue') // placeholder
      },
      {
        path: 'knowledge/list',
        name: 'AdminKnowledgeList',
        component: () => import('@/views/AdminDashboard.vue') // placeholder
      },
      {
        path: 'knowledge/categories',
        name: 'AdminKnowledgeCategories',
        component: () => import('@/views/AdminDashboard.vue') // placeholder
      },
      {
        path: 'products/list',
        name: 'AdminProductsList',
        component: () => import('@/views/AdminDashboard.vue') // placeholder
      },
      {
        path: 'products/create',
        name: 'AdminProductsCreate',
        component: () => import('@/views/AdminDashboard.vue') // placeholder
      },
      {
        path: 'products/categories',
        name: 'AdminProductsCategories',
        component: () => import('@/views/AdminDashboard.vue') // placeholder
      },
      {
        path: 'sales/orders',
        name: 'AdminSalesOrders',
        component: () => import('@/views/AdminDashboard.vue') // placeholder
      },
      {
        path: 'sales/reports',
        name: 'AdminSalesReports',
        component: () => import('@/views/AdminDashboard.vue') // placeholder
      },
      {
        path: 'users/list',
        name: 'AdminUsersList',
        component: () => import('@/views/UserListView.vue')
      },
      {
        path: 'users/storeRegistration',
        name: 'StoreRegistrationCheck',
        component: () => import('@/views/StoreRegistrationCheckView.vue')
      }
      ,
      {
        path: 'backEnd/productsList',
        name: 'BackEndProductsList',
        component: () => import('@/views/BackEndProductsList.vue') // placeholder
      },
      {
        path: 'backEnd/productsStock',
        name: 'BackEndProductsStock',
        component: () => import('@/views/BackEndproductsStock.vue') // placeholder
      },
    ]
  },
  {
    path: '/getusertest',
    name: 'GetUserTest',
    component: () => import('@/views/GetUserTestView.vue')
  }

];

// 建立router
const router = createRouter({
  history: createWebHistory(),
  routes: routes
});



export default router;
