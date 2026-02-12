package com.example.demo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 驗證 2FA 請求 DTO
 */
@Data
public class Verify2FARequest {

    @NotBlank(message = "Verification code is required")
    @Pattern(regexp = "\\d{6}", message = "Code must be 6 digits")
    private String code; // 6 位數的 TOTP 驗證碼
}
