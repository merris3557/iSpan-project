package com.example.demo.common.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;

import jakarta.servlet.http.Cookie;
import org.springframework.web.util.WebUtils;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider tokenProvider;
    private final CustomUserDetailsService customUserDetailsService;

    /**
     * Refresh 端點本就是在 access token 過期後才呼叫，
     * 不應被 access token 的有效性阻擋，因此跳過此 Filter。
     * 後端 AuthController 自行讀取 refreshToken Cookie 處理換發。
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.equals("/api/auth/refresh") || path.equals("/api/admins/refresh");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        try {
            // 取得當前請求「應該」使用的唯一 Token
            String jwt = getTargetJwt(request);

            if (jwt != null) {
                if (tokenProvider.validateToken(jwt)) {
                    String email = tokenProvider.getEmailFromToken(jwt);
                    UserDetails userDetails = customUserDetailsService.loadUserByUsername(email);
                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                } else {
                    // Token 存在但無效（過期、簽名錯誤等），必須明確回傳 401
                    // 不能靜默繼續，否則後續的 .anyRequest().authenticated() 會拋 403
                    // 前端的 Refresh 攔截器只會處理 401，因此不回 401 就無法自動 refresh
                    logger.warn("[JwtAuth] Token 存在但驗證失敗，path=" + request.getRequestURI() + "，回傳 401");
                    response.setContentType("application/json;charset=UTF-8");
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.getWriter().write("{\"success\":false,\"message\":\"Token expired or invalid\"}");
                    return;
                }
            }
        } catch (Exception ex) {
            logger.error("Could not set user authentication in security context", ex);
        }

        filterChain.doFilter(request, response);
    }

    /**
     * 嚴格判斷當前請求的上下文，只回傳對應的 Token。
     * 絕對不可以因為 A 過期就去拿 B，這會導致身份嚴重錯亂 (例如前台拿後台的信箱去 User 表查而報錯)。
     */
    private String getTargetJwt(HttpServletRequest request) {
        // 1. Authorization Header（給行動裝置或外部 API 使用）
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }

        String path = request.getRequestURI();

        // 2. 獲取前端明確指定的上下文提示
        String contextHint = request.getHeader("X-Context-Hint");
        boolean isAdminContext;

        if (StringUtils.hasText(contextHint)) {
            isAdminContext = "ADMIN".equals(contextHint);
        } else {
            // 如果沒有標頭 (例如 Postman 或外部系統呼叫)，以 API URL 前綴為準
            isAdminContext = path.startsWith("/api/admins") ||
                             path.contains("/review") ||
                             path.startsWith("/api/feedbackList");
        }

        logger.debug(String.format("[JwtAuth] path=%s, contextHint=%s -> isAdminContext=%s", path, contextHint, isAdminContext));

        if (isAdminContext) {
            // 在後台操作，只能讀取 Admin 的 Token
            Cookie adminCookie = WebUtils.getCookie(request, "adminAccessToken");
            if (adminCookie != null) {
                logger.debug("[JwtAuth] extracted adminAccessToken");
                return StringUtils.hasText(adminCookie.getValue()) ? adminCookie.getValue() : null;
            }
            return null;
        } else {
            // 在前台操作，只能讀取 User 的 Token
            Cookie userCookie = WebUtils.getCookie(request, "accessToken");
            if (userCookie != null) {
                logger.debug("[JwtAuth] extracted accessToken");
                return StringUtils.hasText(userCookie.getValue()) ? userCookie.getValue() : null;
            }
            return null;
        }
    }
}
