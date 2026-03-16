package com.example.demo.shop.service;

import java.net.URLEncoder;
import java.util.TreeMap;

import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.stereotype.Service;

@Service
public class EcpayMapService {

    // ECPay 官方測試帳戶 - C2C（超商地圖用）
    private static final String MERCHANT_ID = "2000933";
    private static final String HASH_KEY = "XBERn1YOvpM9nfZc";
    private static final String HASH_IV = "h1ONHk4P4yqbl5LK";

    /**
     * 為超商地圖生成 CheckMacValue（使用 MD5）
     * 根據 ECPay 官方文檔要求
     */
    public String generateCheckMacForMap(TreeMap<String, String> params) {
        try {
            // 組合字串
            StringBuilder sb = new StringBuilder();
            sb.append("HashKey=").append(HASH_KEY);
            for (var entry : params.entrySet()) {
                sb.append("&").append(entry.getKey()).append("=").append(entry.getValue());
            }
            sb.append("&HashIV=").append(HASH_IV);

            // URLEncode
            String encoded = URLEncoder.encode(sb.toString(), "UTF-8");
            
            encoded = encoded.toLowerCase();
            
            // 依綠界規範替換字元
            encoded = encoded.replace("%2d", "-");
            encoded = encoded.replace("%5f", "_");
            encoded = encoded.replace("%2e", ".");
            encoded = encoded.replace("%21", "!");
            encoded = encoded.replace("%2a", "*");
            encoded = encoded.replace("%28", "(");
            encoded = encoded.replace("%29", ")");

            System.out.println("超商地圖加密前字串：" + encoded);

            // MD5 轉大寫（ECPay 官方要求）
            return DigestUtils.md5Hex(encoded).toUpperCase();
            
        } catch (Exception e) {
            throw new RuntimeException("超商地圖 CheckMac 產生失敗", e);
        }
    }
}
