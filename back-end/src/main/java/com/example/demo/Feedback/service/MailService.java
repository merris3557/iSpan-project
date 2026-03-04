package com.example.demo.Feedback.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class MailService {

    @Autowired
    private JavaMailSender mailSender;

    public void sendHtmlConfirmation(String toEmail, String ticketNumber) {
        MimeMessage message = mailSender.createMimeMessage();

        try {
            // true 表示這是一封 multipart 郵件 (支援 HTML)
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom("afterrr5pm@gmail.com");
            helper.setTo(toEmail);
            helper.setSubject("【饗島】已收到您的回饋反映 - " + ticketNumber);

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
                    "              <img src='https://github.com/merris3557/iSpan-project/blob/47c4ceb5de5d816e6f0e62f2f791da15e52de7b7/front-end/src/pictures/TL_Logo_forMail.png' alt='饗島 Logo' width='120' style='display: block; '>"
                    + // filter: brightness(0) invert(1); 假設 Logo 是深色，這裡濾鏡轉白色
                    "            </td>" +
                    "          </tr>" +
                    "          " +
                    "          <tr>" +
                    "            <td style='padding: 40px 30px; background-color: " + "rgba(160, 150, 115, 0.05)"
                    + ";'>" +
                    "              <h2 style='color: " + "#776f54" + "; margin-top: 0;'>我們已收到您的訊息</h2>" +
                    "              <p style='color: #555555; font-size: 16px; line-height: 1.6;'>" +
                    "                親愛的用戶您好，感謝您聯繫<strong>饗島 Taste Land</strong>。<br>" +
                    "                您的回饋反映已成功受理，案件編號為：<strong style='color: " + "#9f9572" + ";'>" + ticketNumber
                    + "</strong>" +
                    "              </p>" +
                    "              <p style='color: #555555; font-size: 14px;'>" +
                    "                我們的客服團隊將於 48 小時內進行核實並回覆至此信箱。感謝您的耐心等待。" +
                    "              </p>" +
                    "              " +
                    "              <table border='0' cellpadding='0' cellspacing='0' width='100%' style='margin-top: 30px;'>"
                    +
                    "                <tr>" +
                    "                  <td align='center'>" +
                    "                    <a href='http://localhost:8081/complaint-status/" + ticketNumber + "' " +
                    "                       style='background-color: " + "#9f9572"
                    + "; color: #ffffff; padding: 12px 30px; text-decoration: none; border-radius: 4px; font-weight: bold; display: inline-block;'>"
                    +
                    "                       查看處理進度" +
                    "                    </a>" +
                    "                  </td>" +
                    "                </tr>" +
                    "              </table>" +
                    "            </td>" +
                    "          </tr>" +
                    "          " +
                    "          <tr>" +
                    "            <td style='padding: 20px 30px; background-color: #ffffff; text-align: center;'>" +
                    "              <p style='margin: 0; font-size: 12px; color: #999999;'>" +
                    "                &copy; 2024 饗島 Taste Land Project. All rights reserved.<br>" +
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

    public void replyNotification(String toEmail, String caseNumber, String replyContent) {
        MimeMessage message = mailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom("afterrr5pm@gmail.com");
            helper.setTo(toEmail);
            helper.setSubject("【饗島】您的回饋反映已有回覆 - " + caseNumber);

            String htmlContent = "<html>" +
                    "<body style='margin: 0; padding: 0; background-color: #f9f9f9; font-family: Arial, sans-serif;'>" +
                    "  <table width='100%' border='0' cellpadding='0' cellspacing='0'>" +
                    "    <tr><td style='padding: 20px 0;'>" +
                    "      <table align='center' width='600' style='background-color: #ffffff; border: 1px solid #eeeeee; border-collapse: collapse;'>"
                    +
                    "        " +
                    "        <tr><td align='center' style='padding: 30px; background-color: #9f9572;'>" +
                    "          <img src='你的Logo網址' width='100' style='filter: brightness(0) invert(1);'>" +
                    "        </td></tr>" +
                    "        " +
                    "        <tr><td style='padding: 40px 30px; background-color: rgba(160, 150, 115, 0.05);'>" +
                    "          <h2 style='color: #776f54;'>客服回覆通知</h2>" +
                    "          <p style='color: #555;'>親愛的用戶您好，關於您的案件 <strong>" + caseNumber
                    + "</strong>，客服人員已處理完畢：</p>" +
                    "          <div style='background: #ffffff; padding: 20px; border-left: 4px solid #9f9572; margin: 20px 0; color: #333; font-style: italic;'>"
                    +
                    "            " + replyContent + // 這裡放入管理員寫的回覆內容
                    "          </div>" +
                    "          <p style='font-size: 14px; color: #777;'>若您對回覆內容有任何疑問，歡迎再次聯繫我們。</p>" +
                    "        </td></tr>" +
                    "        " +
                    "        <tr><td style='padding: 20px; text-align: center; font-size: 12px; color: #999;'>" +
                    "          &copy; 2026 饗島 Taste Land. 此為系統自動發送。" +
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
}
