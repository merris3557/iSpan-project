package com.example.demo.security;

import com.example.demo.entity.AuthProvider;

import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

/**
 * OAuth2 用戶服務
 * 處理 Google OAuth2 登入後的用戶資訊
 */
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oauth2User = super.loadUser(userRequest);

        // 取得 OAuth2 提供者 (google, facebook, etc.)
        String registrationId = userRequest.getClientRegistration().getRegistrationId();

        // 從 OAuth2 回應中提取用戶資訊
        Map<String, Object> attributes = oauth2User.getAttributes();
        String email = (String) attributes.get("email");
        String name = (String) attributes.get("name");
        String providerId = (String) attributes.get("sub"); // Google 用戶 ID

        if (email == null) {
            throw new OAuth2AuthenticationException("Email not found from OAuth2 provider");
        }

        // 查找或創建用戶
        User user = userRepository.findByEmail(email)
                .map(existingUser -> updateExistingUser(existingUser, name, providerId))
                .orElseGet(() -> createNewUser(email, name, providerId, registrationId));

        return new CustomOAuth2User(oauth2User, user);
    }

    /**
     * 更新現有用戶的 OAuth 資訊
     */
    private User updateExistingUser(User user, String name, String providerId) {
        // 更新用戶名稱 (如果 OAuth 提供了新名稱)
        if (name != null && !name.isEmpty()) {
            user.setName(name);
        }

        // 更新 provider ID
        if (providerId != null) {
            user.setProviderId(providerId);
        }

        return userRepository.save(user);
    }

    /**
     * 創建新的 OAuth 用戶
     */
    private User createNewUser(String email, String name, String providerId, String registrationId) {
        AuthProvider authProvider = AuthProvider.valueOf(registrationId.toUpperCase());

        User newUser = User.builder()
                .email(email)
                .name(name != null ? name : email.split("@")[0])
                .password("") // OAuth 用戶不需要密碼
                .isStore(false)
                .enabled(true)
                .authProvider(authProvider)
                .providerId(providerId)
                .twoFactorEnabled(false) // OAuth 用戶預設不啟用 2FA
                .build();

        return userRepository.save(newUser);
    }
}
