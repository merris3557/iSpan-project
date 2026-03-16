<script setup>
import BaseButton from '@/components/common/BaseButton.vue';

// Import images
import bannerImage from '@/pictures/bannerBrown.jpg';
</script>

<template>
    <div class="home-view-gdg">
        <!-- Hero Section -->
        <section class="hero-section-gdg">
            <div class="hero-overlay-gdg">
                <div class="hero-content-gdg">
                    <h1 class="hero-title-gdg">饗島</h1>
                    <div class="hero-divider-gdg"></div>
                    <p class="hero-subtitle-gdg">島嶼時光，舌尖上的帶路人</p>
                    <p class="hero-description-gdg">
                        從探索、入座到珍藏<br>
                        在這座寶島尋覓每一處值得品味的美好
                    </p>
                </div>
            </div>
        </section>

        <!-- Philosophy Section
    <section class="philosophy-section-gdg">
      <div class="container">
        <div class="section-header-gdg">
          <h2 class="section-title-gdg">我們的理念</h2>
          <div class="section-underline-gdg"></div>
        </div>
        <div class="philosophy-content-gdg">
          <p class="philosophy-text-gdg">
            台灣，是一座被山海與美味餵養的寶島。<br>
            我們相信，每一道料理背後，都藏著一段島嶼的故事與職人的堅持。<br>
            <span class="highlight-gdg">「饗島」</span>致力於建構全方位的食旅生態圈，<br>
            讓您在寶島的每個角落，都能找到最真誠的款待。
          </p>
        </div>
      </div>
    </section> -->

        <!-- Services Section -->
        <section class="services-section-gdg">
            <div class="container">
                <div class="section-header-gdg">
                    <h2 class="section-title-gdg">饗島禮遇</h2>
                    <div class="section-underline-gdg"></div>
                </div>

                <div class="services-grid-gdg">
                    <!-- Service 1: Navigation -->
                    <div class="service-card-gdg">
                        <div class="service-icon-gdg">
                            <svg xmlns="http://www.w3.org/2000/svg" width="48" height="48" fill="currentColor"
                                viewBox="0 0 16 16">
                                <path
                                    d="M8 16s6-5.686 6-10A6 6 0 0 0 2 6c0 4.314 6 10 6 10zm0-7a3 3 0 1 1 0-6 3 3 0 0 1 0 6z" />
                            </svg>
                        </div>
                        <h3 class="service-title-gdg">美味導航</h3>
                        <p class="service-description-gdg">
                            精心繪製的味覺地圖，帶您穿梭巷弄尋覓寶島秘境，發現更多台灣在地美食
                        </p>
                        <div class="service-tags-gdg">
                            <RouterLink to="/mapSearch" class="service-tag-gdg">地圖搜尋</RouterLink>
                            <RouterLink to="/mapSearch" class="service-tag-gdg">美食搜尋</RouterLink>
                        </div>
                    </div>

                    <!-- Service 2: Booking -->
                    <div class="service-card-gdg">
                        <div class="service-icon-gdg">
                            <svg xmlns="http://www.w3.org/2000/svg" width="48" height="48" fill="currentColor"
                                viewBox="0 0 16 16">
                                <path
                                    d="M11 6.5a.5.5 0 0 1 .5-.5h1a.5.5 0 0 1 .5.5v1a.5.5 0 0 1-.5.5h-1a.5.5 0 0 1-.5-.5v-1zm-3 0a.5.5 0 0 1 .5-.5h1a.5.5 0 0 1 .5.5v1a.5.5 0 0 1-.5.5h-1a.5.5 0 0 1-.5-.5v-1zm-5 3a.5.5 0 0 1 .5-.5h1a.5.5 0 0 1 .5.5v1a.5.5 0 0 1-.5.5h-1a.5.5 0 0 1-.5-.5v-1zm3 0a.5.5 0 0 1 .5-.5h1a.5.5 0 0 1 .5.5v1a.5.5 0 0 1-.5.5h-1a.5.5 0 0 1-.5-.5v-1z" />
                                <path
                                    d="M3.5 0a.5.5 0 0 1 .5.5V1h8V.5a.5.5 0 0 1 1 0V1h1a2 2 0 0 1 2 2v11a2 2 0 0 1-2 2H2a2 2 0 0 1-2-2V3a2 2 0 0 1 2-2h1V.5a.5.5 0 0 1 .5-.5zM1 4v10a1 1 0 0 0 1 1h12a1 1 0 0 0 1-1V4H1z" />
                            </svg>
                        </div>
                        <h3 class="service-title-gdg">饗島之約</h3>
                        <p class="service-description-gdg">
                            您的專屬引路人，為您預約每一場期待已久的相遇，讓美食之旅更加順暢
                        </p>
                        <div class="service-tags-gdg">
                            <span class="service-tag-gdg">線上訂位</span>
                            <RouterLink to="/userInfo/bookings" class="service-tag-gdg">即時確認</RouterLink>
                        </div>
                    </div>

                    <!-- Service 3: Shopping -->
                    <div class="service-card-gdg">
                        <div class="service-icon-gdg">
                            <svg xmlns="http://www.w3.org/2000/svg" width="48" height="48" fill="currentColor"
                                viewBox="0 0 16 16">
                                <path
                                    d="M0 1.5A.5.5 0 0 1 .5 1H2a.5.5 0 0 1 .485.379L2.89 3H14.5a.5.5 0 0 1 .491.592l-1.5 8A.5.5 0 0 1 13 12H4a.5.5 0 0 1-.491-.408L2.01 3.607 1.61 2H.5a.5.5 0 0 1-.5-.5zM5 12a2 2 0 1 0 0 4 2 2 0 0 0 0-4zm7 0a2 2 0 1 0 0 4 2 2 0 0 0 0-4zm-7 1a1 1 0 1 1 0 2 1 1 0 0 1 0-2zm7 0a1 1 0 1 1 0 2 1 1 0 0 1 0-2z" />
                            </svg>
                        </div>
                        <h3 class="service-title-gdg">質感選物</h3>
                        <p class="service-description-gdg">
                            將感動轉化為指尖上的美好，在地嚴選的滋味突破地理限制，隨時傳遞至您的餐桌
                        </p>
                        <div class="service-tags-gdg">
                            <RouterLink to="/shopStore" class="service-tag-gdg">在地嚴選</RouterLink>
                            <RouterLink to="/shopStore" class="service-tag-gdg">產地直送</RouterLink>
                        </div>
                    </div>
                </div>
            </div>
        </section>

        <!-- CTA Section -->
        <section class="cta-section-gdg">
            <div class="container">
                <div class="cta-content-gdg">
                    <h2 class="cta-title-gdg">開始您的美食之旅</h2>
                    <p class="cta-text-gdg">探索台灣，品味每一處角落的美好滋味</p>
                    <div class="cta-buttons-gdg">
                        <RouterLink to="/mapSearch" class="btn-cta-primary-gdg">探索美食地圖</RouterLink>
                        <RouterLink to="/about" class="btn-cta-outline-gdg">了解更多</RouterLink>
                    </div>
                </div>
            </div>
        </section>

        <!-- Original Content (moved to bottom) -->

    </div>
