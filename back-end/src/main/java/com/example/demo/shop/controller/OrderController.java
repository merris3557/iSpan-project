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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.shop.dto.CheckoutRequestDTO;
import com.example.demo.shop.entity.Orders;
import com.example.demo.shop.repository.OrdersRepository;
import com.example.demo.shop.service.OrderService;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = "http://localhost:5173")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrdersRepository ordersRepository;

    @GetMapping("/all")
    public ResponseEntity<?> getAllOrders() {
        try{
            List<Orders> orders = ordersRepository.findAll();
            return ResponseEntity.ok(orders);
        }catch(Exception e){
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/checkout")
    public ResponseEntity<?> checkout(@RequestBody CheckoutRequestDTO dto) {
        try {
            Orders order = orderService.checkout(dto);
            return ResponseEntity.ok().body(Map.of(
                "message", "訂單建立成功",
                "orderId", order.getId(),
                "status", order.getStatus()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }


    @PutMapping("/{id}")
    public ResponseEntity<?> updateOrder(@PathVariable Integer id, @RequestBody Orders orderData) {
    try {
        Orders order = ordersRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("找不到訂單"));
            order.setStatus(orderData.getStatus());
            order.setReceiverName(orderData.getReceiverName());
            order.setReceiverPhone(orderData.getReceiverPhone());
            order.setReceiverAddress(orderData.getReceiverAddress());
            ordersRepository.save(order);
            return ResponseEntity.ok(Map.of("message", "更新成功"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteOrder(@PathVariable Integer id) {
        try {
            ordersRepository.deleteById(id);
            return ResponseEntity.ok(Map.of("message", "刪除成功"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

}