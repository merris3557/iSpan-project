package com.example.demo.admin;

import com.example.demo.admin.dto.AdminLoginRequest;
import com.example.demo.admin.dto.AdminLoginResponse;
import com.example.demo.admin.dto.AdminRegistrationRequest;
import com.example.demo.admin.dto.AdminResponse;
import com.example.demo.common.dto.ApiResponse;
import jakarta.servlet.http.Cookie;
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

@RestController
@RequestMapping("/api/admins")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;
    private final AdminPasswordResetService adminPasswordResetService;

    @Value("${jwt.access-token-expiration-ms}")
    private long accessTokenExpirationMs;

    @Value("${jwt.refresh-token-expiration-ms}")
    private long refreshTokenExpirationMs;

    // ===== 登入 =====
    @PostMapping("/login")
    public ResponseEntity<?> login(
            @Valid @RequestBody AdminLoginRequest request,
            HttpServletResponse response) {
        try {
            AdminLoginResponse loginResponse = adminService.login(request);
            setCookies(response, loginResponse.getAccessToken(), loginResponse.getRefreshToken());
            return ResponseEntity.ok(ApiResponse.success("Admin login successful", loginResponse.getAdmin()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    // ===== 登出 =====
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(HttpServletResponse response) {
        clearCookies(response);
        return ResponseEntity.ok(ApiResponse.success("Admin logout successful", null));
    }

    // ===== 刷新 Token =====
    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(
            HttpServletRequest request,
            HttpServletResponse response) {

        Cookie refreshCookie = WebUtils.getCookie(request, "adminRefreshToken");
        if (refreshCookie == null || refreshCookie.getValue() == null || refreshCookie.getValue().isBlank()) {
            clearCookies(response);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("No refresh token found"));
        }

        try {
            AdminLoginResponse loginResponse = adminService.refreshToken(refreshCookie.getValue());
            setCookies(response, loginResponse.getAccessToken(), loginResponse.getRefreshToken());
            return ResponseEntity.ok(ApiResponse.success("Token refreshed successfully", loginResponse.getAdmin()));
        } catch (Exception e) {
            clearCookies(response);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Invalid or expired refresh token"));
        }
    }

    // ===== 取得目前登入管理員 =====
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentAdmin() {
        try {
            Admin admin = adminService.getCurrentAdmin();
            return ResponseEntity.ok(ApiResponse.success(adminService.mapToPublicAdminResponse(admin)));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    // ===== 新增管理員 =====
    @PostMapping
    public ResponseEntity<?> registerAdmin(@Valid @RequestBody AdminRegistrationRequest request) {
        try {
            AdminResponse response = adminService.registerAdmin(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Admin registered successfully", response));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    // ===== 更新管理員 =====
    @PutMapping("/{id}")
    public ResponseEntity<?> updateAdmin(@PathVariable Integer id,
            @Valid @RequestBody com.example.demo.admin.dto.AdminUpdateRequest request) {
        try {
            AdminResponse response = adminService.updateAdmin(id, request);
            return ResponseEntity.ok(ApiResponse.success("Admin updated successfully", response));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    // ===== 取得所有管理員 =====
    @GetMapping
    public ResponseEntity<?> getAllAdmins() {
        return ResponseEntity.ok(ApiResponse.success("Admins retrieved successfully", adminService.getAllAdmins()));
    }

    // ===== 忘記密碼 =====
    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(
            @Valid @RequestBody com.example.demo.auth.dto.ForgotPasswordRequest request) {
        adminPasswordResetService.initiatePasswordReset(request.getEmail());
        return ResponseEntity.ok(ApiResponse.success("Password reset email sent", null));
    }

    // ===== 重設密碼 =====
    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<Void>> resetPassword(
            @Valid @RequestBody com.example.demo.auth.dto.ResetPasswordRequest request) {
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new RuntimeException("Passwords do not match");
        }
        adminPasswordResetService.resetPassword(request.getToken(), request.getNewPassword());
        return ResponseEntity.ok(ApiResponse.success("Password reset successfully", null));
    }

    // ===== 工具方法：設定管理員 HttpOnly Cookie =====
    private void setCookies(HttpServletResponse response, String accessToken, String refreshToken) {
        ResponseCookie accessCookie = ResponseCookie.from("adminAccessToken", accessToken)
                .httpOnly(true)
                .secure(false) // 生產環境改 true（HTTPS）
                .path("/")
                .maxAge(accessTokenExpirationMs / 1000)
                .sameSite("Lax")
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, accessCookie.toString());

        ResponseCookie refreshCookie = ResponseCookie.from("adminRefreshToken", refreshToken)
                .httpOnly(true)
                .secure(false) // 生產環境改 true（HTTPS）
                .path("/api/admins/refresh") // 僅 refresh 端點可讀取
                .maxAge(refreshTokenExpirationMs / 1000)
                .sameSite("Lax")
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());
    }

    // ===== 工具方法：清除管理員 Cookie =====
    private void clearCookies(HttpServletResponse response) {
        ResponseCookie clearAccess = ResponseCookie.from("adminAccessToken", "")
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(0)
                .sameSite("Lax")
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, clearAccess.toString());

        ResponseCookie clearRefresh = ResponseCookie.from("adminRefreshToken", "")
                .httpOnly(true)
                .secure(false)
                .path("/api/admins/refresh")
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