</template>

<style scoped lang="scss">
@import '@/assets/styles/custom.scss';

.home-view-gdg {
    min-height: 100vh;
    background-color: #fff;
}

// Hero Section
.hero-section-gdg {
    position: relative;
    height: 85vh;
    min-height: 600px;
    background: linear-gradient(135deg, rgba(159, 149, 114, 0.92) 0%, rgba(119, 111, 84, 0.92) 100%),
        url('@/pictures/bannerBrown.jpg') center/cover;
    display: flex;
    align-items: center;
    justify-content: center;
    overflow: hidden;

    &::before {
        content: '';
        position: absolute;
        top: 0;
        left: 0;
        right: 0;
        bottom: 0;
        background: repeating-linear-gradient(0deg,
                rgba(255, 255, 255, 0.02) 0px,
                transparent 1px,
                transparent 2px,
                rgba(255, 255, 255, 0.02) 3px);
        pointer-events: none;
    }
}

.hero-overlay-gdg {
    position: relative;
    z-index: 2;
    text-align: center;
    padding: 2rem;
}

.hero-content-gdg {
    animation: fadeInUp 1.2s ease-out;
}

.hero-title-gdg {
    font-family: 'Noto Serif TC', serif;
    font-size: 6rem;
    font-weight: 700;
    color: #fff;
    letter-spacing: 0.3em;
    margin-bottom: 1.5rem;
    text-shadow: 3px 3px 12px rgba(0, 0, 0, 0.3);

    @media (max-width: 768px) {
        font-size: 3.5rem;
    }
}

