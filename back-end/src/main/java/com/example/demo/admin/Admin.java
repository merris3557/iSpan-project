package com.example.demo.admin;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "admins", indexes = {
        @Index(name = "idx_admin_account", columnList = "account")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Admin {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(unique = true, nullable = false, length = 50)
    private String account; // 限手動創建（不串 Google）

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, length = 50)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private AdminPosition position; // 角色（客服、電商、...）

    @Column(unique = true, nullable = false, length = 100)
    private String email;

    @Column(nullable = false)
    @Builder.Default
    private Boolean enabled = true; // 帳號是否鎖定
}
