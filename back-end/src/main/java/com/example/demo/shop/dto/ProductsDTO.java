package com.example.demo.shop.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.Data;

@Data
public class ProductsDTO{
    private String productName;
    private BigDecimal price;
    private Integer stock;
    private String productDescription;
    private String image;
    private LocalDateTime added;
}