.hero-divider-gdg {
    width: 150px;
    height: 3px;
    background-color: #fff;
    margin: 0 auto 2rem;
    box-shadow: 0 2px 10px rgba(0, 0, 0, 0.2);
}

.hero-subtitle-gdg {
    font-family: 'Noto Serif TC', serif;
    font-size: 1.8rem;
    font-weight: 400;
    color: rgba(255, 255, 255, 0.95);
    letter-spacing: 0.2em;
    margin-bottom: 1.5rem;

    @media (max-width: 768px) {
        font-size: 1.3rem;
    }
}

.hero-description-gdg {
    font-family: 'Noto Serif TC', serif;
    font-size: 1.2rem;
    font-weight: 300;
    color: rgba(255, 255, 255, 0.9);
    letter-spacing: 0.15em;
    line-height: 2;

    @media (max-width: 768px) {
        font-size: 1rem;

        br {
            display: none;
        }
    }
}

// Philosophy Section
.philosophy-section-gdg {
    padding: 6rem 2rem;
    background-color: #fafaf8;
}

.section-header-gdg {
    text-align: center;
    margin-bottom: 3rem;
}

.section-title-gdg {
    font-family: 'Noto Serif TC', serif;
    font-size: 2.5rem;
    font-weight: 700;
    color: $gdg-gold;
    letter-spacing: 0.15em;
    margin-bottom: 1rem;
}

.section-underline-gdg {
    width: 80px;
    height: 3px;
    background-color: $gdg-gold;
    margin: 0 auto;
}

.philosophy-content-gdg {
    max-width: 900px;
    margin: 0 auto;
    text-align: center;
}

.philosophy-text-gdg {
    font-family: 'Noto Serif TC', serif;
    font-size: 1.4rem;
    line-height: 2.2;
    color: #333;
    letter-spacing: 0.08em;

    @media (max-width: 768px) {
        font-size: 1.1rem;

        br {
            display: none;
        }
    }
}

.highlight-gdg {
    color: $gdg-gold;
    font-weight: 600;
    position: relative;

    &::after {
        content: '';
        position: absolute;
        bottom: -2px;
        left: 0;
        width: 100%;
        height: 2px;
        background-color: $gdg-gold;
        opacity: 0.3;
    }
}

// Services Section
.services-section-gdg {
    padding: 6rem 2rem;
    background-color: #fff;
}

.services-grid-gdg {
    max-width: 1200px;
    margin: 0 auto;
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
    gap: 3rem;
    margin-top: 4rem;
}

.service-card-gdg {
    padding: 3rem 2rem;
    text-align: center;
    border: 2px solid $gdg-gold;
    background-color: #fff;
    transition: all 0.4s ease;
    position: relative;

    &::before {
        content: '';
        position: absolute;
        top: 0;
        left: 0;
        right: 0;
        bottom: 0;
        background-color: $gdg-bg-light;
        transform: scaleY(0);
        transform-origin: bottom;
        transition: transform 0.4s ease;
        z-index: 0;
    }

    &:hover {
        box-shadow: 12px 12px 0px rgba(160, 150, 115, 0.2);
        transform: translate(-3px, -3px);

        &::before {
            transform: scaleY(1);
            transform-origin: top;
        }
    }

    >* {
        position: relative;
        z-index: 1;
    }
}

