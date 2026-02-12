package com.example.demo.security;

import com.example.demo.entity.User;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

/**
 * 自訂 OAuth2 用戶類
 * 包裝 OAuth2User 和本地 User 實體
 */
public class CustomOAuth2User implements OAuth2User {

    private final OAuth2User oauth2User;

    @Getter
    private final User user;

    public CustomOAuth2User(OAuth2User oauth2User, User user) {
        this.oauth2User = oauth2User;
        this.user = user;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return oauth2User.getAttributes();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(
                new SimpleGrantedAuthority("ROLE_" + (Boolean.TRUE.equals(user.getIsStore()) ? "STORE" : "USER")));
    }

    @Override
    public String getName() {
        return user.getEmail();
    }
}
