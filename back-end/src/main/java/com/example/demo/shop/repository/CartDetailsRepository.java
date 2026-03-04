package com.example.demo.shop.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.shop.entity.CartDetails;

public  interface CartDetailsRepository extends JpaRepository<CartDetails, Integer>{
    
    //找使用者所有購物車項目
    List<CartDetails>findByUser_Id(Long userId);

    //檢查使用者是否把商品加入購物車(加數量)
    Optional<CartDetails>findByUser_IdAndProduct_ProductId(Long UserId, Integer productId );

}
