package com.example.demo.shop.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.shop.entity.Orders;
import com.example.demo.shop.repository.OrdersRepository;
import com.example.demo.shop.service.EcpayService;
import com.example.demo.shop.service.OrderNotificationService;

@RestController
@RequestMapping("/api/ecpay")
@CrossOrigin(origins = "http://localhost:5173")
public class EcpayController {

    @Autowired
    private EcpayService ecpayService;

    @Autowired
    private OrdersRepository ordersRepository;

    @Autowired
    private OrderNotificationService orderNotificationService;

    // 產生付款表單
    @GetMapping("/pay/{orderId}")
    public ResponseEntity<String> pay(@PathVariable("orderId") Integer orderId) {
        Orders order = ordersRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("找不到訂單"));

        String itemName = "Order" + orderId;
        int totalAmount = order.getTotalPrice().intValue();

        // 使用現有的 merchantTradeNo，如果不存在才生成新的
        String merchantTradeNo = order.getMerchantTradeNo();
        if (merchantTradeNo == null || merchantTradeNo.isEmpty()) {
            merchantTradeNo = "ORD" + System.currentTimeMillis();
            order.setMerchantTradeNo(merchantTradeNo);
            ordersRepository.save(order);
        }

        String form = ecpayService.generatePaymentForm(orderId, totalAmount, itemName, merchantTradeNo);

        return ResponseEntity.ok()
                .header("Content-Type", "text/html; charset=UTF-8")
                .body(form);
    }

    // 綠界付款完成後回傳（ReturnURL）- 後端通知
    @PostMapping("/return")
    public ResponseEntity<String> returnUrl(@RequestParam Map<String, String> params) {
        String rtnCode = params.get("RtnCode");
        String merchantTradeNo = params.get("MerchantTradeNo");
        String paymentType = params.getOrDefault("PaymentType", "");

        if ("1".equals(rtnCode)) {
            ordersRepository.findByMerchantTradeNo(merchantTradeNo).ifPresent(order -> {
                String newStatus = getStatusByPaymentType(paymentType);
                order.setStatus(newStatus);
                order.setPayMethod("線上付款-" + paymentType);  
                order.setPaymentDate(java.time.Instant.now());
                ordersRepository.save(order);
                System.out.println("付款成功，訂單：" + merchantTradeNo + "，狀態更新為：" + newStatus);
                
                // 發送支付成功郵件
                orderNotificationService.sendPaymentSuccessNotification(order);
            });
        }

        return ResponseEntity.ok("1|OK");
    }

    // 綠界付款完成後導回前端（OrderResultURL）
    @CrossOrigin(origins = "*")
    @PostMapping("/result")
    public ResponseEntity<String> result(@RequestParam Map<String, String> params) {
        System.out.println("綠界回傳參數：" + params);

        String rtnCode = params.get("RtnCode");
        String merchantTradeNo = params.getOrDefault("MerchantTradeNo", "");
        String paymentType = params.getOrDefault("PaymentType", "");

        // 用戶未選擇付款方式就離開
        if (!"1".equals(rtnCode) && paymentType.isEmpty()) {
            ordersRepository.findByMerchantTradeNo(merchantTradeNo).ifPresent(order -> {
                order.setStatus("線上付款-未選擇");
                ordersRepository.save(order);
            });
        }

        String redirectUrl;
        if ("1".equals(rtnCode)) {
            redirectUrl = "http://localhost:5173/payment-result?status=success" +
                    "&tradeNo=" + merchantTradeNo +
                    "&amount=" + params.getOrDefault("TradeAmt", "") +
                    "&paymentDate=" + params.getOrDefault("PaymentDate", "").replace(" ", "+");
        } else {
            redirectUrl = "http://localhost:5173/payment-result?status=fail";
        }

        String html = "<!DOCTYPE html><html><head><meta charset='UTF-8'>" +
                "<meta http-equiv='refresh' content='0;url=" + redirectUrl + "'>" +
                "</head><body></body></html>";

        return ResponseEntity.ok()
                .header("Content-Type", "text/html; charset=UTF-8")
                .body(html);
    }

    private String getStatusByPaymentType(String paymentType) {
        if (paymentType == null) return "待付款";
        if (paymentType.startsWith("Credit") ||
                paymentType.equals("ApplePay") ||
                paymentType.startsWith("TWQR") ||
                paymentType.equals("WeiXinPay") ||
                paymentType.equals("BNPL") ||
                paymentType.startsWith("GreenPay") ||
                paymentType.startsWith("DigitalPayment") ||
                paymentType.startsWith("WebATM")) {
            return "待出貨";
        }
        return "待付款";
    }
}