package com.example.demo.shop.entity;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;


@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"product"})

public class Stock {
    

    @Id
    @Column(name = "product_id")
    private Integer productId;

    @Column(name = "available_quantity",nullable = false)
    private Integer availableQuantity;


    @Column(name = "update_at" , nullable=false)
    private LocalDateTime updateAt;


    @Version
    @Column(name = "version")
    private Long version;


    @OneToOne(fetch =FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "product_id")
    @JsonBackReference //防止循環引用導致json資料消失
    private Products product; 
    

    @PreUpdate
    @PrePersist //在這一筆資料正式塞進資料庫之前，先執行下面這段程式碼。
    protected void onUpdate(){
        this.updateAt = LocalDateTime.now();
    }

}
