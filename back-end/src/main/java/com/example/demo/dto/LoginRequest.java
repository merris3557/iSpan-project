package com.example.demo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {

    // 可以是 email 或 username
    @NotBlank(message = "Email or username is required")
    private String identifier;

    @NotBlank(message = "Password is required")
    private String password;

    // 雙因素認證碼 (6 位數字，若用戶啟用 2FA 則必填)
    @Pattern(regexp = "\\d{6}", message = "2FA code must be 6 digits")
    private String twoFactorCode;
}
