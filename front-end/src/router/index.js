import { createRouter, createWebHistory } from 'vue-router';



// 定義router
const routes = [
  // ===== 前台路由：有 Navbar + Footer (via default.vue layout) =====
  {
    path: '/',
    component: () => import('@/layouts/default.vue'),
    children: [
      {
        path: '',
        name: 'Home',
        component: () => import('@/views/HomeView.vue'),
        meta: { title: '饗島' }
      },
      {
        path: 'login',
        name: 'Login',
        component: () => import('@/views/LoginView.vue'),
      },
      {
        path: 'userInfo',
        name: 'UserInfo',
        component: () => import('@/views/UserInfoView.vue'),
        meta: { requiresAuth: true },
        redirect: '/userInfo/profile',
        children: [
          {
            path: 'profile',
            name: 'UserProfile',
            component: () => import('@/views/UserInfoContent.vue'),
            props: { title: '個人資料' },
            meta: { title: '個人資料' }
          },
          {
            path: 'bookings',
            name: 'UserBookingsTab',
            component: () => import('@/views/UserBookingsView.vue')
          },
          {
            path: 'orders',
            name: 'UserOrders',
            component: () => import('@/views/UserInfoOrders.vue'),
            meta: { title: '我的訂單' }
          },
          {
            path: 'store-registration',
            name: 'UserInfoStoreReg',
            component: () => import('@/views/UserInfoStoreReg.vue'),
            meta: { title: '我的訊息' }
          },
          {
            path: 'feedback',
            name: 'UserInfoFeedback',
            component: () => import('@/views/UserInfoFeedbackView.vue'),
            meta: { title: '我的回饋' }
          }
        ]
      },
      {
        path: '/storeRegistration',
        name: 'StoreRegistration',
        component: () => import('@/views/StoreRegistrationView.vue'),
        meta: { requiresAuth: true, hideFromStore: true }
      },
      {
        path: '/shopStore',
        component: () => import('@/views/ShopStore.vue'),
        name: 'ShopStore',
        meta: { title: '質感選物' }
      },
      {
        path: '/Cart',
        component: () => import('@/views/ShopCart.vue'),
        name: 'ShopCart',
        // meta: { requiresAuth: true }
        meta: { title: '購物車' }
      },
      {
        path: '/storeInfo/reservation/:id',
        name: 'Reservation',
        component: () => import('@/views/ReservationView.vue'),
        meta: { title: '預約座位' }
      },
      {
        path: '/storeInfo/:id?',
        name: 'StoreInfo',
        component: () => import('@/views/StoreInfoView.vue'),
        meta: { title: '商家資訊' }
      },
      {
        path: '/owner/storeInfo',
        name: 'OwnerStoreInfo',
        component: () => import('@/views/OwnerProfileView.vue'),
        meta: { requiresAuth: true, requiresStore: true, title: '商家資訊編輯' }
      },
      {
        path: '/owner/bookings/seats',
        name: 'Seats',
        component: () => import('@/views/SeatsAndTimeView.vue'),
        meta: { requiresAuth: true, requiresStore: true, title: '座位與時間' }
      },
      {
        path: '/owner/bookings/data',
        name: 'Data',
        component: () => import('@/views/BookingDataView.vue'),
        meta: { requiresAuth: true, requiresStore: true, title: '訂位紀錄' }
      },
      // {
      //   path: '/home',
      //   component: () => import('@/views/HomeView.vue'),
      //   name: 'Home'
      // },
      {
        path: '/about',
        component: () => import('@/views/AboutView.vue'),
        name: 'About',
        meta: { title: '島嶼初心' }
      },
      {
        path: '/mapSearch',
        component: () => import('@/views/MapSearchView.vue'),
        name: 'MapSearch',
        meta: { title: '美味座標' }
      },
      {
        path: '/feedback',
        component: () => import('@/views/FeedbackView.vue'),
        name: 'Feedback',
        meta: { title: '聯絡我們' }
      },
      // {
      //   path: 'feedbackAP',
      //   component: () => import('@/views/FeedbackAPView.vue'),
      //   name: 'FeedbackAP',
      //   meta: { title: '客服管理後台' }
      // },
      {
        path: 'productsDetail/:id',
        component: () => import('@/views/ProductsDetail.vue'),
        name: 'productsDetail',
        meta: { title: '商品介紹' }

      },
      {
        path: 'checkOut',
        component: () => import('@/views/Checkout.vue'),
        name: 'checkOut',
        meta: { title: '結帳' }
      },
      {
        path: 'getusertest',
        name: 'GetUserTest',
        component: () => import('@/views/GetUserTestView.vue')
      },
      {
        path: '/payment-result',
        component: () => import('@/views/PaymentResult.vue'),
        name: 'PaymentResult',
        meta: { title: '結帳結果' }
      }
    ]
  },

  // ===== Blank Layout 路由：無 Navbar / Footer =====
  {
    path: '/forgot-password',
    name: 'ForgetPassword',
    component: () => import('@/views/ForgetPasswordView.vue'),
  },
  {
    path: '/reset-password',
    name: 'ResetPassword',
    component: () => import('@/views/ResetPasswordView.vue'),
  },
  {
    path: '/oauth2/redirect',
    name: 'OAuth2Redirect',
    component: () => import('@/views/OAuth2RedirectHandler.vue'),
  },
  {
    path: '/register',
    component: () => import('@/views/RegisterView.vue'),
    name: 'Register',
  },

  // ===== Admin 路由：使用 admin.vue layout，無 Navbar / Footer =====
  {
    path: '/admin',
    component: () => import('@/layouts/admin.vue'),
    meta: { requiresAdminAuth: true },
    children: [
      // {
      //   path: '',
      //   name: 'AdminDashboard',
      //   component: () => import('@/views/ERPTransferView.vue')
      // },
      // {
      //   path: 'frontend/banners',
      //   name: 'AdminFrontendBanners',
      //   component: () => import('@/views/AdminDashboard.vue')
      // },
      // {
      //   path: 'frontend/content',
      //   name: 'AdminFrontendContent',
      //   component: () => import('@/views/AdminDashboard.vue')
      // },
      // {
      //   path: 'news/list',
      //   name: 'AdminNewsList',
      //   component: () => import('@/views/AdminDashboard.vue')
      // },
      // {
      //   path: 'news/create',
      //   name: 'AdminNewsCreate',
      //   component: () => import('@/views/AdminDashboard.vue')
      // },
      // {
      //   path: 'knowledge/list',
      //   name: 'AdminKnowledgeList',
      //   component: () => import('@/views/AdminDashboard.vue')
      // },
      // {
      //   path: 'knowledge/categories',
      //   name: 'AdminKnowledgeCategories',
      //   component: () => import('@/views/AdminDashboard.vue')
      // },
      // {
      //   path: 'products/list',
      //   name: 'AdminProductsList',
      //   component: () => import('@/views/AdminDashboard.vue')
      // },
      // {
      //   path: 'products/create',
      //   name: 'AdminProductsCreate',
      //   component: () => import('@/views/AdminDashboard.vue')
      // },
      // {
      //   path: 'products/categories',
      //   name: 'AdminProductsCategories',
      //   component: () => import('@/views/AdminDashboard.vue')
      // },
      // {
      //   path: 'sales/orders',
      //   name: 'AdminSalesOrders',
      //   component: () => import('@/views/AdminDashboard.vue')
      // },
      // {
      //   path: 'sales/reports',
      //   name: 'AdminSalesReports',
      //   component: () => import('@/views/AdminDashboard.vue')
      // },
      {
        path: 'users/list',
        name: 'AdminUsersList',
        component: () => import('@/views/UserListView.vue'),
        meta: { roles: ['SUPER_ADMIN', 'CUSTOMER_SERVICE'], title: '使用者列表' }
      },
      {
        path: 'users/storeRegistration',
        name: 'StoreRegistrationCheck',
        component: () => import('@/views/StoreRegistrationCheckView.vue'),
        meta: { roles: ['SUPER_ADMIN', 'CUSTOMER_SERVICE'], title: '店家註冊審核' }
      },
      {
        path: 'backEnd/productsList',
        name: 'BackEndProductsList',
        component: () => import('@/views/BackEndProductsList.vue'),
        meta: { roles: ['SUPER_ADMIN', 'SHOP_MANAGER'] },
        meta: { title: '管理 | 商品&庫存' }
      },
      {
        path: 'backEnd/productsOrders',
        name: 'BackEndproductsOrders',
        component: () => import('@/views/BackEndproductsOrders.vue'),
        meta: { roles: ['SUPER_ADMIN', 'SHOP_MANAGER'] },
        meta: { title: '管理 | 訂單紀錄' }
      },
      {
        path: 'feedbackAP',
        name: 'FeedbackAP',
        component: () => import('@/views/FeedbackAPView.vue'),
        meta: { roles: ['SUPER_ADMIN', 'CUSTOMER_SERVICE'], title: '客服管理後台' }
      },
      {
        path: 'admins/list',
        name: 'AdminsList',
        component: () => import('@/views/AdminListView.vue'),
        meta: {
          requiresAdminAuth: true,
          roles: ['SUPER_ADMIN', 'HUMAN_RESOURCE'],
          title: '管理員列表'
        }
      }
    ]
  },
  {
    path: '/admin/login',
    name: 'AdminLogin',
    component: () => import('@/views/AdminLoginView.vue'),
  },
  {
    path: '/admin/forgot-password',
    name: 'AdminForgetPassword',
    component: () => import('@/views/AdminForgetPasswordView.vue'),
  },
  {
    path: '/admin/reset-password',
    name: 'AdminResetPassword',
    component: () => import('@/views/AdminResetPasswordView.vue'),
  },

];

