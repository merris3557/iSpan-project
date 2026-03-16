package com.example.demo.shop.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.core.context.SecurityContextHolder;
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
import com.example.demo.shop.repository.OrderDetailsRepository;
import com.example.demo.shop.repository.OrdersRepository;
import com.example.demo.shop.service.OrderService;
import com.example.demo.user.User;
import com.example.demo.user.UserRepository;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = "http://localhost:5173")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrdersRepository ordersRepository;

    @Autowired
    private OrderDetailsRepository orderDetailsRepository;

    @Autowired
    private UserRepository userRepository;


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
        } catch (ObjectOptimisticLockingFailureException e) {
        return ResponseEntity.status(409).body(Map.of("error", "結帳失敗，商品庫存已變動，請重新整理購物車再試"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }



    @PutMapping("/{id}")
    public ResponseEntity<?> updateOrder(@PathVariable("id") Integer id, @RequestBody Orders orderData) {
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
    public ResponseEntity<?> deleteOrder(@PathVariable("id") Integer id) {
        try {
            System.out.println("刪除訂單：" + id);
            orderDetailsRepository.deleteByOrderId(id);
            System.out.println("明細刪除完成");
            ordersRepository.deleteById(id);
            System.out.println("訂單刪除完成");
            return ResponseEntity.ok(Map.of("message", "刪除成功"));
        } catch (Exception e) {
            System.out.println("刪除失敗：" + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }


    @GetMapping("/my")
    public ResponseEntity<?> getMyOrders() {
        try {
            String email = SecurityContextHolder.getContext().getAuthentication().getName();
            System.out.println("查詢訂單，email：" + email);
            User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("找不到使用者"));
            System.out.println("找到使用者：" + user.getId());
            List<Orders> orders = ordersRepository.findByUser_IdOrderByCreatedAtDesc(user.getId());
            System.out.println("訂單數量：" + orders.size());
            return ResponseEntity.ok(orders);
        } catch (Exception e) {
            System.out.println("錯誤：" + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }


    @GetMapping("/latest")
    public ResponseEntity<?> getLatestOrder() {
        try {
            List<Orders> orders = ordersRepository.findAll(
                org.springframework.data.domain.Sort.by(
                    org.springframework.data.domain.Sort.Direction.DESC, "createdAt"
                )
            );
            if (orders.isEmpty()) return ResponseEntity.ok(null);
            
            Orders latest = orders.get(0);
            String name = latest.getReceiverName();
            
            // 取姓 + 先生/小姐（隨機）
            String surname = name != null && !name.isEmpty() ? name.substring(0, 1) : "某";
            String masked = surname;
            if (name != null && name.length() > 1) {
                masked = surname + "x".repeat(name.length() - 1);
            }
            String displayName = masked;

            // 取第一個商品名稱
            String productName = "商品";
            if (latest.getOrderDetails() != null && !latest.getOrderDetails().isEmpty()) {
                productName = latest.getOrderDetails().get(0).getProductNameSnapshot();
            }

            return ResponseEntity.ok(Map.of(
                "displayName", displayName,
                "productName", productName
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}