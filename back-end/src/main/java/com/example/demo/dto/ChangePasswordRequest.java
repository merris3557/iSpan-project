package com.example.demo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 修改密碼請求 DTO
 */
@Data
public class ChangePasswordRequest {

    @NotBlank(message = "Current password is required")
    private String currentPassword;

    @NotBlank(message = "New password is required")
    @Size(min = 6, max = 100, message = "Password must be between 6 and 100 characters")
    private String newPassword;

    @NotBlank(message = "Password confirmation is required")
    private String confirmPassword;
}
