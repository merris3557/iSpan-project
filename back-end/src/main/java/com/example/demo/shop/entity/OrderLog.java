package com.example.demo.shop.entity;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "order_logs")
@Data
public class OrderLog {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "log_level")
    private String logLevel; // INFO, ERROR, WARN

    @Column(name = "action")
    private String action;  

    @Column(name = "message", columnDefinition = "NVARCHAR(MAX)")
    private String message;

    
    @Column(name = "payload", columnDefinition = "NVARCHAR(MAX)")
    private String payload;

    @Column(name = "created_at")
    private Instant createdAt = Instant.now();
}