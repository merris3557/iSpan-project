package com.example.demo.shop.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.example.demo.shop.dto.CheckoutRequestDTO;
import com.example.demo.shop.entity.CartDetails;
import com.example.demo.shop.entity.OrderDetails;
import com.example.demo.shop.entity.Orders;
import com.example.demo.shop.entity.Stock;
import com.example.demo.shop.repository.CartDetailsRepository;
import com.example.demo.shop.repository.OrderDetailsRepository;
import com.example.demo.shop.repository.OrdersRepository;
import com.example.demo.shop.repository.StockRepository;
import com.example.demo.user.User;
import com.example.demo.user.UserRepository;

import jakarta.transaction.Transactional;

@Service
public class OrderService {

    @Autowired private OrdersRepository ordersRepository;
    @Autowired private OrderDetailsRepository orderDetailsRepository;
    @Autowired private CartDetailsRepository cartDetailsRepository;
    @Autowired private StockRepository stockRepository;
    @Autowired private UserRepository userRepository;

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("找不到使用者"));
    }

    @Transactional
    public Orders checkout(CheckoutRequestDTO dto) {
        User user = getCurrentUser();

        // 1. 取得購物車
        List<CartDetails> cartItems = cartDetailsRepository.findByUser_Id(user.getId());
        if (cartItems.isEmpty()) {
            throw new RuntimeException("購物車是空的");
        }

        // 2. 建立訂單
        Orders order = new Orders();
        order.setUser(user);
        order.setReceiverName(dto.getName());
        order.setReceiverPhone(dto.getPhone());

        String city = dto.getCity();
        String district = dto.getDistrict();
        String street = dto.getStreet() != null ? dto.getStreet() : "";
        order.setReceiverAddress(city + district + street);


        order.setPayMethod(dto.getPaymentMethod());
        order.setStatus(dto.getPaymentMethod().equals("ECpay") ? "待付款" : "待出貨");

        // 3. 計算總金額
        BigDecimal totalPrice = cartItems.stream()
            .map(item -> item.getProduct().getPrice()
                .multiply(new BigDecimal(item.getQuantity())))
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal shipping = new BigDecimal(dto.getShippingFee() != null ? dto.getShippingFee() : 0);
            totalPrice = totalPrice.add(shipping);
            order.setTotalPrice(totalPrice);

        Orders savedOrder = ordersRepository.save(order);

        // 4. 建立訂單明細 + 扣庫存
        for (CartDetails cartItem : cartItems) {
            // 扣庫存
            Stock stock = stockRepository.findById(cartItem.getProduct().getProductId())
                .orElseThrow(() -> new RuntimeException("找不到庫存：" + cartItem.getProduct().getProductName()));

            if (stock.getAvailableQuantity() < cartItem.getQuantity()) {
                throw new RuntimeException("庫存不足：" + cartItem.getProduct().getProductName());
            }
            stock.setAvailableQuantity(stock.getAvailableQuantity() - cartItem.getQuantity());
            stockRepository.save(stock);

            // 建立訂單明細
            OrderDetails detail = new OrderDetails();
            detail.setOrder(savedOrder);
            detail.setProduct(cartItem.getProduct());
            detail.setOrderQuantity(cartItem.getQuantity());
            detail.setProductNameSnapshot(cartItem.getProduct().getProductName());
            detail.setPriceSnapshot(cartItem.getProduct().getPrice());
            detail.setSubtotal(cartItem.getProduct().getPrice()
                .multiply(new BigDecimal(cartItem.getQuantity())));
            orderDetailsRepository.save(detail);
        }

        // 5. 清空購物車
        cartDetailsRepository.deleteByUser_Id(user.getId());

        return savedOrder;
    }
}