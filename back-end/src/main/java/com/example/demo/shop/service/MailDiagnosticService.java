package com.example.demo.shop.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.shop.entity.Orders;
import com.example.demo.shop.repository.OrdersRepository;

import lombok.extern.slf4j.Slf4j;

/**
 * 郵件服務診斷工具
 * 用於檢查郵件發送是否正常運行
 */
@RestController
@RequestMapping("/api/mail-test")
@Slf4j
public class MailDiagnosticService {

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Autowired(required = false)
    private OrderNotificationService orderNotificationService;

    @Autowired(required = false)
    private OrdersRepository ordersRepository;

    /**
     * 檢查郵件配置是否正確
     * 訪問: http://localhost:8080/api/mail-test/check-config
     */
    @GetMapping("/check-config")
    public String checkEmailConfig() {
        StringBuilder result = new StringBuilder();

        if (mailSender == null) {
            result.append("❌ JavaMailSender 未被正確注入\n");
        } else {
            result.append("✓ JavaMailSender 已正確注入\n");
        }

        if (orderNotificationService == null) {
            result.append("❌ OrderNotificationService 未被注入\n");
        } else {
            result.append("✓ OrderNotificationService 已正確注入\n");
        }

        return result.toString();
    }

    /**
     * 簡單郵件測試 - 同步發送
     * 訪問: http://localhost:8080/api/mail-test/send-simple?to=test@example.com
     */
    @GetMapping("/send-simple")
    public String sendSimpleTestEmail(@RequestParam String to) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("afterrr5pm@gmail.com");
            message.setTo(to);
            message.setSubject("【測試】iSpan 郵件服務測試");
            message.setText("親愛的用戶您好，\n\n" +
                "這是一封測試郵件，用於驗證郵件服務是否正常運作。\n\n" +
                "如果您收到這封郵件，表示郵件配置正確。\n\n" +
                "最佳祝願，\n" +
                "iSpan 系統");
            
            mailSender.send(message);
            return "✓ 簡單郵件已成功發送至：" + to;
            
        } catch (Exception e) {
            return "❌ 簡單郵件發送失敗：" + e.getMessage();
        }
    }

    /**
     * 測試訂單郵件 - 為某個訂單重新發送郵件
     * 訪問: http://localhost:8080/api/mail-test/resend-order-mail?merchantTradeNo=ORD1773143757339
     */
    @GetMapping("/resend-order-mail")
    public String resendOrderMail(@RequestParam String merchantTradeNo) {
        try {
            Orders order = ordersRepository.findByMerchantTradeNo(merchantTradeNo)
                .orElseThrow(() -> new RuntimeException("找不到訂單"));
            
            if (order.getUser() == null) {
                return "❌ 訂單沒有關聯用戶";
            }
            
            String userEmail = order.getUser().getEmail();
            if (userEmail == null || userEmail.isEmpty()) {
                return "❌ 用戶沒有信箱";
            }
            
            orderNotificationService.sendOrderNotification(order);
            return "✓ 郵件已發送至：" + userEmail;
            
        } catch (Exception e) {
            return "❌ 郵件發送失敗：" + e.getMessage();
        }
    }


}
