package com.example.demo.admin;

import com.example.demo.admin.dto.AdminLoginRequest;
import com.example.demo.admin.dto.AdminLoginResponse;
import com.example.demo.admin.dto.AdminRegistrationRequest;
import com.example.demo.admin.dto.AdminResponse;
import com.example.demo.common.dto.ApiResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admins")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;
    private final AdminPasswordResetService adminPasswordResetService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody AdminLoginRequest request) {
        try {
            AdminLoginResponse response = adminService.login(request);
            return ResponseEntity.ok(ApiResponse.success("Admin login successful", response));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

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

    @GetMapping
    public ResponseEntity<?> getAllAdmins() {
        return ResponseEntity.ok(ApiResponse.success("Admins retrieved successfully", adminService.getAllAdmins()));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<AdminResponse>> getCurrentAdmin() {
        AdminResponse response = adminService.getCurrentAdminResponse();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(
            @Valid @RequestBody com.example.demo.auth.dto.ForgotPasswordRequest request) {
        adminPasswordResetService.initiatePasswordReset(request.getEmail());
        return ResponseEntity.ok(ApiResponse.success("Password reset email sent", null));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<Void>> resetPassword(
            @Valid @RequestBody com.example.demo.auth.dto.ResetPasswordRequest request) {
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new RuntimeException("Passwords do not match");
        }
        adminPasswordResetService.resetPassword(request.getToken(), request.getNewPassword());
        return ResponseEntity.ok(ApiResponse.success("Password reset successfully", null));
    }
}
