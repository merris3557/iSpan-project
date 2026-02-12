# 開發者指南：取得使用者資料

本文件說明如何在前端頁面與後端程式中，取得當前登入者或特定使用者的資料。

## 1. 前端 (Vue + Pinia)

在前端，我們使用 Pinia 的 `auth` store 來管理登入狀態與使用者資訊。

### 如何取得當前登入者資料

首先，在您的 Vue Component `<script setup>` 中引入 `useAuthStore`：

```javascript
import { useAuthStore } from '@/stores/auth';
import { storeToRefs } from 'pinia'; // 建議使用 storeToRefs 保持響應性

const authStore = useAuthStore();
// 如果需要解構並保持響應性 (Reactive)
const { user, isLoggedIn, userName } = storeToRefs(authStore);
```

### 常用欄位存取

一旦取得 `authStore` 或 `user`，您可以直接存取以下欄位：

```javascript
// 取得使用者名稱
console.log(authStore.userName); 
// 或
console.log(authStore.user?.name);

// 取得 Email
console.log(authStore.user?.email);

// 判斷是否為商家 (Boolean)
// true = 商家, false = 一般用戶
if (authStore.user?.isStore) {
    console.log("這是商家帳號");
}

// 取得使用者 ID (Primary Key)
console.log(authStore.user?.id);
```

### 檢查是否登入

```javascript
if (authStore.isLoggedIn) {
    // 已登入
} else {
    // 未登入
}
```

---

## 2. 後端 (Spring Boot)

在後端 Service 層或 Controller 層，您可以透過 `UserRepository` 查詢資料，或使用 `UserService` 取得當前登入者。

### 透過 ID 或 Email 查詢 (UserRepository)

如果您已經知道使用者的 ID 或 Email，可以直接注入 `UserRepository` 進行查詢。

```java
import com.example.demo.repository.UserRepository;
import com.example.demo.entity.User;
import com.example.demo.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired; // 或使用 Lombok @RequiredArgsConstructor

@Service
@RequiredArgsConstructor
public class YourService {

    private final UserRepository userRepository;

    public void someMethod(Long userId, String email) {
        // 1. 透過 ID 查詢
        User userById = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("找不到使用者 ID: " + userId));

        // 2. 透過 Email 查詢
        User userByEmail = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("找不到使用者 Email: " + email));
        
        // 取得資料
        System.out.println("Name: " + userById.getName());
        System.out.println("Is Store: " + userById.getIsStore());
    }
}
```

### 取得當前登入者 (Context)

如果您需要獲取「目前發送請求的使用者」資料，可以使用 `SecurityContextHolder` 或參考 `UserService` 中的實作。

```java
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.Authentication;

// ...

public void getCurrentUserExample() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    
    if (authentication != null && authentication.isAuthenticated() && 
        !authentication.getPrincipal().equals("anonymousUser")) {
            
        String currentEmail = authentication.getName();
        
        // 然後使用 repository 查詢完整資料
        User currentUser = userRepository.findByEmail(currentEmail)
                .orElseThrow(() -> new RuntimeException("使用者不存在"));
                
        // 判斷是否為商家
        if (Boolean.TRUE.equals(currentUser.getIsStore())) {
            // 是商家
        }
    }
}
```

---

## 3. 技術設計細節 (給進階開發者)

測試頁面:
- **路徑**: `/getusertest`
- **對應組件**: `src/views/GetUserTestView.vue`

此頁面展示了同時從 Pinia Store 讀取資料以及從後端 API 抓取資料的實作方式。

為了保持程式碼的整潔與可維護性，本專案在 API 存取上遵循以下設計原則：

### API 模組化與封裝 (以 `user.js` 為例)

*   **關注點分離 (Separation of Concerns)**：
    Vue 組件 (View) 只負責 UI 邏輯。所有的 HTTP 請求路徑、方法 (GET/POST) 應封裝在 `src/api/` 目錄下的 JS 檔案中。
*   **優點**：當後端端點變更時，只需修改對應的 API JS 檔案，無需改動多個 UI 組件。

### 非同步資料抓取模式

在 `GetUserTestView.vue` 中，我們展示了處理非同步資料的標準流程：

1.  **定義狀態**：`loading` (讀取中)、`error` (錯誤訊息)、`data` (回傳結果)。
2.  **使用 `async/await`**：確保程式碼易讀，並配合 `try...catch` 捕捉網路或權限錯誤。
3.  **狀態清理**：在 `finally` 區塊中關閉載入狀態。

### 安全驗證 (JWT Token)

雖然我們有攔截器，但在範例中顯式展示了在 Header 帶入 `Authorization: Bearer <token>` 的做法，這是為了讓小組成員理解後端 API 是如何識別「你是誰」以及「你有什麼權限」的基礎原理。
