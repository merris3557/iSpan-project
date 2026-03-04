package com.example.demo.admin;

import com.example.demo.common.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 管理員密碼重設服務
 */
@Service
@RequiredArgsConstructor
public class AdminPasswordResetService {

    private final AdminRepository adminRepository;
    private final AdminPasswordResetTokenRepository tokenRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    // 從 application.properties 讀取 token 過期時間 (分鐘)
    @Value("${app.password-reset-token-expiration-minutes:60}")
    private int tokenExpirationMinutes;

    /**
     * 發起管理員密碼重設請求
     * 生成 token 並發送郵件
     */
    @Transactional
    public void initiatePasswordReset(String email) {
        // 利用 email 尋找 admin（若 AdminRepository 尚無 findByEmail，後續會補上）
        Admin admin = adminRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Admin not found with email: " + email));

        // 刪除該管理員之前的重設 token
        tokenRepository.findByAdmin(admin).ifPresent(tokenRepository::delete);

        // 生成新的 token
        String token = UUID.randomUUID().toString();
        LocalDateTime expiryDate = LocalDateTime.now().plusMinutes(tokenExpirationMinutes);

        AdminPasswordResetToken resetToken = AdminPasswordResetToken.builder()
                .token(token)
                .admin(admin)
                .expiryDate(expiryDate)
                .used(false)
                .build();

        tokenRepository.save(resetToken);

        // 發送郵件
        emailService.sendAdminPasswordResetEmail(email, token);
    }

    /**
     * 驗證並重設密碼
     */
    @Transactional
    public void resetPassword(String token, String newPassword) {
        AdminPasswordResetToken resetToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid password reset token"));

        // 檢查是否已使用
        if (resetToken.getUsed()) {
            throw new RuntimeException("Password reset token has already been used");
        }

        // 檢查是否過期
        if (resetToken.isExpired()) {
            throw new RuntimeException("Password reset token has expired");
        }

        // 重設密碼
        Admin admin = resetToken.getAdmin();
        admin.setPassword(passwordEncoder.encode(newPassword));
        adminRepository.save(admin);

        // 標記 token 為已使用
        resetToken.setUsed(true);
        tokenRepository.save(resetToken);
    }

    /**
     * 清理過期的 token (可設定排程定期執行)
     */
    @Transactional
    public void cleanupExpiredTokens() {
        tokenRepository.deleteExpiredTokens(LocalDateTime.now());
    }
}