// 建立router
const router = createRouter({
  history: createWebHistory(),
  routes: routes,
  scrollBehavior(to, from, savedPosition) {
    // 始終滾動到頂部
    return { top: 0 }
  }
});



import { useAuthStore } from '@/stores/auth';
import { useAdminAuthStore } from '@/stores/adminAuth';
import Swal from 'sweetalert2';
import { useCartStore } from '@/stores/cart';

// 路由守衛
router.beforeEach(async (to, from, next) => {
  const authStore = useAuthStore();
  const adminAuthStore = useAdminAuthStore();
  document.title = to.meta.title || '饗島';


  //for登出入狀態攔截，顯示購物車
  const cartStore = useCartStore();
  if (!authStore.isLoggedIn && cartStore.items.length > 0) {
    cartStore.clearCart();
  }
  if (from.path === '/login' && authStore.isLoggedIn) {
    cartStore.fetchCart();
  }


  if (to.meta.requiresAdminAuth) {
    let wasAdminLoggedIn = localStorage.getItem('isAdminLoggedIn') === 'true';

    console.log(`[Router Admin] 導航到: ${to.path}`);
    console.log(`[Router Admin] wasAdminLoggedIn: ${wasAdminLoggedIn} | isLoggedIn: ${adminAuthStore.isLoggedIn} | admin.position: ${adminAuthStore.admin?.position ?? 'null'}`);

    if (!adminAuthStore.isLoggedIn && wasAdminLoggedIn) {
      console.log('[Router Admin] admin=null 且有登入旗標 → 呼叫 syncAdminProfile()');
      await adminAuthStore.syncAdminProfile();
      console.log(`[Router Admin] syncAdminProfile 完成 → isLoggedIn: ${adminAuthStore.isLoggedIn} | position: ${adminAuthStore.admin?.position ?? 'null'}`);
    }

    if (!adminAuthStore.isLoggedIn) {
      console.warn('[Router Admin] 仍未登入 → 跳至 /admin/login');
      // [2026/03/06 修正] 為了不干擾 config.js 的「登入逾期」彈窗，這裡只針對「完全未登入」的狀態彈出提示
      if (!wasAdminLoggedIn) {
        Swal.fire({
          icon: 'warning',
          title: '請先登入',
          text: '您尚未登入，該頁面需要登入才能訪問，請先登入',
          confirmButtonColor: '#1e3c72'
        });
      }
      return next('/admin/login');
    }

    // Role-based authorization
    if (to.meta.roles && to.meta.roles.length > 0) {
      console.log(`[Router Admin] Role check: 需要 ${JSON.stringify(to.meta.roles)} | 目前 position: ${adminAuthStore.admin?.position ?? 'null'}`);
      if (!adminAuthStore.hasAnyRole(to.meta.roles)) {
        console.warn(`[Router Admin] Role check 失敗！position=${adminAuthStore.admin?.position} 不符合 ${JSON.stringify(to.meta.roles)}`);
        const Swal = (await import('sweetalert2')).default;
        await Swal.fire({
          icon: 'error',
          title: '權限不足',
          text: '您沒有訪問此頁面的權限',
          confirmButtonColor: '#1e3c72'
        });

        const position = adminAuthStore.admin?.position;
        let redirectTo = '/admin';
        if (position === 'SUPER_ADMIN' || position === 'HUMAN_RESOURCE') {
          redirectTo = '/admin/admins/list';
        } else if (position === 'CUSTOMER_SERVICE') {
          redirectTo = '/admin/feedbackAP';
        } else if (position === 'SHOP_MANAGER') {
          redirectTo = '/admin/backEnd/productsList';
        }

        return next(redirectTo); // 依據職位導向對應首頁
      }
      console.log('[Router Admin] Role check 通過 ✅');
    }
  }






  if (to.meta.requiresAuth) {
    // 同理，等待一般使用者狀態同步
    let wasUserLoggedIn = localStorage.getItem('isUserLoggedIn') === 'true';
    if (!authStore.isLoggedIn && wasUserLoggedIn) {
      await authStore.syncUserProfile();
    }

    if (!authStore.isLoggedIn) {
      // 交由 config.js 負責提示登入逾期，這裡只管完全未登入的情境
      if (!wasUserLoggedIn) {
        Swal.fire({
          icon: 'warning',
          title: '請先登入',
          text: '您尚未登入，該頁面需要登入才能訪問，請先登入',
          confirmButtonColor: '#9f9572'
        });
      }
      return next('/login');
    }

    // Role-based restrictions for general users
    if (to.meta.hideFromStore && authStore.isStoreUser && !to.query.id) {
      Swal.fire({
        icon: 'info',
        title: '您已是商家',
        text: '您已經具備商家身分，無需重複申請。',
        confirmButtonColor: '#9f9572'
      });
      return next('/owner/storeInfo');
    }

    if (to.meta.requiresStore && !authStore.isStoreUser) {
      Swal.fire({
        icon: 'error',
        title: '權限不足',
        text: '您不是商家，沒有權限訪問此頁面。',
        confirmButtonColor: '#9f9572'
      });
      return next('/');
    }
  }
  next();
});

router.afterEach((to) => {
  if (to.meta.title) {
    document.title = to.meta.title;
  } else {
    document.title = 'ViteApp';
  }
});

export default router;
