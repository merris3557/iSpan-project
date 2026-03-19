<script setup>
import { useRouter } from 'vue-router';

const router = useRouter();

const goTo = (path) => {
  router.push(path);
};
</script>

<template>
  <footer class="footer-gdg mt-auto py-5">
    <div class="container">
      <div class="row">
        <!-- Social / Contact (Left 1/3) -->
        <div class="col-md-4 mb-4 mb-md-0 d-flex flex-column align-items-start">
          <h5 class="text-uppercase mb-3 fw-bold text-gdg">聯絡我們</h5>
          <p class="small text-muted mb-4">
            我們重視您的意見，歡迎隨時與我們聯繫。
          </p>
          <a class="btn btn-gdg d-flex align-items-center gap-2" href="#" @click.prevent="goTo('/feedback')">
            <i class="bi bi-chat-dots-fill"></i>
            <span>意見回饋</span>
          </a>
        </div>

        <!-- Links (Right 2/3) -->
        <div class="col-md-8">
          <div class="row">
            <!-- 關於饗島 -->
            <div class="col-6 col-md-3 mb-3">
              <h6 class="text-uppercase mb-3 fw-bold text-gdg">關於饗島</h6>
              <ul class="list-unstyled">
                <li class="mb-2"><a href="#" @click.prevent="goTo('/about')" class="footer-link">島嶼初心</a></li>
                <li class="mb-2"><span class="footer-link" style="cursor: default;">ESG永續經營</span></li>
                <li class="mb-2"><span class="footer-link" style="cursor: default;">人才招募</span></li>
              </ul>
            </div>

            <!-- 餐廳搜尋 -->
            <div class="col-6 col-md-3 mb-3">
              <h6 class="text-uppercase mb-3 fw-bold text-gdg">餐廳搜尋</h6>
              <ul class="list-unstyled">
                <li class="mb-2"><a href="#" @click.prevent="goTo('/mapSearch')" class="footer-link">美味座標</a></li>
                <li class="mb-2"><span class="footer-link" style="cursor: default;">餐廳訂位</span></li>
              </ul>
            </div>

            <!-- 購物商城 -->
            <div class="col-6 col-md-3 mb-3">
              <h6 class="text-uppercase mb-3 fw-bold text-gdg">購物商城</h6>
              <ul class="list-unstyled">
                <li class="mb-2"><a href="#" @click.prevent="goTo('/shopStore')" class="footer-link">質感選物</a></li>
                <li class="mb-2"><span class="footer-link" style="cursor: default;">購物須知</span></li>
                <li class="mb-2"><span class="footer-link" style="cursor: default;">退貨處理</span></li>
              </ul>
            </div>

            <!-- 會員中心 -->
            <div class="col-6 col-md-3 mb-3">
              <h6 class="text-uppercase mb-3 fw-bold text-gdg">會員中心</h6>
              <ul class="list-unstyled">
                <li class="mb-2"><a href="#" @click.prevent="goTo('/login')" class="footer-link">會員登入</a></li>
                <li class="mb-2"><a href="#" @click.prevent="goTo('/register')" class="footer-link">會員註冊</a></li>
                <li class="mb-2"><span class="footer-link" style="cursor: default;">會員權益</span></li>
                <li class="mb-2"><span class="footer-link" style="cursor: default;">隱私權政策</span></li>
              </ul>
            </div>
          </div>
        </div>
      </div>
      
      <div class="row mt-4 border-top border-secondary pt-3">
        <div class="col-12 text-center small text-muted">
          &copy; 2026 iSpan Project. All Rights Reserved.
        </div>
      </div>
    </div>
  </footer>
</template>

<style scoped>
/* Scoped styles if needed, but primarily using custom.scss */
</style>
