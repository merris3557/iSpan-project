package com.example.demo.storeRegis;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.example.demo.admin.Admin;
import com.example.demo.user.User;

import java.time.LocalDateTime;

@Entity
@Table(name = "store_regis")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StoreRegistration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "applier_id", nullable = false)
    private User applicant; // 申請人

    @Column(nullable = false, length = 100)
    private String email; // 強制帶入user.email

    @Column(length = 100)
    private String name; // 預設帶入user.name

    @Column(name = "store_name", length = 100)
    private String storeName; // 店家名稱

    @Column(length = 20)
    private String phone; // 店家電話

    @Column(length = 255)
    private String address; // 店家地址

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manager_id")
    private Admin manager; // 負責處理的客服人員

    @Column(length = 1000)
    private String reply; // 回復訊息 (退回原因)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private StoreRegistrationStatus status = StoreRegistrationStatus.PENDING;

    @Column(nullable = false)
    @Builder.Default
    private Boolean isUpdate = false;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
