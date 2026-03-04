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
    private UserRepository userRepository; // 假設你有這個

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("使用者不存在"));
    }

    @Transactional
    public void addToCart(Integer productId, Integer quantity) {
        User user = getCurrentUser();
        Long userId = user.getId();

        // 1. 找商品與使用者
        Products product = productsRepository.findById(productId)
            .orElseThrow(() -> new RuntimeException("商品不存在"));
        
        // 2. 檢查購物車是否已有此商品
        Optional<CartDetails> existingItem = cartDetailsRepository.findByUser_IdAndProduct_ProductId(userId, productId);

        if (existingItem.isPresent()) {
            // 3. 已存在則累加數量
            CartDetails item = existingItem.get();
            item.setQuantity(item.getQuantity() + quantity);
            // 更新小計 (數量 * 單價)
            item.setItemAmount(product.getPrice().multiply(new BigDecimal(item.getQuantity())));
            cartDetailsRepository.save(item);
        } else {
            // 4. 不存在則新增
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
            //前端不需要知道資料庫的所有欄位（例如：密碼、建立時間），我們只把前端需要看到的（商品名、價格、數量）填進 DTO

            //從關聯product拿資料寫入DTO
            dto.setProductId(item.getProduct().getProductId());
            dto.setProductName(item.getProduct().getProductName());
            dto.setPrice(item.getProduct().getPrice());
            dto.setImage(item.getProduct().getImage());
            return dto;
    
        }).collect(Collectors.toList());
    }

    @Transactional
    public void deleteCartItem(Integer cartDetailsId){
        if (!cartDetailsRepository.existsById(cartDetailsId)){
            throw new RuntimeException("找不到該購物車項目，無法刪除");
        }
        cartDetailsRepository.deleteById(cartDetailsId);
    }
}







// package com.example.demo.shop.service;

// import java.math.BigDecimal;
// import java.util.List;
// import java.util.Optional;
// import java.util.stream.Collectors;

// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.stereotype.Service;

// import com.example.demo.shop.dto.CartDTO;
// import com.example.demo.shop.entity.CartDetails;
// import com.example.demo.shop.entity.Products;
// import com.example.demo.shop.repository.CartDetailsRepository;
// import com.example.demo.shop.repository.ProductsRepository;
// import com.example.demo.user.User;
// import com.example.demo.user.UserRepository;

// import jakarta.transaction.Transactional;

// @Service
// public class CartService {
//     @Autowired
//     private CartDetailsRepository cartDetailsRepository;
//     @Autowired
//     private ProductsRepository productsRepository;
//     @Autowired
//     private UserRepository userRepository; // 假設你有這個

//     @Transactional
//     public void addToCart(Integer productId, Integer quantity) {
//         Long mockUserId = 1L; // 先硬編碼一個 ID，等登入模組好了再從 SecurityContext 取

//         // 1. 找商品與使用者
//         Products product = productsRepository.findById(productId)
//             .orElseThrow(() -> new RuntimeException("商品不存在"));
//         User user = userRepository.findById(mockUserId).get();

//         // 2. 檢查購物車是否已有此商品
//         Optional<CartDetails> existingItem = cartDetailsRepository.findByUser_IdAndProduct_ProductId(mockUserId, productId);

//         if (existingItem.isPresent()) {
//             // 3. 已存在則累加數量
//             CartDetails item = existingItem.get();
//             item.setQuantity(item.getQuantity() + quantity);
//             // 更新小計 (數量 * 單價)
//             item.setItemAmount(product.getPrice().multiply(new BigDecimal(item.getQuantity())));
//             cartDetailsRepository.save(item);
//         } else {
//             // 4. 不存在則新增
//             CartDetails newItem = new CartDetails();
//             newItem.setUser(user);
//             newItem.setProduct(product);
//             newItem.setQuantity(quantity);
//             newItem.setItemAmount(product.getPrice().multiply(new BigDecimal(quantity)));
//             newItem.setImage(product.getImage());
//             cartDetailsRepository.save(newItem);
//         }
//     }


//     //取得購物車清單
//     @Transactional
//     public List<CartDTO> getCartItems(){
//         Long mockUserId = 1L;  
//         //從資料庫撈出1號會員所有東西

//         //把撈出來的「資料庫格式 (Entity)」一個個轉變變成「前端格式 (DTO)」。
//         return cartDetailsRepository.findByUser_Id(mockUserId).stream().map(item -> {
//             CartDTO dto = new CartDTO();
//             dto.setId(item.getId());
//             dto.setQuantity(item.getQuantity());
//             dto.setItemAmount(item.getItemAmount());
//             //前端不需要知道資料庫的所有欄位（例如：密碼、建立時間），我們只把前端需要看到的（商品名、價格、數量）填進 DTO

//             //從關聯product拿資料寫入DTO
//             dto.setProductId(item.getProduct().getProductId());
//             dto.setProductName(item.getProduct().getProductName());
//             dto.setPrice(item.getProduct().getPrice());
//             dto.setImage(item.getProduct().getImage());
//             return dto;
    
//         }).collect(Collectors.toList());
//     }

//     @Transactional
//     public void deleteCartItem(Integer cartDetailsId){
//         if (!cartDetailsRepository.existsById(cartDetailsId)){
//             throw new RuntimeException("找不到該購物車項目，無法刪除");
//         }
//         cartDetailsRepository.deleteById(cartDetailsId);
//     }
// }