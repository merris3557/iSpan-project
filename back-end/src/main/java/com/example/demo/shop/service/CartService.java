package com.example.demo.shop.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.example.demo.shop.dto.CartDTO;
import com.example.demo.shop.entity.CartDetails;
import com.example.demo.shop.entity.Products;
import com.example.demo.shop.repository.CartDetailsRepository;
import com.example.demo.shop.repository.ProductsRepository;
import com.example.demo.user.User;
import com.example.demo.user.UserRepository;

import jakarta.transaction.Transactional;

@Service
public class CartService {
    @Autowired
    private CartDetailsRepository cartDetailsRepository;
    @Autowired
    private ProductsRepository productsRepository;
    @Autowired
    private UserRepository userRepository; 

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("使用者不存在"));
    }

    @Transactional
    public void addToCart(Integer productId, Integer quantity) {
        User user = getCurrentUser();
        Long userId = user.getId();

        // 找商品與使用者
        Products product = productsRepository.findById(productId)
            .orElseThrow(() -> new RuntimeException("商品不存在"));
        
        // 檢查購物車是否已有此商品
        Optional<CartDetails> existingItem = cartDetailsRepository.findByUser_IdAndProduct_ProductId(userId, productId);

        if (existingItem.isPresent()) {
            // 已存在就累加數量
            CartDetails item = existingItem.get();
            item.setQuantity(item.getQuantity() + quantity);
            // 更新小計 (數量 * 單價)
            item.setItemAmount(product.getPrice().multiply(new BigDecimal(item.getQuantity())));
            cartDetailsRepository.save(item);
        } else {
            // 不存在就新增
            CartDetails newItem = new CartDetails();
            newItem.setUser(user);
            newItem.setProduct(product);
            newItem.setQuantity(quantity);
            newItem.setItemAmount(product.getPrice().multiply(new BigDecimal(quantity)));
            cartDetailsRepository.save(newItem);
        }
    }


    //取得購物車清單
    @Transactional
    public List<CartDTO> getCartItems(){
        User user = getCurrentUser();
        Long userId = user.getId();

        //把撈出來的「資料庫格式 (Entity)」一個個轉變變成「前端格式 (DTO)」。
        return cartDetailsRepository.findByUser_Id(userId).stream().map(item -> {
            CartDTO dto = new CartDTO();
            dto.setId(item.getId());
            dto.setQuantity(item.getQuantity());
            dto.setItemAmount(item.getItemAmount());
            //把前端需要看到的（商品名、價格、數量）填進 DTO

            //從關聯product拿資料寫入DTO
            dto.setProductId(item.getProduct().getProductId());
            dto.setProductName(item.getProduct().getProductName());
            dto.setPrice(item.getProduct().getPrice());
            dto.setImage(item.getProduct().getImage());
            dto.setStock(item.getProduct().getStock().getAvailableQuantity());
            return dto;
    
        }).collect(Collectors.toList());
    }

    @Transactional
    public void deleteCartItem(Integer cartDetailsId){
        try {
            if (!cartDetailsRepository.existsById(cartDetailsId)){
                throw new RuntimeException("找不到該購物車項目，無法刪除");
            }
            cartDetailsRepository.deleteById(cartDetailsId);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    public void checkCartStock() {
    User user = getCurrentUser();
    List<CartDetails> items = cartDetailsRepository.findByUser_Id(user.getId());

        for (CartDetails item : items) {
            int stock = item.getProduct().getStock().getAvailableQuantity();
            if (item.getQuantity() > stock) {
                throw new RuntimeException(
                    "庫存不足：" + item.getProduct().getProductName() 
                    + "：" + stock
                );
            }
        }
    }



    @Transactional
    public List<CartDTO> syncCartWithStock() {
        User user = getCurrentUser();
        Long userId = user.getId();

        List<CartDetails> items = cartDetailsRepository.findByUser_Id(userId);

        for (CartDetails item : items) {
            int stock = item.getProduct().getStock().getAvailableQuantity();
            if (item.getQuantity() > stock) {
                if (stock <= 0) {
                    cartDetailsRepository.delete(item); // 庫存為0直接移除
                } else {
                    item.setQuantity(stock);
                    item.setItemAmount(item.getProduct().getPrice()
                        .multiply(new BigDecimal(stock)));
                    cartDetailsRepository.save(item);
                }
            }
        }

        return getCartItems();
    }
}

