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
import java.util.ArrayList;
import java.util.List;

import jakarta.servlet.http.Cookie;
import org.springframework.web.util.WebUtils;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider tokenProvider;
    private final CustomUserDetailsService customUserDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        try {
            // 取得所有候選 Token（依優先順序排列）
            List<String> candidates = getJwtCandidates(request);

            for (String jwt : candidates) {
                if (tokenProvider.validateToken(jwt)) {
                    String email = tokenProvider.getEmailFromToken(jwt);
                    UserDetails userDetails = customUserDetailsService.loadUserByUsername(email);
                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    break; // 找到有效的就停止
                }
            }
        } catch (Exception ex) {
            logger.error("Could not set user authentication in security context", ex);
        }

        filterChain.doFilter(request, response);
    }

    /**
     * 回傳所有候選 JWT Token（依優先順序）。
     * 若第一個 token 過期，doFilterInternal 會自動嘗試下一個。
     */
    private List<String> getJwtCandidates(HttpServletRequest request) {
        List<String> candidates = new ArrayList<>();

        // 1. Authorization Header（行動裝置 / 第三方系統 — 最高優先）
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            candidates.add(bearerToken.substring(7));
            return candidates; // Header 有就不看 Cookie
        }

        String path = request.getRequestURI();

        // 2. /api/admins/** 路徑：admin cookie 優先
        if (path.startsWith("/api/admins")) {
            addCookieIfPresent(candidates, request, "adminAccessToken");
            addCookieIfPresent(candidates, request, "accessToken");
        } else {
            // 3. 其他路徑（/api/users 等）：都加入，讓 doFilterInternal 逐一驗證
            addCookieIfPresent(candidates, request, "accessToken");
            addCookieIfPresent(candidates, request, "adminAccessToken");
        }

        return candidates;
    }

    private void addCookieIfPresent(List<String> list, HttpServletRequest request, String cookieName) {
        Cookie cookie = WebUtils.getCookie(request, cookieName);
        if (cookie != null && StringUtils.hasText(cookie.getValue())) {
            list.add(cookie.getValue());
        }
    }
}
