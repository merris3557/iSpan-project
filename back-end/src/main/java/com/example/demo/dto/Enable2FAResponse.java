package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 啟用 2FA 回應 DTO
 * 包含 QR code 圖片和密鑰
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Enable2FAResponse {

    private String qrCodeImage; // Base64 編碼的 QR code 圖片
    private String secret; // 密鑰 (可手動輸入到 Google Authenticator)
    private String issuer; // 發行者名稱
    private String accountName; // 帳號名稱 (通常是 email)
}