.service-icon-gdg {
    width: 80px;
    height: 80px;
    margin: 0 auto 2rem;
    display: flex;
    align-items: center;
    justify-content: center;
    color: $gdg-gold;
    transition: all 0.3s ease;

    .service-card-gdg:hover & {
        transform: scale(1.1);
        color: $gdg-gold-dark;
    }
}

.service-title-gdg {
    font-family: 'Noto Serif TC', serif;
    font-size: 1.8rem;
    font-weight: 700;
    color: $gdg-gold;
    letter-spacing: 0.12em;
    margin-bottom: 1.5rem;
}

.service-description-gdg {
    font-family: 'Noto Sans TC', sans-serif;
    font-size: 1.05rem;
    line-height: 1.8;
    color: #555;
    margin-bottom: 2rem;
    letter-spacing: 0.05em;
}

.service-tags-gdg {
    display: flex;
    gap: 0.8rem;
    justify-content: center;
    flex-wrap: wrap;
}

.service-tag-gdg {
    display: inline-block;
    padding: 0.4rem 1rem;
    background-color: transparent;
    color: $gdg-gold-dark;
    font-size: 0.85rem;
    font-weight: 500;
    letter-spacing: 0.05em;
    border: 1px solid $gdg-gold;
    text-decoration: none;
    transition: all 0.3s ease;

    .service-card-gdg:hover & {
        background-color: $gdg-gold;
        color: #fff;
    }
}

// CTA Section
.cta-section-gdg {
    padding: 6rem 2rem;
    background: linear-gradient(135deg, rgba(159, 149, 114, 0.08) 0%, rgba(119, 111, 84, 0.08) 100%);
    text-align: center;
}

.cta-content-gdg {
    max-width: 800px;
    margin: 0 auto;
}

.cta-title-gdg {
    font-family: 'Noto Serif TC', serif;
    font-size: 2.5rem;
    font-weight: 700;
    color: $gdg-gold;
    letter-spacing: 0.15em;
    margin-bottom: 1.5rem;

    @media (max-width: 768px) {
        font-size: 2rem;
    }
}

.cta-text-gdg {
    font-family: 'Noto Serif TC', serif;
    font-size: 1.3rem;
    color: $gdg-gold-dark;
    letter-spacing: 0.1em;
    margin-bottom: 3rem;

    @media (max-width: 768px) {
        font-size: 1.1rem;
    }
}

.cta-buttons-gdg {
    display: flex;
    gap: 1.5rem;
    justify-content: center;
    flex-wrap: wrap;
}

.btn-cta-primary-gdg {
    display: inline-block;
    padding: 1rem 2.5rem;
    background-color: $gdg-gold;
    color: #fff;
    text-decoration: none;
    font-family: 'Noto Sans TC', sans-serif;
    font-size: 1.1rem;
    font-weight: 500;
    letter-spacing: 0.08em;
    border: 2px solid $gdg-gold;
    transition: all 0.3s ease;

    &:hover {
        background-color: $gdg-gold-dark;
        border-color: $gdg-gold-dark;
        transform: translateY(-3px);
        box-shadow: 0 8px 16px rgba(159, 149, 114, 0.3);
    }
}

.btn-cta-outline-gdg {
    display: inline-block;
    padding: 1rem 2.5rem;
    background-color: transparent;
    color: $gdg-gold;
    text-decoration: none;
    font-family: 'Noto Sans TC', sans-serif;
    font-size: 1.1rem;
    font-weight: 500;
    letter-spacing: 0.08em;
    border: 2px solid $gdg-gold;
    transition: all 0.3s ease;

    &:hover {
        background-color: $gdg-gold;
        color: #fff;
        transform: translateY(-3px);
        box-shadow: 0 8px 16px rgba(159, 149, 114, 0.3);
    }
}

// Test Section (original content at bottom)
.test-section-gdg {
    padding: 4rem 0;
    background-color: #f8f9fa;
    border-top: 1px solid rgba(160, 150, 115, 0.2);
}

// Animations
@keyframes fadeInUp {
    from {
        opacity: 0;
        transform: translateY(40px);
    }

    to {
        opacity: 1;
        transform: translateY(0);
    }
}
</style>