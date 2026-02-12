package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "users", indexes = {
        @Index(name = "idx_email", columnList = "email")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 100)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false)
    @Builder.Default
    private Boolean isStore = false; // 區分使用者是否為商店

    @Column(nullable = false)
    @Builder.Default
    private Boolean enabled = true;

    // ===== 雙因素認證 (2FA) 相關欄位 =====

    @Column(name = "two_factor_secret")
    private String twoFactorSecret; // Google Authenticator 密鑰

    @Column(name = "two_factor_enabled", nullable = false)
    @Builder.Default
    private Boolean twoFactorEnabled = false; // 是否啟用 2FA

    // ===== OAuth 相關欄位 =====

    @Enumerated(EnumType.STRING)
    @Column(name = "auth_provider", nullable = false, length = 20)
    @Builder.Default
    private AuthProvider authProvider = AuthProvider.LOCAL; // 認證提供者 (本地/Google)

    @Column(name = "provider_id", length = 100)
    private String providerId; // OAuth 提供者的用戶 ID

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
