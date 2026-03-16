package com.example.demo.shop.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;


@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"stock"})

public class Products{
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id")
    private Integer productId;

    @Column(name = "product_name", nullable = false, length = 100)    
    private String  productName;

    @Column(name = "product_code", unique = true, length = 20)
    private String productCode;

    @Column(name = "image", columnDefinition = "NVARCHAR(MAX)")
    private String image;

    @Column(name= "price", nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "product_description", nullable = true, columnDefinition = "NVARCHAR(MAX)")
    private String productDescription;


    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime added;

    @Column(name = "category", length = 50)
    private String category;

    
    //資料存數的時候，自動填入當下時間
    @PrePersist 
    protected void onCreate(){
        if (this.added == null){
            this.added = LocalDateTime.now();
        }
    }

    

    @OneToOne(mappedBy = "product", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JsonManagedReference //父端
    private Stock stock;

}