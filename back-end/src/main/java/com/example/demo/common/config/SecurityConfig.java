package com.example.demo.common.config;

import java.util.Arrays;
import java.util.List;
import lombok.RequiredArgsConstructor;
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
                        .requestMatchers("/api/auth/**").permitAll()
                        // 放行管理員登入
                        .requestMatchers("/api/admins/login").permitAll()
                        // TODO: 測試完成後，移除此行恢復強制登入檢查
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/admins").permitAll()
                        .requestMatchers(org.springframework.http.HttpMethod.POST, "/api/admins").permitAll() // 暫時放行新增管理員
                        // 鎖定其他管理員 API，必須具備 ADMIN 權限才能訪問
                        .requestMatchers("/api/admins/**").hasRole("ADMIN")
                        // 允許訪問店鋪註冊端點
                        .requestMatchers("/api/store-registrations/**").permitAll()
                        // OAuth2 登入端點
                        .requestMatchers("/api/products/**").permitAll()
                        //放行電商商品相關API
                        .requestMatchers("/oauth2/**", "/login/oauth2/**").permitAll()
                        // 地圖搜尋端點：允許匿名存取（搜尋不需要登入）
                        .requestMatchers("/api/map/**").permitAll()
                        // 管理員權限端點
                        // .requestMatchers(HttpMethod.GET, "/api/users").hasRole("ADMIN")
                        // .requestMatchers(HttpMethod.PUT,
                        // "/api/users/*/store-status").hasRole("ADMIN")
                        // .requestMatchers(HttpMethod.DELETE, "/api/users/*").hasRole("ADMIN")
                        // 其他請求需要認證
                        .anyRequest().authenticated())
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
                        .successHandler(oAuth2LoginSuccessHandler))
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration
                .setAllowedOrigins(List.of("http://localhost:3000", "http://localhost:8080", "http://localhost:5173"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setExposedHeaders(List.of("Authorization", "Content-Type"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
