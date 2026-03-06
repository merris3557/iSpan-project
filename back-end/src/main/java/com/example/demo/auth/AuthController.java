package com.example.demo.auth;

import com.example.demo.auth.dto.AuthResponse;
import com.example.demo.auth.dto.ForgotPasswordRequest;
import com.example.demo.auth.dto.LoginRequest;
import com.example.demo.auth.dto.OAuth2TwoFactorRequest;
import com.example.demo.auth.dto.RegisterRequest;
import com.example.demo.auth.dto.ResetPasswordRequest;
import com.example.demo.common.dto.ApiResponse;
import com.example.demo.user.UserService;
import com.example.demo.user.dto.UserResponse;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.WebUtils;

import jakarta.servlet.http.Cookie;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final UserService userService;
    private final PasswordResetService passwordResetService;

    @Value("${jwt.access-token-expiration-ms}")
    private long accessTokenExpirationMs;

    @Value("${jwt.refresh-token-expiration-ms}")
    private long refreshTokenExpirationMs;

    // ===== 登入 =====
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<UserResponse>> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletResponse response) {

        AuthResponse authResponse = authService.login(request);
        setCookies(response, authResponse.getAccessToken(), authResponse.getRefreshToken());

        return ResponseEntity.ok(ApiResponse.success("Login successful", authResponse.getUser()));
    }

    // ===== 註冊 =====
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserResponse>> register(
            @Valid @RequestBody RegisterRequest request,
            HttpServletResponse response) {

        AuthResponse authResponse = authService.register(request);
        setCookies(response, authResponse.getAccessToken(), authResponse.getRefreshToken());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("User registered successfully", authResponse.getUser()));
    }

    // ===== 登出 =====
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(HttpServletResponse response) {
        clearCookies(response);
        return ResponseEntity.ok(ApiResponse.success("Logout successful", null));
    }

    // ===== 刷新 Token =====
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<UserResponse>> refreshToken(
            HttpServletRequest request,
            HttpServletResponse response) {

        Cookie refreshCookie = WebUtils.getCookie(request, "refreshToken");
        if (refreshCookie == null || refreshCookie.getValue() == null || refreshCookie.getValue().isBlank()) {
            clearCookies(response);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("No refresh token found"));
        }

        try {
            AuthResponse authResponse = authService.refreshToken(refreshCookie.getValue());
            setCookies(response, authResponse.getAccessToken(), authResponse.getRefreshToken());
            return ResponseEntity.ok(ApiResponse.success("Token refreshed successfully", authResponse.getUser()));
        } catch (Exception e) {
            clearCookies(response);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Invalid or expired refresh token"));
        }
    }

    // ===== OAuth2 2FA 驗證 =====
    @PostMapping("/oauth2/2fa-verify")
    public ResponseEntity<ApiResponse<UserResponse>> oauth2Verify2FA(
            @Valid @RequestBody OAuth2TwoFactorRequest request,
            HttpServletResponse response) {

        AuthResponse authResponse = authService.verifyOAuth2TwoFactor(request);
        setCookies(response, authResponse.getAccessToken(), authResponse.getRefreshToken());

        return ResponseEntity.ok(ApiResponse.success("OAuth2 Login successful", authResponse.getUser()));
    }

    // ===== 取得目前登入使用者 =====
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> getCurrentUser() {
        UserResponse response = userService.getCurrentUser();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // ===== 忘記密碼 =====
    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        passwordResetService.initiatePasswordReset(request.getEmail());
        return ResponseEntity.ok(ApiResponse.success("Password reset email sent", null));
    }

    // ===== 重設密碼 =====
    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<Void>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new RuntimeException("Passwords do not match");
        }
        passwordResetService.resetPassword(request.getToken(), request.getNewPassword());
        return ResponseEntity.ok(ApiResponse.success("Password reset successfully", null));
    }

    // ===== 工具方法：設定 HttpOnly Cookie =====
    private void setCookies(HttpServletResponse response, String accessToken, String refreshToken) {
        ResponseCookie accessCookie = ResponseCookie.from("accessToken", accessToken)
                .httpOnly(true)
                .secure(false) // 生產環境改 true（HTTPS）
                .path("/")
                .maxAge(accessTokenExpirationMs / 1000)
                .sameSite("Lax")
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, accessCookie.toString());

        ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", refreshToken)
                .httpOnly(true)
                .secure(false) // 生產環境改 true（HTTPS）
                .path("/api/auth/refresh") // 僅 refresh 端點可讀取
                .maxAge(refreshTokenExpirationMs / 1000)
                .sameSite("Lax")
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());
    }

    // ===== 工具方法：清除 Cookie =====
    private void clearCookies(HttpServletResponse response) {
        ResponseCookie clearAccess = ResponseCookie.from("accessToken", "")
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(0)
                .sameSite("Lax")
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, clearAccess.toString());

        ResponseCookie clearRefresh = ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .secure(false)
                .path("/api/auth/refresh")
                .maxAge(0)
                .sameSite("Lax")
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, clearRefresh.toString());

        ResponseCookie clearJsessionId = ResponseCookie.from("JSESSIONID", "")
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(0)
                .sameSite("Lax")
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, clearJsessionId.toString());
    }
}
