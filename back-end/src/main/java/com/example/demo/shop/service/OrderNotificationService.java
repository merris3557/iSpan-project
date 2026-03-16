package com.example.demo.shop.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.shop.entity.OrderDetails;
import com.example.demo.shop.entity.Orders;
import com.example.demo.shop.repository.OrdersRepository;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class OrderNotificationService {

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private OrdersRepository ordersRepository;

    private final String fromEmail = "afterrr5pm@gmail.com";
    private final String primaryColor = "#9f9572";
    private final String lightBackground = "rgba(160, 150, 115, 0.05)";

    /**
     * 發送訂單通知郵件（訂單未付款狀態）
     */
    @Async
    @Transactional
    public void sendOrderNotification(Orders order) {
        try {
            // 重新查詢完整的訂單及其詳情，確保 orderDetails 被加載
            Orders completeOrder = ordersRepository.findById(order.getId())
                .orElse(order);
            
            // 强制加载 orderDetails
            if (completeOrder.getOrderDetails() != null) {
                completeOrder.getOrderDetails().size();
            }
            
            String toEmail = completeOrder.getUser().getEmail();
            if (toEmail == null || toEmail.isEmpty()) {
                return;
            }
            
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("【訂單確認】您的訂單已收到 - 訂單號：" + completeOrder.getMerchantTradeNo());

            String htmlContent = buildOrderNotificationHtml(completeOrder);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("✓ 訂單通知郵件已發送至：{}，訂單編號：{}", toEmail, completeOrder.getMerchantTradeNo());

        } catch (MessagingException e) {
            log.error("❌ 發送訂單通知郵件失敗 - 郵件異常：", e);
        } catch (Exception e) {
            log.error("❌ 發送訂單通知郵件發生錯誤：", e);
        }
    }

    /**
     * 發送支付成功通知郵件
     */
    @Async
    @Transactional
    public void sendPaymentSuccessNotification(Orders order) {
        try {
            // 重新查詢完整的訂單及其詳情，確保 orderDetails 被加載
            Orders completeOrder = ordersRepository.findById(order.getId())
                .orElse(order);
            
            // 强制加载 orderDetails
            if (completeOrder.getOrderDetails() != null) {
                completeOrder.getOrderDetails().size();
            }
            
            String toEmail = completeOrder.getUser().getEmail();
            if (toEmail == null || toEmail.isEmpty()) {
                return;
            }
            
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("【支付成功】您的訂單已付款 - 訂單號：" + completeOrder.getMerchantTradeNo());

            String htmlContent = buildPaymentSuccessHtml(completeOrder);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("✓ 支付成功通知郵件已發送至：{}，訂單編號：{}", toEmail, completeOrder.getMerchantTradeNo());

        } catch (MessagingException e) {
            log.error("❌ 發送支付成功郵件失敗 - 郵件異常：", e);
        } catch (Exception e) {
            log.error("❌ 發送支付成功郵件發生錯誤：", e);
        }
    }

    /**
     * 構建訂單通知郵件HTML內容
     */
    private String buildOrderNotificationHtml(Orders order) {
        StringBuilder html = new StringBuilder();
        html.append("<html>")
            .append("<head><meta charset='UTF-8'></head>")
            .append("<body style='margin: 0; padding: 0; background-color: #f9f9f9; font-family: \"Helvetica Neue\", Helvetica, Arial, sans-serif;'>")
            .append("<table border='0' cellpadding='0' cellspacing='0' width='100%'>")
            .append("<tr><td style='padding: 20px 0;'>")
            
            // 主容器
            .append("<table align='center' border='0' cellpadding='0' cellspacing='0' width='600' style='border-collapse: collapse; background-color: #ffffff; border: 1px solid #eeeeee;'>")
            
            // 頭部
            .append("<tr>")
            .append("<td align='center' style='padding: 30px 0; background-color: ").append(primaryColor).append(";'>")
            .append("<h1 style='color: #ffffff; margin: 0; font-size: 24px;'>訂單確認</h1>")
            .append("</td>")
            .append("</tr>")
            
            // 主內容
            .append("<tr>")
            .append("<td style='padding: 40px 30px; background-color: ").append(lightBackground).append(";'>")
            
            // 歡迎信息
            .append("<h2 style='color: #776f54; margin-top: 0;'>親愛的 ").append(order.getReceiverName()).append(" 您好</h2>")
            .append("<p style='color: #555555; font-size: 16px; line-height: 1.6;'>")
            .append("感謝您的訂購！我們已收到您的訂單，以下是訂單詳情：")
            .append("</p>")
            
            // 訂單信息卡片
            .append("<div style='background: #ffffff; padding: 20px; margin: 20px 0; border-left: 4px solid ").append(primaryColor).append("; border-radius: 4px;'>")
            .append("<p style='margin: 10px 0; color: #333;'><strong>訂單號碼：</strong> ").append(order.getMerchantTradeNo()).append("</p>")
            .append("<p style='margin: 10px 0; color: #333;'><strong>訂單日期：</strong> ").append(order.getCreatedAt()).append("</p>")
            .append("</div>")
            
            // 商品清單
            .append("<h3 style='color: ").append(primaryColor).append("; margin: 25px 0 15px 0;'>商品清單</h3>")
            .append("<table width='100%' border='1' cellpadding='10' cellspacing='0' style='border-collapse: collapse; border-color: #e0e0e0;'>")
            .append("<tr style='background-color: ").append(lightBackground).append(";'>")
            .append("<th align='left' style='padding: 12px; color: ").append(primaryColor).append("; font-weight: bold;'>商品名稱</th>")
            .append("<th align='center' style='padding: 12px; color: ").append(primaryColor).append("; font-weight: bold;'>數量</th>")
            .append("<th align='right' style='padding: 12px; color: ").append(primaryColor).append("; font-weight: bold;'>單價</th>")
            .append("<th align='right' style='padding: 12px; color: ").append(primaryColor).append("; font-weight: bold;'>小計</th>")
            .append("</tr>");
            
            // 添加訂單項目
            List<OrderDetails> details = order.getOrderDetails();
            if (details != null && !details.isEmpty()) {
                for (OrderDetails detail : details) {
                    html.append("<tr>")
                        .append("<td style='padding: 12px; color: #555;'>").append(detail.getProductNameSnapshot()).append("</td>")
                        .append("<td align='center' style='padding: 12px; color: #555;'>").append(detail.getOrderQuantity()).append("</td>")
                        .append("<td align='right' style='padding: 12px; color: #555;'>NT$ ").append(detail.getPriceSnapshot()).append("</td>")
                        .append("<td align='right' style='padding: 12px; color: #555; font-weight: bold;'>NT$ ").append(detail.getSubtotal()).append("</td>")
                        .append("</tr>");
                }
            }
            
            html.append("</table>");
            
            // 配送信息
            html.append("<h3 style='color: ").append(primaryColor).append("; margin: 25px 0 15px 0;'>配送信息</h3>")
            .append("<div style='background: #f0f0f0; padding: 15px; border-radius: 4px;'>")
            .append("<p style='margin: 8px 0; color: #333;'><strong>收件人：</strong> ").append(order.getReceiverName()).append("</p>")
            .append("<p style='margin: 8px 0; color: #333;'><strong>聯絡電話：</strong> ").append(maskPhone(order.getReceiverPhone())).append("</p>")
            .append("<p style='margin: 8px 0; color: #333;'><strong>配送方式：</strong> ").append(sanitizeDeliveryMethod(order.getDeliveryMethod())).append("</p>")
            .append("<p style='margin: 8px 0; color: #333;'><strong>配送地址：</strong> ").append(maskAddress(order.getReceiverAddress())).append("</p>")
            .append("</div>");
            
            // 費用總結
            html.append("<div style='background: ").append(lightBackground).append("; padding: 15px; margin: 20px 0; border-radius: 4px;'>")
            .append("<p style='margin: 8px 0; text-align: right; color: #555;'><strong>總金額：NT$ ").append(order.getTotalPrice()).append("</strong></p>")
            .append("</div>")
            
            // 付款狀態
            .append("<div style='background: #fff3cd; padding: 15px; border-left: 4px solid #ffc107; border-radius: 4px; margin: 20px 0;'>")
            .append("<p style='margin: 5px 0; color: #856404;'><strong>付款狀態：</strong> ").append(order.getStatus()).append("</p>")
            .append("<p style='margin: 5px 0; color: #856404; font-size: 14px;'>請盡快完成付款以便及時處理您的訂單。</p>")
            .append("</div>")
            
            // 結尾
            .append("<p style='color: #777; font-size: 14px; margin-top: 20px;'>")
            .append("如有任何問題，歡迎聯繫我們的客服團隊。")
            .append("</p>")
            
            .append("</td>")
            .append("</tr>")
            
            // 底部
            .append("<tr>")
            .append("<td style='padding: 20px 30px; background-color: #ffffff; text-align: center; border-top: 1px solid #eeeeee;'>")
            .append("<p style='margin: 0; font-size: 12px; color: #999999;'>")
            .append("&copy; 2026 Taste Land Project. All rights reserved.<br>")
            .append("此為系統自動發送，請勿直接回覆。")
            .append("</p>")
            .append("</td>")
            .append("</tr>")
            
            .append("</table>")
            .append("</td></tr></table>")
            .append("</body>")
            .append("</html>");
            
            return html.toString();
    }

    /**
     * 構建支付成功郵件HTML內容
     */
    private String buildPaymentSuccessHtml(Orders order) {
        StringBuilder html = new StringBuilder();
        html.append("<html>")
            .append("<head><meta charset='UTF-8'></head>")
            .append("<body style='margin: 0; padding: 0; background-color: #f9f9f9; font-family: \"Helvetica Neue\", Helvetica, Arial, sans-serif;'>")
            .append("<table border='0' cellpadding='0' cellspacing='0' width='100%'>")
            .append("<tr><td style='padding: 20px 0;'>")
            
            // 主容器
            .append("<table align='center' border='0' cellpadding='0' cellspacing='0' width='600' style='border-collapse: collapse; background-color: #ffffff; border: 1px solid #eeeeee;'>")
            
            // 頭部 - 成功風格
            .append("<tr>")
            .append("<td align='center' style='padding: 30px 0; background-color: ").append(primaryColor).append(";'>")
            .append("<h1 style='color: #ffffff; margin: 0; font-size: 24px;'>✓ 支付成功</h1>")
            .append("</td>")
            .append("</tr>")
            
            // 主內容
            .append("<tr>")
            .append("<td style='padding: 40px 30px; background-color: ").append(lightBackground).append(";'>")
            
            // 歡迎信息
            .append("<h2 style='color: #776f54; margin-top: 0;'>親愛的 ").append(order.getReceiverName()).append(" 您好</h2>")
            .append("<p style='color: #555555; font-size: 16px; line-height: 1.6;'>")
            .append("恭喜！您的訂單支付已成功完成。我們將儘快為您安排配送，以下是您的訂單摘要：")
            .append("</p>")
            
            // 訂單信息卡片
            .append("<div style='background: #f0f0f0; padding: 20px; margin: 20px 0; border-left: 4px solid #856404; border-radius: 4px;'>")
            .append("<p style='margin: 10px 0; color: #333;'><strong>訂單號碼：</strong> ").append(order.getMerchantTradeNo()).append("</p>")
            .append("<p style='margin: 10px 0; color: #333;'><strong>支付日期：</strong> ").append(order.getPaymentDate()).append("</p>")
            .append("</div>")
            
            // 商品簡列
            .append("<h3 style='color: ").append(primaryColor).append("; margin: 25px 0 15px 0;'>訂單商品</h3>");
            
            List<OrderDetails> details = order.getOrderDetails();
            if (details != null && !details.isEmpty()) {
                for (OrderDetails detail : details) {
                    html.append("<p style='margin: 8px 0; color: #555;'>")
                        .append("<strong>").append(detail.getProductNameSnapshot()).append("</strong> x ")
                        .append(detail.getOrderQuantity()).append(" = NT$ ")
                        .append(detail.getSubtotal())
                        .append("</p>");
                }
            }
            
            // 費用總結
            html.append("<div style='background: #ffffff; padding: 15px; margin: 20px 0; border-top: 2px solid ").append(primaryColor).append(";'>")
            .append("<p style='margin: 10px 0; text-align: right; color: #333; font-size: 18px;'><strong>總金額：NT$ ").append(order.getTotalPrice()).append("</strong></p>")
            .append("</div>")
            
            // 配送信息簡要
            .append("<div style='background: #e8f5e9; padding: 15px; border-radius: 4px; margin: 20px 0;'>")
            .append("<p style='margin: 5px 0; color: #2e7d32;'><strong>配送方式：</strong> ").append(sanitizeDeliveryMethod(order.getDeliveryMethod())).append("</p>")
            .append("<p style='margin: 5px 0; color: #2e7d32; font-size: 14px;'>配送至：").append(maskAddress(order.getReceiverAddress())).append("</p>")
            .append("</div>")
            
            // 成功消息
            .append("<div style='background: #fef8e7; padding: 15px; border-left: 4px solid #ffc107; border-radius: 4px; margin: 20px 0;'>")
            .append("<p style='margin: 5px 0; color: #856404;'><strong>感謝您的購買！</strong></p>")
            .append("<p style='margin: 5px 0; color: #856404; font-size: 14px;'>我們已開始準備您的訂單，稍後將通知您配送狀態。</p>")
            .append("</div>")
            
            // 結尾
            .append("<p style='color: #777; font-size: 14px; margin-top: 20px;'>")
            .append("如有任何問題或需要協助，請隨時聯繫我們。")
            .append("</p>")
            
            .append("</td>")
            .append("</tr>")
            
            // 底部
            .append("<tr>")
            .append("<td style='padding: 20px 30px; background-color: #ffffff; text-align: center; border-top: 1px solid #eeeeee;'>")
            .append("<p style='margin: 0; font-size: 12px; color: #999999;'>")
            .append("&copy; 2026 Taste Land Project. All rights reserved.<br>")
            .append("此為系統自動發送，請勿直接回覆。")
            .append("</p>")
            .append("</td>")
            .append("</tr>")
            
            .append("</table>")
            .append("</td></tr></table>")
            .append("</body>")
            .append("</html>");
            
            return html.toString();
    }

    /**
     * 遮蔽地址 - 只顯示縣市+行政區+前幾個字
     */
    private String maskAddress(String address) {
        if (address == null || address.isEmpty()) {
            return "未提供";
        }
        
        // 假設格式為：城市+區域+街道
        // 只取前9個字（約縣市+區+部分街道）
        if (address.length() > 9) {
            return address.substring(0, 9) + "***";
        }
        return address;
    }

    private String maskPhone(String phone) {
        if (phone == null || phone.isEmpty()) {
            return "未提供";
        }
        
        
        if (phone.length() > 5) {
            return phone.substring(0, 5) + "***";
        }
        return phone;
    }

    /**
     * 配送方式名稱
     */
    private String sanitizeDeliveryMethod(String method) {
        if (method == null || method.isEmpty()) {
            return "宅配";
        }
        
        return switch (method.toLowerCase()) {
            case "home" -> "宅配";
            case "store" -> "超商取貨";
            default -> method;
        };
    }
}
