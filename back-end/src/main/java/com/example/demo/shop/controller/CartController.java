package com.example.demo.shop.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.shop.dto.CartDTO;
import com.example.demo.shop.service.CartService;


@RestController
@RequestMapping("/api/cart")
@CrossOrigin(origins = "http://localhost:5173")
public class CartController {
    
    @Autowired
    private CartService cartService;

    //加入購物車，前端發送json productId 1 ,quantity 2
    @PostMapping("/add")
    public ResponseEntity<?> addToCart(@RequestBody Map<String, Object> payload){
        try {
            Integer productId = (Integer) payload.get("productId");
            Integer quantity = (Integer) payload.get("quantity");

            cartService.addToCart(productId, quantity);

            return ResponseEntity.ok().body(Map.of("message", "已成功加入購物車"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    //取得目前購物清單
    @GetMapping("/all")
    public ResponseEntity<List<CartDTO>> getMyCart(){
        List<CartDTO> cartList = cartService.getCartItems();
        return ResponseEntity.ok(cartList);
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCartItem(@PathVariable("id") Integer id) {
        try {
            cartService.deleteCartItem(id);
            return ResponseEntity.ok().body(Map.of("message", "已從購物車移除商品"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }



    @GetMapping("/check-stock")
    public ResponseEntity<?> checkStock() {
        try {
            cartService.checkCartStock();
            return ResponseEntity.ok().body(Map.of("message", "庫存正常"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }


    @PostMapping("/sync")
    public ResponseEntity<List<CartDTO>> syncCart() {
        List<CartDTO> cartList = cartService.syncCartWithStock();
        return ResponseEntity.ok(cartList);
    }



}



