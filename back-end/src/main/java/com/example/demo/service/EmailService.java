package com.example.demo.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * Email 發送服務
 * 用於發送密碼重設郵件等通知
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    // 從 application.properties 讀取發件人地址
    @Value("${spring.mail.username}")
    private String fromEmail;

    /**
     * 發送密碼重設郵件
     * 
     * @param toEmail    收件人郵箱
     * @param resetToken 重設 token
     */
    public void sendPasswordResetEmail(String toEmail, String resetToken) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Password Reset Request - iSpan Project");

            // TODO: 實際使用時請修改前端 URL
            String resetUrl = "http://localhost:3000/reset-password?token=" + resetToken;

            message.setText(String.format(
                    "Hello,\n\n" +
                            "You have requested to reset your password.\n\n" +
                            "Please click the link below to reset your password:\n%s\n\n" +
                            "This link will expire in 1 hour.\n\n" +
                            "If you didn't request this, please ignore this email.\n\n" +
                            "Best regards,\niSpan Project Team",
                    resetUrl));

            mailSender.send(message);
            log.info("Password reset email sent to: {}", toEmail);

        } catch (Exception e) {
            log.error("Failed to send password reset email to: {}", toEmail, e);
            throw new RuntimeException("Failed to send email", e);
        }
    }
}
