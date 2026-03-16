# 訂單郵件功能 - 問題修正說明

## 修正的問題

### 1. ✅ 訂單編號不一致問題
**原因**：EcpayController.pay() 方法重新生成 merchantTradeNo，覆蓋了 OrderService.checkout() 時設置的值。

**修正**：
- EcpayController.pay() 現在檢查訂單是否已有 merchantTradeNo
- 如果已存在則沿用，只在需要時才生成新的

**文件修改**：`EcpayController.java` 的 pay() 方法

---

### 2. ✅ 商品清單無法顯示問題
**原因**：訂單的 orderDetails 是 lazy loaded，在非同步 @Async 方法中 Hibernate session 已關閉，無法加載數據。

**修正**：
在 OrderNotificationService 中：
1. 注入 OrdersRepository
2. 在 sendOrderNotification() 和 sendPaymentSuccessNotification() 開始時
3. 重新查詢完整訂單及指名加載 orderDetails
4. 通過 `.size()` 方法强制 Hibernate 加載 lazy collection

**程式碼範例**：
```java
Orders completeOrder = ordersRepository.findById(order.getId()).orElse(order);
if (completeOrder.getOrderDetails() != null) {
    completeOrder.getOrderDetails().size();  // 强制加載
}
```

**文件修改**：`OrderNotificationService.java`

---

### 3. ✅ 支付成功郵件未送達問題
**原因**：同問題 2 - orderDetails 未被加載

**修正**：通過上述修正自動解決

---

### 4. ✅ 訂單編號在郵件中顯示不正確
**原因**：之前郵件顯示 order.getId()（數據庫自增ID），應為 order.getMerchantTradeNo()（用戶看到的訂單號）

**修正**：
- 訂單確認郵件：改用 order.getMerchantTradeNo()
- 支付成功郵件：改用 order.getMerchantTradeNo()
- 郵件主旨線也更新為使用 merchantTradeNo

**文件修改**：`OrderNotificationService.java`

---

## 測試步驟

### 1. 訂單確認郵件測試
```
1. 用戶在前端進行結帳
2. OrderService.checkout() 執行，設置 merchantTradeNo（格式：ORD時間戳）
3. 訂單郵件發送（非同步，通常 1-2 秒內送到）
4. 檢查收信：
   - 主旨：【訂單確認】您的訂單已收到 - 訂單號：ORDxxxx
   - 訂單號應與前端顯示一致
   - 商品清單應完整顯示（名稱、數量、單價、小計）
```

### 2. 支付成功郵件測試
```
1. 用戶點擊「前往付款」進入綠界
2. EcpayController.pay() 執行，使用現有 merchantTradeNo
3. 用戶完成支付
4. 綠界回調 EcpayController.returnUrl()
5. 支付成功郵件發送（非同步，通常 1-2 秒內送到）
6. 在前端 payment-result 頁面看到成功提示後數秒會收到郵件
7. 檢查收信：
   - 主旨：【支付成功】您的訂單已付款 - 訂單號：ORDxxxx
   - 訂單號應與購物單一致
   - 商品清單應顯示（簡列格式）
   - 配送方式應正確顯示
```

---

## 重要事項

### 郵件發送是非同步的
- 郵件不會立即送出
- 通常延遲 1-3 秒
- **不要期待在付款完成同時收到郵件**
- 建議在支付完成後等待 2-3 秒再檢查信箱

### 調試方法
如果郵件未送達，檢查後端日誌：

**訂單郵件日誌**：
```
訂單通知郵件已發送至：user@example.com，訂單編號：ORD1699999999999
```

**支付成功郵件日誌**：
```
支付成功通知郵件已發送至：user@example.com，訂單編號：ORD1699999999999
```

如果沒有看到這些日誌：
1. 檢查 application.properties 郵件配置是否正確
2. 檢查郵件帳號密碼是否有效
3. 確認 @Async 功能已啟用
4. 查看是否有異常日誌

### 郵件內容驗證清單

**訂單確認郵件應包含**：
- ✓ 訂單號碼（ORD 開頭）
- ✓ 訂單日期
- ✓ 商品清單表格：名稱、數量、單價、小計
- ✓ 配送信息：收件人、電話、配送方式、地址（遮蔽）
- ✓ 費用總計
- ✓ 待付款狀態警告

**支付成功郵件應包含**：
- ✓ 訂單號碼（ORD 開頭）
- ✓ 支付日期
- ✓ 商品列表（簡列格式：商品名 × 數量 = 金額）
- ✓ 費用總計
- ✓ 配送方式和地址
- ✓ 成功提示

---

## 修改摘要

| 文件 | 修改內容 |
|------|--------|
| OrderNotificationService.java | - 注入 OrdersRepository<br>- 重新加載訂單及 orderDetails<br>- 改用 merchantTradeNo 顯示訂單號 |
| EcpayController.java | - pay() 方法不再覆蓋現有 merchantTradeNo |
| OrderService.java | - 保持不變（原有 merchantTradeNo 設置正確） |

---

## 驗證修復是否成功

### 方法 1：查看訂單號一致性
- 前端訂單頁面顯示的訂單號
- 郵件中【訂單確認】顯示的訂單號
- 郵件主旨中的訂單號
- **這三個應該完全相同**

### 方法 2：查看商品清單
- 開啟訂單確認郵件
- 商品清單表格應顯示所有購買的商品
- 每條商品應有：名稱、數量、單價、小計
- 表格應該有棕色 (#9f9572) 的表頭

### 方法 3：查看支付成功郵件
- 支付 10 秒後檢查信箱
- 應收到【支付成功】郵件
- 郵件頭部應為綠色（成功提示）
- 應包含完整的商品列表和金額

---

## 常見問題

**Q: 為什麼收不到支付成功郵件？**
A: 
1. 支付完成後要等待 2-3 秒（非同步延遲）
2. 檢查後端日誌是否有"支付成功通知郵件已發送"
3. 檢查垃圾箱
4. 確認郵件配置正確

**Q: 商品清單顯示為空怎麼辦？**
A: 
1. 確認 Orders entity 的 @OneToMany 關係正確
2. 查看後端日誌有無錯誤
3. 檢查 OrderDetails 是否被正確建立到數據庫

**Q: 訂單號還是不一致？**
A:
1. 檢查是否清除了瀏覽器快取和數據庫
2. 確認修改已編譯並重啟應用
3. 新建訂單後立即查看郵件號碼
