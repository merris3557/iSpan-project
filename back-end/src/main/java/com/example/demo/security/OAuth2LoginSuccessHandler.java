package com.example.demo.security;

import com.example.demo.entity.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

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

        // 生成 JWT tokens
        String accessToken = tokenProvider.generateAccessToken(user.getEmail(),
                Boolean.TRUE.equals(user.getIsStore()) ? "STORE" : "USER");
        String refreshToken = tokenProvider.generateRefreshToken(user.getEmail());

        // 重定向到前端，將 tokens 作為 URL 參數傳遞
        // 注意：實際production環境建議使用更安全的方式傳遞token
        String targetUrl = UriComponentsBuilder.fromUriString(frontendRedirectUri)
                .queryParam("accessToken", accessToken)
                .queryParam("refreshToken", refreshToken)
                .queryParam("tokenType", "Bearer")
                .build().toUriString();

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}
