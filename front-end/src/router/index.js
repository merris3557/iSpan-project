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
            props: { title: '個人資料' }
          },
          {
            path: 'bookings',
            name: 'UserBookingsTab',
            component: () => import('@/views/UserBookingsView.vue')
          },
          {
            path: 'orders',
            name: 'UserOrders',
            component: () => import('@/views/UserInfoContent.vue'),
            props: { title: '我的訂單' }
          },
          {
            path: 'store-registration',
            name: 'UserInfoStoreReg',
            component: () => import('@/views/UserInfoStoreReg.vue')
          },
          {
            path: 'feedback',
            name: 'UserInfoFeedback',
            component: () => import('@/views/UserInfoFeedback.vue')
          }
        ]
      },
      {
        path: '/storeRegistration',
        name: 'StoreRegistration',
        component: () => import('@/views/StoreRegistrationView.vue'),
        meta: { requiresAuth: true }
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
        path: '/storeInfo/reservation/:id',
        name: 'Reservation',
        component: () => import('@/views/ReservationView.vue'),
      },
      {
        path: '/storeInfo/:id?',
        name: 'StoreInfo',
        component: () => import('@/views/StoreInfoView.vue'),
      },
      {
        path: '/owner/storeInfo',
        name: 'OwnerStoreInfo',
        component: () => import('@/views/OwnerProfileView.vue'),
        meta: { requiresAuth: true }
      },
      {
        path: '/owner/bookings/seats',
        name: 'Seats',
        component: () => import('@/views/SeatsAndTimeView.vue'),
        meta: { requiresAuth: true }
      },
      {
        path: '/owner/bookings/data',
        name: 'Data',
        component: () => import('@/views/BookingDataView.vue'),
        meta: { requiresAuth: true }
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
        path: 'feedbackAP',
        component: () => import('@/views/FeedbackAPView.vue'),
        name: 'FeedbackAP'
      },
      {
        path: 'productsDetail/:id',
        component: () => import('@/views/productsDetail.vue'),
        name: 'productsDetail',
      },
      {
        path: 'checkOut',
        component: () => import('@/views/checkOut.vue'),
        name: 'checkOut',
      },
      {
        path: 'getusertest',
        name: 'GetUserTest',
        component: () => import('@/views/GetUserTestView.vue')
      },
      {
        path: 'test',
        name: 'Test',
        component: () => import('@/views/TestView.vue')
      },
      {
        path: '/payment-result',
        component: () => import('@/views/PaymentResult.vue'),
        name: 'PaymentResult'
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
      {
        path: '',
        name: 'AdminDashboard',
        component: () => import('@/views/ERPTransferView.vue')
      },
      {
        path: 'frontend/banners',
        name: 'AdminFrontendBanners',
        component: () => import('@/views/AdminDashboard.vue')
      },
      {
        path: 'frontend/content',
        name: 'AdminFrontendContent',
        component: () => import('@/views/AdminDashboard.vue')
      },
      {
        path: 'news/list',
        name: 'AdminNewsList',
        component: () => import('@/views/AdminDashboard.vue')
      },
      {
        path: 'news/create',
        name: 'AdminNewsCreate',
        component: () => import('@/views/AdminDashboard.vue')
      },
      {
        path: 'knowledge/list',
        name: 'AdminKnowledgeList',
        component: () => import('@/views/AdminDashboard.vue')
      },
      {
        path: 'knowledge/categories',
        name: 'AdminKnowledgeCategories',
        component: () => import('@/views/AdminDashboard.vue')
      },
      {
        path: 'products/list',
        name: 'AdminProductsList',
        component: () => import('@/views/AdminDashboard.vue')
      },
      {
        path: 'products/create',
        name: 'AdminProductsCreate',
        component: () => import('@/views/AdminDashboard.vue')
      },
      {
        path: 'products/categories',
        name: 'AdminProductsCategories',
        component: () => import('@/views/AdminDashboard.vue')
      },
      {
        path: 'sales/orders',
        name: 'AdminSalesOrders',
        component: () => import('@/views/AdminDashboard.vue')
      },
      {
        path: 'sales/reports',
        name: 'AdminSalesReports',
        component: () => import('@/views/AdminDashboard.vue')
      },
      {
        path: 'users/list',
        name: 'AdminUsersList',
        component: () => import('@/views/UserListView.vue'),
        meta: { roles: ['SUPER_ADMIN', 'CUSTOMER_SERVICE'] }
      },
      {
        path: 'users/storeRegistration',
        name: 'StoreRegistrationCheck',
        component: () => import('@/views/StoreRegistrationCheckView.vue')
      },
      {
        path: 'backEnd/productsList',
        name: 'BackEndProductsList',
        component: () => import('@/views/BackEndProductsList.vue')
      },
      {
        path: 'backEnd/productsOrders',
        name: 'BackEndproductsOrders',
        component: () => import('@/views/BackEndproductsOrders.vue')
      },
      {
        path: 'feedbackAP',
        name: 'FeedbackAP',
        component: () => import('@/views/FeedbackAPView.vue')
      },
      {
        path: 'admins/list',
        name: 'AdminsList',
        component: () => import('@/views/AdminListView.vue'),
        meta: { requiresAdminAuth: false }
        // meta: {
        //   requiresAdminAuth: true,
        //   roles: ['SUPER_ADMIN']
        // }
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
      // [2026/03/06 修正] 交由 config.js 負責提示「登入逾期」與強制跳轉
      // 這裡如果彈窗會干擾背後的 silent refresh 流程，所以只需擋下路由即可。
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
        return next('/admin'); // 導向預設的 dashboard
      }
      console.log('[Router Admin] Role check 通過 ✅');
    }
  }




  //   //Anna改的，為了將目標登入後保留原始路徑返回前頁
  //   if (to.meta.requiresAuth) {
  //     if (authStore.isExpired || !authStore.isLoggedIn) {
  //       sessionStorage.setItem('redirectPath', to.fullPath);        //將當前目標路徑存入瀏覽器站存


  //       await authStore.handleLogoutAndNotify(authStore.isExpired ? 'timeout' : 'unauthorized');
  //       return next({ name: 'Login', query: { redirect: to.fullPath } });
  //     }
  //   }

  //   //當使用者從/login登入成功切換出來時，自動攔截並導向
  //   if (from.path === '/login' && authStore.isLoggedIn) {
  //     const savedPath = sessionStorage.getItem('redirectPath');
  //     if (savedPath) {
  //       sessionStorage.removeItem('redirectPath');
  //       return next({ name: 'Login', query: { redirect: to.fullPath } });
  //     }
  //   }
  //   next();

  // });


  if (to.meta.requiresAuth) {
    // 同理，等待一般使用者狀態同步
    let wasUserLoggedIn = localStorage.getItem('isUserLoggedIn') === 'true';
    if (!authStore.isLoggedIn && wasUserLoggedIn) {
      await authStore.syncUserProfile();
    }

    if (!authStore.isLoggedIn) {
      // 交由 config.js 負責提示與強制登出跳轉，這裡只管擋下未登入路由
      return next('/login');
    }
  }
  next();
});

export default router;
