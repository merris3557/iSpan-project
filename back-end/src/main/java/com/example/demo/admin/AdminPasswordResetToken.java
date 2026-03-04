package com.example.demo.admin;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 管理員密碼重設 Token 實體
 * 用於儲存密碼重設請求的驗證 token
 */
@Entity
@Table(name = "admin_password_reset_tokens")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminPasswordResetToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String token; // UUID token

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(nullable = false, name = "admin_id")
    private Admin admin;

    @Column(nullable = false)
    private LocalDateTime expiryDate;

    @Column(nullable = false)
    @Builder.Default
    private Boolean used = false; // 是否已使用

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    /**
     * 檢查 token 是否已過期
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiryDate);
    }
}
