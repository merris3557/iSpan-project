package com.example.demo.shop.controller;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.shop.service.EcpayMapService;

import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/api/ecpay")
public class EcpayMapController {

    // ECPay 官方測試帳戶 - C2C（超商地圖用）
    private static final String MERCHANT_ID = "2000933";
    // private static final String NGROK_URL = "https://shily-untusked-yuri.ngrok-free.dev";
    private static final String NGROK_URL = "https://retouchable-hypoploid-barbera.ngrok-free.dev";

    @Autowired
    private EcpayMapService ecpayMapService;

    // 前端呼叫這個取得地圖參數
    @GetMapping("/map-form")
    public ResponseEntity<Map<String, String>> getMapForm(@RequestParam String storeType) {
        Map<String, String> result = new HashMap<>();
        
        // 準備超商地圖所需的參數（使用官方測試帳戶）
        TreeMap<String, String> params = new TreeMap<>();
        params.put("MerchantID", MERCHANT_ID);
        params.put("LogisticsType", "CVS");
        params.put("LogisticsSubType", storeType);
        params.put("IsCollection", "N");
        params.put("ServerReplyURL", NGROK_URL + "/api/ecpay/map-callback");
        
        // 使用超商地圖服務生成 CheckMacValue（官方測試金鑰）
        String checkMacValue = ecpayMapService.generateCheckMacForMap(params);
        
        result.put("actionUrl", "https://logistics-stage.ecpay.com.tw/Express/map");
        result.put("merchantId", MERCHANT_ID);
        result.put("logisticsType", "CVS");
        result.put("logisticsSubType", storeType);
        result.put("isCollection", "N");
        result.put("serverReplyURL", NGROK_URL + "/api/ecpay/map-callback");
        result.put("checkMacValue", checkMacValue);
        
        return ResponseEntity.ok(result);
    }

    // 綠界選完超商後 POST 到這裡
    @CrossOrigin(origins = "*", allowedHeaders = "*")
    @PostMapping("/map-callback")
    public void mapCallback(@RequestParam Map<String, String> params, 
                            HttpServletResponse response) throws IOException {
        String storeName = params.getOrDefault("CVSStoreName", "");
        String storeAddress = params.getOrDefault("CVSAddress", "");
        String storeId = params.getOrDefault("CVSStoreID", "");
        String storeTelephone = params.getOrDefault("CVSTelephone", "");

        System.out.println("\n=== 超商選擇回調 ===");
        System.out.println("門市名稱: " + storeName);
        System.out.println("地址: " + storeAddress);
        System.out.println("門市代碼: " + storeId);
        System.out.println("電話: " + storeTelephone);
        System.out.println("====================\n");

        String html = String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <title>超商選擇</title>
                <style>
                    body {
                        font-family: 'Microsoft YaHei', Arial, sans-serif;
                        display: flex;
                        justify-content: center;
                        align-items: center;
                        height: 100vh;
                        margin: 0;
                        background: #f5f5f5;
                    }
                    .container {
                        text-align: center;
                        background: white;
                        padding: 40px;
                        border-radius: 8px;
                        box-shadow: 0 2px 8px rgba(0,0,0,0.1);
                    }
                    .loading {
                        font-size: 18px;
                        color: #666;
                    }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="loading">正在回傳門市信息...</div>
                </div>
                <script>
                    (function() {
                        const storeData = {
                            storeName: '%s',
                            storeAddress: '%s',
                            storeId: '%s',
                            storeTelephone: '%s'
                        };
                        
                        console.log('準備發送門市數據:', storeData);
                        
                        // 確保 window.opener 存在
                        if (window.opener) {
                            // 發送數據
                            window.opener.postMessage(storeData, '*');
                            console.log('數據已發送');
                            
                            // 給前端一點時間接收消息
                            setTimeout(() => {
                                console.log('關閉窗口');
                                window.close();
                            }, 500);
                        } else {
                            alert('無法回傳，父窗口不存在');
                            console.error('window.opener 不存在');
                        }
                    })();
                </script>
            </body>
            </html>
        """, storeName, storeAddress, storeId, storeTelephone);

        response.setContentType("text/html;charset=UTF-8");
        response.getWriter().write(html);
    }
}