package com.example.demo.shop.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.shop.entity.Orders;


public interface  OrdersRepository extends JpaRepository<Orders, Integer> {
    
    Optional<Orders>findByMerchantTradeNo(String merchantTradeNo);
}
