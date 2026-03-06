package com.example.demo.common.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import com.example.demo.user.User;

import java.io.IOException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;

/**
 * OAuth2 登入成功處理器
 * 生成 JWT token 並重定向到前端
 */
@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtTokenProvider tokenProvider;

    // TODO: 請在 application.properties 設定前端的重定向 URL
    @Value("${app.oauth2.redirect-uri:http://localhost:3000/oauth2/redirect}")
    private String frontendRedirectUri;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication) throws IOException, ServletException {

        if (response.isCommitted()) {
            logger.debug("Response has already been committed. Unable to redirect.");
            return;
        }

        // 從 authentication 中取得用戶資訊
        CustomOAuth2User oauth2User = (CustomOAuth2User) authentication.getPrincipal();
        User user = oauth2User.getUser();

        String targetUrl;

        if (Boolean.TRUE.equals(user.getTwoFactorEnabled())) {
            // 需要進行 2FA 驗證，發行短暫的 preAuthToken
            String preAuthToken = tokenProvider.generatePreAuthToken(user.getEmail());
            targetUrl = UriComponentsBuilder.fromUriString(frontendRedirectUri)
                    .queryParam("require2fa", "true")
                    .queryParam("preAuthToken", preAuthToken)
                    .build().toUriString();
        } else {
            // 不需要 2FA，直接發行正式 Token
            String accessToken = tokenProvider.generateAccessToken(user.getEmail(),
                    Boolean.TRUE.equals(user.getIsStore()) ? "STORE" : "USER");
            String refreshToken = tokenProvider.generateRefreshToken(user.getEmail());

            // 寫入 HttpOnly Cookie
            ResponseCookie jwtCookie = ResponseCookie.from("accessToken", accessToken)
                    .httpOnly(true)
                    .secure(false) // in production use true
                    .path("/")
                    .maxAge(24 * 60 * 60)
                    .build();
            response.addHeader(HttpHeaders.SET_COOKIE, jwtCookie.toString());

            ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", refreshToken)
                    .httpOnly(true)
                    .secure(false)
                    .path("/")
                    .maxAge(7 * 24 * 60 * 60)
                    .build();
            response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());

            targetUrl = UriComponentsBuilder.fromUriString(frontendRedirectUri)
                    .queryParam("success", "true")
                    .build().toUriString();
        }

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}
