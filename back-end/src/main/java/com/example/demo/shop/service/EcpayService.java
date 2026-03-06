package com.example.demo.shop.service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.TreeMap;

import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.stereotype.Service;

@Service
public class EcpayService {

    private static final String MERCHANT_ID = "3002607";
    private static final String HASH_KEY = "pwFHCqoQZGmho4w6";
    private static final String HASH_IV = "EkRm7iFT261dpevs";
    private static final String ECPAY_URL = "https://payment-stage.ecpay.com.tw/Cashier/AioCheckOut/V5";
    private static final String NGROK_URL = "https://shily-untusked-yuri.ngrok-free.dev";

    public String generatePaymentForm(Integer orderId, int finalTotalPrice, String itemName, String merchantTradeNo) {
        String merchantTradeDate = LocalDateTime.now()
            .format(DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss")); 
        

        TreeMap<String, String> params = new TreeMap<>();
        params.put("MerchantID", MERCHANT_ID);
        params.put("MerchantTradeNo", merchantTradeNo);
        params.put("MerchantTradeDate", merchantTradeDate);
        params.put("PaymentType", "aio");
        params.put("TotalAmount", String.valueOf(finalTotalPrice));
        params.put("TradeDesc", "Shopping");
        params.put("ItemName", "Order" + orderId);
        params.put("ReturnURL", NGROK_URL + "/api/ecpay/return");
        params.put("ClientBackURL", "http://localhost:5173/payment-result");
        params.put("OrderResultURL", NGROK_URL + "/api/ecpay/result");
        params.put("ChoosePayment", "ALL");
        params.put("EncryptType", "1");

        String checkMac = generateCheckMac(params);
        params.put("CheckMacValue", checkMac);

        // 產生 HTML form
        StringBuilder form = new StringBuilder();
        form.append("<form id='ecpayForm' method='post' action='").append(ECPAY_URL).append("'>");
        for (var entry : params.entrySet()) {
            form.append("<input type='hidden' name='").append(entry.getKey())
                .append("' value='").append(entry.getValue()).append("'/>");
        }
        form.append("</form>");

        return form.toString(); 

    }

    private String generateCheckMac(TreeMap<String, String> params) {
        try {
            // 步驟1: 組合字串
            StringBuilder sb = new StringBuilder();
            sb.append("HashKey=").append(HASH_KEY);
            for (var entry : params.entrySet()) {
                sb.append("&").append(entry.getKey()).append("=").append(entry.getValue());
            }
            sb.append("&HashIV=").append(HASH_IV);

            // 步驟2: URLEncode
            String encoded = URLEncoder.encode(sb.toString(), "UTF-8");
            
            // 步驟3: 轉小寫
            encoded = encoded.toLowerCase();
            
            // 步驟4: 依綠界規範替換字元
            encoded = encoded.replace("%2d", "-");
            encoded = encoded.replace("%5f", "_");
            encoded = encoded.replace("%2e", ".");
            encoded = encoded.replace("%21", "!");
            encoded = encoded.replace("%2a", "*");
            encoded = encoded.replace("%28", "(");
            encoded = encoded.replace("%29", ")");

            System.out.println("加密前字串：" + encoded);

            // 步驟5: SHA256 轉大寫
            return DigestUtils.sha256Hex(encoded).toUpperCase();
            
        } catch (Exception e) {
            throw new RuntimeException("CheckMac 產生失敗", e);
        }
    }

    private String urlEncodeToLower(String str) {
        try {
            String encoded = URLEncoder.encode(str, StandardCharsets.UTF_8);
        
            // 轉小寫
            encoded = encoded.toLowerCase();
            
            // 依照綠界規範替換回來
            encoded = encoded.replace("%2d", "-");
            encoded = encoded.replace("%5f", "_");
            encoded = encoded.replace("%2e", ".");
            encoded = encoded.replace("%21", "!");
            encoded = encoded.replace("%2a", "*");
            encoded = encoded.replace("%28", "(");
            encoded = encoded.replace("%29", ")");
            
            return encoded;
        } catch (Exception e) {
            throw new RuntimeException("URL encode 失敗", e);
        }

    }
}