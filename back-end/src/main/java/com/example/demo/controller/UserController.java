package com.example.demo.controller;

import com.example.demo.dto.ApiResponse;
import com.example.demo.dto.UpdateProfileRequest;
import com.example.demo.dto.UserResponse;
import com.example.demo.dto.ChangePasswordRequest;
import com.example.demo.dto.Enable2FAResponse;
import com.example.demo.dto.Verify2FARequest;

import com.example.demo.service.UserService;
import com.example.demo.service.TwoFactorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final TwoFactorService twoFactorService;

    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<UserResponse>> getProfile() {
        UserResponse response = userService.getCurrentUser();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/profile")
    public ResponseEntity<ApiResponse<UserResponse>> updateProfile(@Valid @RequestBody UpdateProfileRequest request) {
        UserResponse response = userService.updateProfile(request);
        return ResponseEntity.ok(ApiResponse.success("Profile updated successfully", response));
    }

    @GetMapping
    // @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAllUsers() {
        List<UserResponse> users = userService.getAllUsers();
        return ResponseEntity.ok(ApiResponse.success(users));
    }

    @PutMapping("/{id}/store-status")
    // @PreAuthorize("hasRole('ADMIN')") // TODO: Define new permission model
    public ResponseEntity<ApiResponse<UserResponse>> updateStoreStatus(
            @PathVariable Long id,
            @RequestParam Boolean isStore) {
        UserResponse response = userService.updateStoreStatus(id, isStore);
        return ResponseEntity.ok(ApiResponse.success("User store status updated successfully", response));
    }

    @DeleteMapping("/{id}")
    // @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok(ApiResponse.success("User deleted successfully", null));
    }

    /**
     * 修改密碼
     */
    @PostMapping("/change-password")
    public ResponseEntity<ApiResponse<Void>> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        // 驗證密碼確認
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new RuntimeException("Passwords do not match");
        }

        userService.changePassword(request.getCurrentPassword(), request.getNewPassword());
        return ResponseEntity.ok(ApiResponse.success("Password changed successfully", null));
    }

    /**
     * 啟用 2FA - 生成 QR code
     */
    @PostMapping("/2fa/enable")
    public ResponseEntity<ApiResponse<Enable2FAResponse>> enable2FA() {
        Enable2FAResponse response = twoFactorService.generate2FASecret();
        return ResponseEntity.ok(ApiResponse.success("Scan the QR code with Google Authenticator", response));
    }

    /**
     * 驗證並啟用 2FA
     */
    @PostMapping("/2fa/verify")
    public ResponseEntity<ApiResponse<Void>> verify2FA(@Valid @RequestBody Verify2FARequest request) {
        twoFactorService.verifyAndEnable2FA(request.getCode());
        return ResponseEntity.ok(ApiResponse.success("2FA enabled successfully", null));
    }

    /**
     * 停用 2FA
     */
    @PostMapping("/2fa/disable")
    public ResponseEntity<ApiResponse<Void>> disable2FA() {
        twoFactorService.disable2FA();
        return ResponseEntity.ok(ApiResponse.success("2FA disabled successfully", null));
    }

    /**
     * 查詢 2FA 狀態
     */
    @GetMapping("/2fa/status")
    public ResponseEntity<ApiResponse<Boolean>> get2FAStatus() {
        boolean enabled = userService.is2FAEnabled();
        return ResponseEntity.ok(ApiResponse.success(enabled));
    }
}
