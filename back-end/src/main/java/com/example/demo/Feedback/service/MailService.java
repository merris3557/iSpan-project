package com.example.demo.Feedback.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class MailService {

    @Autowired
    private JavaMailSender mailSender;

    public void sendHtmlConfirmation(String toEmail, String ticketNumber, boolean isMember) {
        MimeMessage message = mailSender.createMimeMessage();

        try {
            // true 表示這是一封 multipart 郵件 (支援 HTML)
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom("afterrr5pm@gmail.com");
            helper.setTo(toEmail);
            helper.setSubject("【饗島】已收到您的回饋訊息 - " + ticketNumber);

            // 準備 HTML 內容
            String htmlContent = "<html>" +
                    "<body style='margin: 0; padding: 0; background-color: #f9f9f9; font-family: \"Helvetica Neue\", Helvetica, Arial, sans-serif;'>"
                    +
                    "  <table border='0' cellpadding='0' cellspacing='0' width='100%'>" +
                    "    <tr>" +
                    "      <td style='padding: 20px 0;'>" +
                    "        <table align='center' border='0' cellpadding='0' cellspacing='0' width='600' style='border-collapse: collapse; background-color: #ffffff; border: 1px solid #eeeeee;'>"
                    +
                    "          " +
                    "          <tr>" +
                    "            <td align='center' style='padding: 40px 0 20px 0; background-color: " + "#9f9572"
                    + ";'>" +
                    "              <img src='https://raw.githubusercontent.com/merris3557/Project_TL_picture/refs/heads/main/%E9%A5%97%E5%B3%B6.png' alt='饗島 Logo' width='120' style='display: block; '>"
                    + // filter: brightness(0) invert(1); 假設 Logo 是深色，這裡濾鏡轉白色
                    "            </td>" +
                    "          </tr>" +
                    "          " +
                    "          <tr>" +
                    "            <td style='padding: 40px 30px; background-color: " + "rgba(160, 150, 115, 0.05)"
                    + ";'>" +
                    "              <h2 style='color: " + "#776f54" + "; margin-top: 0;'>我們已收到您的回饋訊息</h2>" +
                    "              <p style='color: #555555; font-size: 16px; line-height: 1.6;'>" +
                    "                親愛的用戶您好，感謝您聯繫<strong>饗島 Taste Land</strong>。<br>" +
                    "                您的回饋訊息已成功受理，案件編號為：<strong style='color: " + "#9f9572" + ";'>" + ticketNumber
                    + "</strong>" +
                    "              </p>" +
                    "              <p style='color: #555555; font-size: 14px;'>" +
                    "                饗島客服團隊正全力處理您的訴求，我們將盡速回覆至此信箱與您聯繫。" +
                    "              </p>" +
                    "              </p>" +
                    (isMember
                            ? "              <table border='0' cellpadding='0' cellspacing='0' width='100%' style='margin-top: 30px;'>"
                                    +
                                    "                <tr>" +
                                    "                  <td align='center'>" +
                                    "                    <a href='http://localhost:5173/userInfo/feedback' " +
                                    "                       style='background-color: " + "#9f9572"
                                    + "; color: #ffffff; padding: 12px 30px; text-decoration: none; border-radius: 4px; font-weight: bold; display: inline-block;'>"
                                    +
                                    "                       查看處理進度" +
                                    "                    </a>" +
                                    "                  </td>" +
                                    "                </tr>" +
                                    "              </table>"
                            : "")
                    +
                    "            </td>" +
                    "          </tr>" +
                    "          " +
                    "          <tr>" +
                    "            <td style='padding: 20px 30px; background-color: #ffffff; text-align: center;'>" +
                    "              <p style='margin: 0; font-size: 12px; color: #999999;'>" +
                    "                &copy; 2026 饗島 Taste Land Project. All rights reserved.<br>" +
                    "                此為系統自動發送，請勿直接回覆。" +
                    "              </p>" +
                    "            </td>" +
                    "          </tr>" +
                    "        </table>" +
                    "      </td>" +
                    "    </tr>" +
                    "  </table>" +
                    "</body>" +
                    "</html>";

            helper.setText(htmlContent, true); // 第二個參數 true 代表這段字串是 HTML

            mailSender.send(message);

        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }

    public void replyNotification(String toEmail, String caseNumber, String replyContent, boolean isMember) {
        MimeMessage message = mailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom("afterrr5pm@gmail.com");
            helper.setTo(toEmail);
            helper.setSubject("【饗島】客服團隊：關於意見回饋「" + caseNumber + "」的處理進度 ");

            String htmlContent = "<html>" +
                    "<body style='margin: 0; padding: 0; background-color: #f9f9f9; font-family: Arial, sans-serif;'>" +
                    "  <table width='100%' border='0' cellpadding='0' cellspacing='0'>" +
                    "    <tr><td style='padding: 20px 0;'>" +
                    "      <table align='center' width='600' style='background-color: #ffffff; border: 1px solid #eeeeee; border-collapse: collapse;'>"
                    +
                    "        " +
                    "        <tr><td align='center' style='padding: 30px; background-color: #9f9572;'>" +
                    "          <img src='https://raw.githubusercontent.com/merris3557/Project_TL_picture/refs/heads/main/%E9%A5%97%E5%B3%B6.png' width='100' style='filter: brightness(0) invert(1);'>"
                    +
                    "        </td></tr>" +
                    "        " +
                    "        <tr><td style='padding: 40px 30px; background-color: rgba(160, 150, 115, 0.05);'>" +
                    "          <h2 style='color: #776f54;'>客服回覆通知</h2>" +
                    "          <p style='color: #555;'>親愛的用戶您好，關於您的回饋訊息 <strong>" + caseNumber
                    + "</strong>，客服人員已處理完畢：</p>" +
                    "          <div style='background: #ffffff; padding: 20px; border-left: 4px solid #9f9572; margin: 20px 0; color: #333; font-style: italic;'>"
                    +
                    "            " + replyContent + // 這裡放入管理員寫的回覆內容
                    "          </div>" +
                    "          <p style='font-size: 14px; color: #777;'>若您對回覆內容有任何疑問，歡迎再次聯繫我們。</p>" +
                    (isMember
                            ? "              <table border='0' cellpadding='0' cellspacing='0' width='100%' style='margin-top: 30px;'>"
                                    +
                                    "                <tr>" +
                                    "                  <td align='center'>" +
                                    "                    <a href='http://localhost:5173/userInfo/feedback' " +
                                    "                       style='background-color: #9f9572; color: #ffffff; padding: 12px 30px; text-decoration: none; border-radius: 4px; font-weight: bold; display: inline-block;'>"
                                    +
                                    "                       查看處理進度" +
                                    "                    </a>" +
                                    "                  </td>" +
                                    "                </tr>" +
                                    "              </table>"
                            : "")
                    +
                    "        </td></tr>" +
                    "        " +
                    "        <tr><td style='padding: 20px; text-align: center; font-size: 12px; color: #999;'>" +
                    "          &copy; 2026 饗島 Taste Land Project. All rights reserved.<br>" +
                    "                此為系統自動發送，請勿直接回覆。" +
                    "        </td></tr>" +
                    "      </table>" +
                    "    </td></tr>" +
                    "  </table>" +
                    "</body>" +
                    "</html>";

            helper.setText(htmlContent, true);
            mailSender.send(message);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }

    @Async
    public void sendBookingNotification(String toEmail, String guestName, String storeName,
            String date, String time, int guestCount, String type) {
        jakarta.mail.internet.MimeMessage mimeMessage = mailSender.createMimeMessage();

        // 強制轉型成字串（雖然 String.format 支援 %d，但轉型後比較保險）
        String countStr = String.valueOf(guestCount);

        try {
            org.springframework.mail.javamail.MimeMessageHelper helper = new org.springframework.mail.javamail.MimeMessageHelper(
                    mimeMessage, "UTF-8");

            helper.setFrom("afterrr5pm@gmail.com");
            helper.setTo(toEmail);

            // 根據 type 決定主旨與標題
            String subjectTag;
            String headMessage;
            String color; // 增加顏色區分，取消用紅色，更新用藍色

            switch (type) {
                case "UPDATE":
                    subjectTag = "【訂位變更通知】";
                    headMessage = "您的訂位資訊已成功更新";
                    color = "#2196F3";
                    break;
                case "CANCEL":
                    subjectTag = "【訂位取消確認】";
                    headMessage = "您的訂位已成功取消";
                    color = "#F44336";
                    break;
                default:
                    subjectTag = "【訂位成功確認】";
                    headMessage = "您的訂位已完成";
                    color = "#4CAF50";
            }

            helper.setSubject(subjectTag + storeName);

            String htmlContent = String.format(
                    "<html>" +
                    "<body style='margin: 0; padding: 0; background-color: #f9f9f9; font-family: Arial, sans-serif;'>" +
                    "  <table width='100%%' border='0' cellpadding='0' cellspacing='0'>" +
                    "    <tr><td style='padding: 20px 0;'>" +
                    "      <table align='center' width='600' style='background-color: #ffffff; border: 1px solid #eeeeee; border-collapse: collapse;'>" +
                    "        <tr><td align='center' style='padding: 30px; background-color: #9f9572;'>" +
                    "          <img src='https://raw.githubusercontent.com/merris3557/Project_TL_picture/refs/heads/main/%%E9%%A5%%97%%E5%%B3%%B6.png' width='100' style='filter: brightness(0) invert(1);'>" +
                    "        </td></tr>" +
                    "        <tr><td style='padding: 40px 30px; background-color: rgba(160, 150, 115, 0.05);'>" +
                    "          <h2 style='color: %s; margin-top: 0;'>%s</h2>" +
                    "          <p style='color: #555; font-size: 16px; line-height: 1.6;'>親愛的 <strong>%s</strong> 您好：<br>以下是您的預約明細：</p>" +
                    "          <div style='background: #ffffff; padding: 20px; border-left: 4px solid %s; margin: 20px 0; color: #333; line-height: 1.8;'>" +
                    "            <strong>餐廳名稱：</strong> %s<br>" +
                    "            <strong>訂位日期：</strong> %s<br>" +
                    "            <strong>訂位時間：</strong> %s<br>" +
                    "            <strong>用餐人數：</strong> %s 人" +
                    "          </div>" +
                    "          <p style='font-size: 14px; color: #777; margin-bottom: 0;'>如有任何問題，請洽餐廳專線。期待再次為您服務！</p>" +
                    "        </td></tr>" +
                    "        <tr><td style='padding: 20px; text-align: center; font-size: 12px; color: #999;'>" +
                    "          &copy; 2026 饗島 Taste Land Project. All rights reserved.<br>此為系統自動發送，請勿直接回覆。" +
                    "        </td></tr>" +
                    "      </table>" +
                    "    </td></tr>" +
                    "  </table>" +
                    "</body>" +
                    "</html>",
                    color, headMessage, guestName, color, storeName, date, time, countStr);

            helper.setText(htmlContent, true);
            mailSender.send(mimeMessage);
            System.out.println(">>>> [" + type + "] 郵件已發送至 " + toEmail);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
