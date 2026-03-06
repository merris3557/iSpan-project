package com.example.demo.shop.entity;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;


@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"order", "product"})
public class OrderDetails {
    

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name ="order_quantity",nullable = false)
    private Integer orderQuantity;

    @Column(name ="product_name_snapshot", length=100)
    private String productNameSnapshot;

    @Column(name ="price_snapshot",nullable = false, precision = 10, scale = 2)
    private BigDecimal priceSnapshot;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal;

    @Column(name ="comments_section",length=200)
    private String commentsSection;


    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable=false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "orderDetails", "user"})
    private Orders order;

    @ManyToOne(fetch =FetchType.LAZY)
    @JoinColumn(name = "product_id" , nullable=false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "stock"})
    private Products product;

}
