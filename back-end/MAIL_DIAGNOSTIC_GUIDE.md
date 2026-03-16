# 郵件無法發送診斷指南

## 快速診斷步驟

### 步驟 1：檢查服務是否啟動
```
1. 訪問 http://localhost:8080/api/mail-test/check-config
2. 查看頁面結果
   - 如果顯示 "✓ JavaMailSender: 已正確注入" → 郵件配置正確
   - 如果顯示 "❌ JavaMailSender: 未被注入" → 郵件配置有問題
```

### 步驟 2：查看後台日誌
```
重新執行一個訂單流程，然後在後台日誌中搜尋：

訂單通知郵件的日誌：
✓ 訂單通知郵件已發送至：user@example.com，訂單編號：ORD...
或
❌ 發送訂單通知郵件失敗 - 郵件異常：...
或
❌ 發送訂單通知郵件發生錯誤：...

支付成功郵件的日誌：
✓ 支付成功通知郵件已發送至：user@example.com，訂單編號：ORD...
或
❌ 發送支付成功郵件失敗 - 郵件異常：...
或
❌ 發送支付成功郵件發生錯誤：...
```

---

## 常見問題排查

### 問題 1：日誌中完全沒有郵件相關信息

**可能原因：**
- 郵件發送方法沒有被調用
- 訂單流程有問題

**排查步驟：**
1. 確認是否完成了完整的訂單流程
2. 檢查 OrderService.checkout() 中是否有：
   ```java
   orderNotificationService.sendOrderNotification(savedOrder);
   ```
3. 檢查 EcpayController.returnUrl() 中是否有：
   ```java
   orderNotificationService.sendPaymentSuccessNotification(order);
   ```

---

### 問題 2：日誌顯示「開始發送」但沒有成功日誌

**日誌範例：**
```
開始發送訂單通知郵件，訂單ID：1
（然後沒有後續日誌）
```

**可能原因：**
- 方法拋出異常但沒有記錄
- 郵件配置有問題
- 網絡連接問題

**排查步驟：**
1. 檢查後台是否有 error 級別的日誌
2. 查看完整的異常堆棧

---

### 問題 3：日誌顯示「收件人信箱為空」

**日誌範例：**
```
❌ 收件人信箱為空，訂單ID：1
```

**可能原因：**
- 用戶帳號中沒有信箱地址
- 訂單與用戶的關聯有問題

**排查步驟：**
1. 檢查數據庫中 users 表的該用戶記錄
2. 確保 email 欄位有值
3. 確保 orders.user_id 與 users.id 的關聯正確

---

### 問題 4：日誌顯示「郵件異常」

**日誌範例：**
```
❌ 發送訂單通知郵件失敗 - 郵件異常：
javax.mail.AuthenticationFailedException: 535 5.7.8 Username and password not accepted
```

**可能原因：**
- Gmail 帳號密碼錯誤
- Gmail 未啟用兩步驗證
- 使用的是 Gmail 密碼而不是應用程式密碼

**排查步驟：**
1. 檢查 application.properties 中的：
   ```properties
   spring.mail.username=afterrr5pm@gmail.com
   spring.mail.password=nftk bxnm ahkl kmid
   ```
2. 確認密碼是否為應用程式密碼（不是 Gmail 帳號密碼）
3. 如果忘記應用程式密碼，到 Google 帳號重新生成一個

---

### 問題 5：日誌顯示「訂單詳情為 null」

**日誌範例：**
```
⚠️ 訂單詳情為 null，訂單ID：1
```

**可能原因：**
- 訂單沒有對應的詳情記錄
- 數據庫查詢有問題

**排查步驟：**
1. 檢查數據庫 order_details 表
2. 確保該訂單 ID 有對應的詳情記錄
3. 檢查 orderDetails 與 orders 的外鍵關聯

---

## 郵件配置檢查清單

**application.properties 中必須的配置：**

```properties
# ===== Email Configuration 郵件配置 =====
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=afterrr5pm@gmail.com
spring.mail.password=nftk bxnm ahkl kmid
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
spring.mail.properties.mail.smtp.starttls.required=true
```

**Gmail 應用程式密碼生成步驟：**

1. 前往 https://myaccount.google.com/
2. 左側選擇「安全性」
3. 確認已啟用「兩步驟驗證」
4. 搜尋「應用程式密碼」
5. 選擇「郵件」和「其他裝置 (Windows)」
6. Google 將生成 16 位密碼，複製並在 application.properties 中貼上

---

## 日誌級別配置

如果想看到更詳細的日誌（包括 DEBUG），可以在 application.properties 中添加：

```properties
# 顯示郵件服務的詳細日誌
logging.level.com.example.demo.shop.service.OrderNotificationService=DEBUG
logging.level.org.springframework.mail=DEBUG
```

---

## 測試流程

1. **準備測試帳號**
   - 確保有登錄用戶，且用戶帳號有 email

2. **進行結帳**
   - 添加商品到購物車
   - 完成結帳流程
   - 等待 2-3 秒

3. **查看日誌**
   - 後台應出現「開始發送訂單通知郵件」
   - 如果成功，應出現「✓ 訂單通知郵件已發送至」
   - 如果失敗，應出現「❌ 發送訂單通知郵件失敗」及詳細錯誤

4. **進行支付（可選）**
   - 點擊「前往付款」
   - 完成綠界支付
   - 等待 2-3 秒
   - 查看日誌中的支付成功郵件記錄

5. **檢查信箱**
   - 查看帳戶對應的信箱
   - 應收到訂單確認郵件和（如果已支付）支付成功郵件

---

## 日誌摘要

**關鍵日誌消息：**

| 日誌信息 | 含義 | 狀態 |
|--------|------|------|
| 開始發送訂單通知郵件，訂單ID | 郵件發送流程已開始 | ℹ️ 信息 |
| 查詢完訂單，ID | 訂單已查詢到 | 🐛 調試 |
| 訂單詳情已加載，項數 | 訂單項目已加載 | 🐛 調試 |
| ✓ 訂單通知郵件已發送至 | 郵件成功發送 | ✅ 成功 |
| ❌ 收件人信箱為空 | 無法發送（信箱空值） | ❌ 錯誤 |
| ❌ 發送訂單通知郵件失敗 - 郵件異常 | 郵件配置或發送問題 | ❌ 錯誤 |
| ❌ 發送訂單通知郵件發生錯誤 | 未知錯誤 | ❌ 錯誤 |

---

## 聯絡支援

如果以上步驟都無法解決，請提供：
1. 完整的日誌輸出（包括錯誤堆棧）
2. application.properties 中的郵件配置（密碼部分可隱藏）
3. 數據庫中相關記錄的內容
