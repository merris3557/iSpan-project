package com.example.demo.store.service;

import com.example.demo.user.User;
import com.example.demo.common.exception.ResourceNotFoundException;
import com.example.demo.user.UserRepository;
import com.example.demo.store.entity.StoresInfo;
import com.example.demo.store.repository.StoreInfoRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

// 供 StoreInfoService 和 StoreOperationService 共用的基底服務類別，主要提供「取得目前登入店家資訊」的共用邏輯
@RequiredArgsConstructor
public abstract class StoreBaseService {

    protected final UserRepository userRepository;
    protected final StoreInfoRepository storeInfoRepository;

    // 統一取得目前登入店家的邏輯，如果找不到，會由 GlobalExceptionHandler 捕獲並回傳適當的錯誤訊息
    public StoresInfo getMyStore() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        // 防衛性檢查：當 Cookie 不存在或 JWT Filter 未設定 SecurityContext 時，
        // authentication 會是匿名用戶（anonymousUser）。
        // 若不攔截直接查 DB，會因找不到 email="anonymousUser" 而拋 ResourceNotFoundException → 403，
        // 前端的 Refresh 攔截器只處理 401，因此必須在此明確拋出 AuthenticationException → 讓 Spring Security 回 401。
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
            throw new InsufficientAuthenticationException("請先登入後再執行此操作");
        }

        String email = auth.getName();

        // 自動捕獲並回傳 404
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("找不到該登入使用者"));
        
        // 自動捕獲並回傳 403
        return storeInfoRepository.findByUser_Id(user.getId())
                .orElseThrow(() -> new AccessDeniedException("找不到該店家的資訊或您不具備店家身分"));
    }
}