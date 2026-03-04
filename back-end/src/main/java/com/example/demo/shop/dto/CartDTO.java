package com.example.demo.shop.dto;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class CartDTO {
    private Integer id;
    private Integer quantity;
    private BigDecimal itemAmount; 
    private Integer productId;
    private String productName;
    private BigDecimal price;
    private String image;

}





// package com.example.demo.shop.dto;

// import java.math.BigDecimal;

// import lombok.Data;

// @Data
// public class CartDTO {
//     private Integer id;
//     private Integer quantity;
//     private BigDecimal itemAmount; 
//     private Integer productId;
//     private String productName;
//     private BigDecimal price;
//     private String image;

// }
