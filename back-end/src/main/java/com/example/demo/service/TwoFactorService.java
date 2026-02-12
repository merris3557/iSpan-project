package com.example.demo.service;

import com.example.demo.dto.Enable2FAResponse;
import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorKey;
import com.warrenstrange.googleauth.GoogleAuthenticatorQRGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.util.Base64;

/**
 * 雙因素認證 (2FA) 服務
 * 使用 Google Authenticator TOTP 演算法
 */
@Service
@RequiredArgsConstructor
public class TwoFactorService {

    private final UserRepository userRepository;
    private final GoogleAuthenticator googleAuthenticator = new GoogleAuthenticator();

    // 從 application.properties 讀取應用名稱 (顯示在 Google Authenticator)
    @Value("${app.2fa.issuer:iSpan Project}")
    private String issuer;

    /**
     * 為當前用戶生成 2FA 密鑰和 QR code
     */
    @Transactional
    public Enable2FAResponse generate2FASecret() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 如果已經啟用 2FA，拋出異常
        if (Boolean.TRUE.equals(user.getTwoFactorEnabled())) {
            throw new RuntimeException("2FA is already enabled for this user");
        }

        // 生成新的密鑰
        GoogleAuthenticatorKey credentials = googleAuthenticator.createCredentials();
        String secret = credentials.getKey();

        // 暫存密鑰到用戶資料 (尚未啟用)
        user.setTwoFactorSecret(secret);
        userRepository.save(user);

        // 生成 QR code URL
        String qrUrl = GoogleAuthenticatorQRGenerator.getOtpAuthTotpURL(
                issuer,
                email,
                credentials);

        // 生成 QR code 圖片 (Base64)
        String qrCodeImage = generateQRCodeImage(qrUrl);

        return Enable2FAResponse.builder()
                .qrCodeImage(qrCodeImage)
                .secret(secret)
                .issuer(issuer)
                .accountName(email)
                .build();
    }

    /**
     * 驗證 TOTP 驗證碼並啟用 2FA
     */
    @Transactional
    public void verifyAndEnable2FA(String code) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getTwoFactorSecret() == null) {
            throw new RuntimeException("2FA secret not generated. Please generate it first.");
        }

        // 驗證碼
        if (!verify2FACode(user.getTwoFactorSecret(), code)) {
            throw new RuntimeException("Invalid verification code");
        }

        // 啟用 2FA
        user.setTwoFactorEnabled(true);
        userRepository.save(user);
    }

    /**
     * 停用 2FA
     */
    @Transactional
    public void disable2FA() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setTwoFactorEnabled(false);
        user.setTwoFactorSecret(null);
        userRepository.save(user);
    }

    /**
     * 驗證 2FA 驗證碼
     */
    public boolean verify2FACode(String secret, String code) {
        try {
            int codeInt = Integer.parseInt(code);
            return googleAuthenticator.authorize(secret, codeInt);
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * 生成 QR code 圖片 (Base64 編碼)
     */
    private String generateQRCodeImage(String text) {
        try {
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, 300, 300);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", outputStream);
            byte[] imageBytes = outputStream.toByteArray();

            return "data:image/png;base64," + Base64.getEncoder().encodeToString(imageBytes);
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate QR code", e);
        }
    }
}
