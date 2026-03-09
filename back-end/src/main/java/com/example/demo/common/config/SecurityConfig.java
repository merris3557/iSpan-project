package com.example.demo.common.config;

import java.util.Arrays;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.example.demo.common.security.JwtAuthenticationFilter;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final UserDetailsService userDetailsService;
    private final com.example.demo.common.security.CustomOAuth2UserService customOAuth2UserService;
    private final com.example.demo.common.security.OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;
    private final com.example.demo.common.security.OAuth2LoginFailureHandler oAuth2LoginFailureHandler;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // 允許訪問認證相關端點
                        // 注意：/api/auth/me 必須在 /api/auth/** 之前，否則 permitAll 會覆蓋它
                        // Token 過期時若 /me 被 permitAll，後端用 anonymousUser 查找會回 404 而非 401，
                        // 導致前端 Refresh 攔截器無法觸發
                        .requestMatchers("/api/auth/me").authenticated()
                        .requestMatchers("/api/auth/**").permitAll()
                        // 放行管理員登入與相關基礎功能
                        .requestMatchers("/api/admins/login").permitAll()
                        .requestMatchers("/api/admins/logout").permitAll()
                        .requestMatchers("/api/admins/refresh").permitAll() // Refresh 不需帶有效 Token
                        .requestMatchers("/api/admins/forgot-password").permitAll()
                        .requestMatchers("/api/admins/reset-password").permitAll()
                        // TODO: 測試完成後，移除此行恢復強制登入檢查
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/admins").permitAll()
                        .requestMatchers(org.springframework.http.HttpMethod.POST, "/api/admins").permitAll() // 暫時放行新增管理員
                        // 鎖定其他管理員 API，必須具備 ADMIN 權限才能訪問
                        .requestMatchers("/api/admins/**").hasRole("ADMIN")
                        // 允許訪問店鋪註冊端點 (註：原先測試時已發現 permitAll 會導致 Token 過期時無法觸發 refresh 且拋出 400 anonymousUser 錯誤)
                        .requestMatchers("/api/store-registrations/**").authenticated()
                        // 放行綠界相關 API（付款、回傳等）
                        .requestMatchers("/api/ecpay/**").permitAll()
                        // 產品相關 API: GET 允許匿名瀏覽，POST/PUT/DELETE 需要驗證或店家/管理員權限
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/products/**").permitAll()
                        .requestMatchers("/api/products/**").authenticated() // 新增、修改、刪除需登入 (若有特定 Role 請自行調整)
                        // OAuth2 登入端點
                        .requestMatchers("/oauth2/**", "/login/oauth2/**").permitAll()
                        // 地圖搜尋端點：允許匿名存取（搜尋不需要登入）
                        .requestMatchers("/api/map/**").permitAll()
                        // 標籤列表：允許匿名存取（SearchBar 動態載入標籤不需要登入）
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/categories").permitAll()
                        // --- 新增：放行客服表單相關 API（公開） ---
                        .requestMatchers(org.springframework.http.HttpMethod.POST, "/api/feedback").permitAll()
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/feedback/typeList").permitAll()
                        // /api/feedback/userInfoList 需要登入（使用 @AuthenticationPrincipal）
                        // 客服後台清單與回覆：僅限管理員
                        .requestMatchers("/api/feedbackList/**").hasRole("ADMIN")
                        // 商家資訊端點：允許公開查看特定商家資訊
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/owner/store/{id}").permitAll()
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/bookings/config/**").permitAll()
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/bookings/available-slots").permitAll()
                        // 前端商店顯示最新購買資料 (允許匿名查詢最新訂單)
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/orders/latest").permitAll()
                        // 其他訂單 API：必須登入才能結帳或查詢個人的訂單
                        .requestMatchers("/api/orders/**").authenticated()
                        // 管理員權限端點
                        // .requestMatchers(HttpMethod.GET, "/api/users").hasRole("ADMIN")
                        // .requestMatchers(HttpMethod.PUT,
                        // "/api/users/*/store-status").hasRole("ADMIN")
                        // .requestMatchers(HttpMethod.DELETE, "/api/users/*").hasRole("ADMIN")
                        // 放行 Spring Boot 預設錯誤處理器路由，防止 API Exception（如403/404）轉送至此時觸發 OAuth 登入重導
                        .requestMatchers("/error").permitAll()
                        // 其他請求需要認證
                        .anyRequest().authenticated()
                        
                    )
                // 處理 /api/** 的未授權請求直接回傳 401 而非重新導向 OAuth2 登入頁
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint((request, response, authException) -> {
                            if (request.getRequestURI().startsWith("/api/")) {
                                response.setContentType("application/json;charset=UTF-8");
                                response.setStatus(401);
                                response.getWriter().write("{\"success\":false,\"message\":\"Unauthorized: "
                                        + authException.getMessage() + "\"}");
                            } else {
                                response.sendRedirect("/oauth2/authorization/google"); // fallback for non-api
                            }
                        }))
                // OAuth2 登入配置
                .oauth2Login(oauth2 -> oauth2
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(customOAuth2UserService))
                        .successHandler(oAuth2LoginSuccessHandler)
                        .failureHandler(oAuth2LoginFailureHandler))
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration
                .setAllowedOrigins(List.of("http://localhost:3000", "http://localhost:8080", "http://localhost:5173",
                "https://shily-untusked-yuri.ngrok-free.dev",
                "https://payment-stage.ecpay.com.tw",  
                "https://payment.ecpay.com.tw"
                    
                ));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setExposedHeaders(List.of("Authorization", "Content-Type"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
