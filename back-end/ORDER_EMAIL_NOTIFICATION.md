# 訂單郵件通知功能說明

## 功能概述

實現了兩種自動郵件通知功能：
1. **訂單確認郵件** - 客人下訂單時發送（未付款狀態）
2. **支付成功郵件** - 客人使用 ECPay 完成付款時發送

## 實現細節

### 1. 後端修改

#### Orders Entity 擴展
- 新增 `deliveryMethod` 欄位，用於存儲配送方式

#### OrderNotificationService (新建)
位置: `com.example.demo.shop.service.OrderNotificationService`

提供兩個主要方法：
- `sendOrderNotification(Orders order)` - 發送訂單確認郵件
- `sendPaymentSuccessNotification(Orders order)` - 發送支付成功郵件

**郵件內容特性：**
- ✓ 響應式 HTML 模板
- ✓ 網站主色配色 (#9f9572)
- ✓ 商品清單詳細展示（名稱、數量、價格、小計）
- ✓ 配送信息（收件人、電話、配送方式）
- ✓ 地址遮蔽：只顯示「縣市+行政區+前幾字」（e.g., 高雄市前鎮區中正***）
- ✓ 付款狀態提示
- ✓ 非同步發送（使用 @Async）

#### OrderService 修改
- 在 `checkout()` 方法最後調用 `orderNotificationService.sendOrderNotification()`
- 保存配送方式到訂單

#### EcpayController 修改
- 在 `returnUrl()` 方法支付成功時調用 `orderNotificationService.sendPaymentSuccessNotification()`

### 2. 郵件模板設計

#### 訂單確認郵件
```
頭部：棕色背景 (主色)
內容：
  - 訂單號、訂單日期
  - 商品清單表格（商品名、數量、單價、小計）
  - 配送信息（收件人、電話、配送方式、地址）
  - 費用總計
  - 付款狀態警告（黃色背景，提示待付款）
```

#### 支付成功郵件
```
頭部：綠色背景 (成功提示)
內容：
  - 訂單號、支付日期
  - 商品簡列（名稱 x 數量 = 金額）
  - 費用總計
  - 配送信息簡要（配送方式、地址）
  - 成功提示（綠色背景）
```

### 3. 地址遮蔽規則

```java
// 原地址: 高雄市前鎮區中正三路123號
// 遮蔽後: 高雄市前鎮區中正***

// 規則: 顯示前 12 個字 + "***"
if (address.length() > 12) {
    return address.substring(0, 12) + "***";
}
```

### 4. 配送方式轉換

支援的配送方式轉換：
- "STANDARD" → "標準配送"
- "EXPRESS" → "快速配送"
- "PICKUP" → "門市自取"
- 其他值保持原樣

## 配置要求

### application.properties 設定
需要已有郵件配置：
```properties
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=your-email@gmail.com
spring.mail.password=your-app-password
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
spring.mail.properties.mail.smtp.starttls.required=true
```

### application.yml 設定（可選）
```yaml
spring:
  mail:
    host: smtp.gmail.com
    port: 587
    username: your-email@gmail.com
    password: your-app-password
    properties:
      mail:
        smtp:
          auth: true
          starttls:
            enable: true
            required: true
```

## 使用流程

### 1. 下訂單階段
```
用戶點擊「結帳」
  ↓
OrderService.checkout() 執行
  ├─ 建立訂單
  ├─ 建立訂單明細
  ├─ 扣除庫存
  └─ 調用 orderNotificationService.sendOrderNotification()
      └─ [非同步] 發送訂單確認郵件給客人
```

### 2. 付款階段
```
用戶完成 ECPay 付款
  ↓
EcpayController.returnUrl() 接收回調
  ├─ 更新訂單狀態為「待出貨」
  ├─ 更新付款方式和付款時間
  └─ 調用 orderNotificationService.sendPaymentSuccessNotification()
      └─ [非同步] 發送支付成功郵件給客人
```

## 技術細節

### 非同步發送
- 使用 Spring 的 `@Async` 注解
- 需要在主程序配置 `@EnableAsync`（已配置）
- 不會阻塞主線程，用戶體驗更好

### 郵件類型
使用 MIME 郵件格式支援 HTML 內容

### 字符編碼
全部使用 UTF-8 編碼，確保中文顯示正常

## 郵件發送者

當前設定郵件發送者: `afterrr5pm@gmail.com`

**生產環境修改：**
在 OrderNotificationService 中修改：
```java
private final String fromEmail = "your-email@gmail.com";
```

## 測試建議

1. **本地測試**：使用 [MailHog](https://github.com/mailhog/Mailhog) 或 [Mailtrap](https://mailtrap.io/)

2. **複製郵件內容**：
   - 訂單確認郵件會在 checkout 後立即發送
   - 支付成功郵件會在 ECPay 支付完成後發送

3. **查看日誌**：
   ```
   訂單通知郵件已發送至：xxx@xxx.com，訂單ID：1
   支付成功通知郵件已發送至：xxx@xxx.com，訂單ID：1
   ```

## 常見問題

### Q: 郵件沒有發送？
A: 檢查：
- application.properties 郵件配置是否正確
- 郵件帳號密碼是否正確
- 防火牆是否允許 SMTP 連接
- 查看後端日誌是否有錯誤

### Q: 郵件格式不正確？
A: 某些郵件客戶端（特別是 Outlook）可能需要調整 CSS。如需修改樣式，編輯 OrderNotificationService 中的 `buildOrderNotificationHtml()` 或 `buildPaymentSuccessHtml()` 方法。

### Q: 如何修改郵件內容？
A: 編輯 OrderNotificationService 中的模板方法，主要可自訂區域：
- 顏色配置（primaryColor, lightBackground）
- HTML 結構
- 動態內容的顯示方式

## 後續改進建議

1. 添加郵件發送隊列（使用 RabbitMQ 或 Kafka）
2. 添加郵件模板管理（Thymeleaf 模板）
3. 訂單發貨時發送「配送中」通知
4. 訂單取消時發送「訂單取消」通知
5. 提供郵件發送歷史記錄
