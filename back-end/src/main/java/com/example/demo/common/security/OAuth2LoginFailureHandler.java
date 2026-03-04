package com.example.demo.common.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * OAuth2 登入失敗處理器
 * 當帳號被停用（enabled=false）或其他 OAuth2 錯誤時，
 * 將錯誤訊息帶回前端。
 */
@Component
@RequiredArgsConstructor
public class OAuth2LoginFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    @Value("${app.oauth2.redirect-uri:http://localhost:3000/oauth2/redirect}")
    private String frontendRedirectUri;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException exception) throws IOException, ServletException {

        String errorMessage = "登入失敗，請稍後再試。";

        // 判斷是否為帳號停用錯誤
        if (exception.getMessage() != null && exception.getMessage().contains("此帳號已被停用")) {
            errorMessage = exception.getMessage();
        }

        String targetUrl = UriComponentsBuilder.fromUriString(frontendRedirectUri)
                .queryParam("error", URLEncoder.encode(errorMessage, StandardCharsets.UTF_8))
                .build().toUriString();

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}
