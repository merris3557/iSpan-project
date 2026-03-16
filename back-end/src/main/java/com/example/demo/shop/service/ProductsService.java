package com.example.demo.shop.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.shop.dto.ProductsDTO;
import com.example.demo.shop.entity.Products;
import com.example.demo.shop.entity.Stock;
import com.example.demo.shop.exception.ProductNotFoundException;
import com.example.demo.shop.repository.OrderDetailsRepository;
import com.example.demo.shop.repository.ProductsRepository;
import com.example.demo.shop.repository.StockRepository;

import jakarta.transaction.Transactional;
@Service
public class ProductsService {
    
    @Autowired
    private ProductsRepository productsRepository;
    @Autowired
    private StockRepository stockRepository;
    @Autowired
    private OrderDetailsRepository orderDetailsRepository;


    @Transactional //確認商品跟庫存同時成功或同時失敗
    public void addProductWithStock(ProductsDTO dto){  
        Products product = new Products();
        product.setProductName(dto.getProductName());
        product.setPrice(dto.getPrice());
        product.setProductDescription(dto.getProductDescription());
        product.setImage(dto.getImage());
        
        //儲存取得的商品
        Products saveProduct = productsRepository.save(product);

        //編號生成
        saveProduct.setProductCode(String.format("PRD-%04d", saveProduct.getProductId()));
        productsRepository.save(saveProduct);

        Stock stock = new Stock();
        stock.setAvailableQuantity(dto.getStock());
        stock.setProduct(saveProduct); 
        //因為在stock使用mapsID關聯productsID，所以這邊jpa自動把saveproducts ID自動填入stock的productID

        stock.setUpdateAt(java.time.LocalDateTime.now());

        stockRepository.save(stock);
    }


    public List<Products> getAllProducts() {
        return productsRepository.findAll();
    }

    @Transactional
    public Products updateProduct(Integer id, ProductsDTO productsDTO) {
        Products existingProduct = productsRepository.findById(id)
            .orElseThrow(() -> new ProductNotFoundException("資料有異動，找不到該商品，請重新整理畫面再做修改"));

        existingProduct.setProductName(productsDTO.getProductName());
        existingProduct.setPrice(productsDTO.getPrice());
        existingProduct.setImage(productsDTO.getImage());
        existingProduct.setProductDescription(productsDTO.getProductDescription());

        Stock existingStock = stockRepository.findById(id)
            .orElseThrow(() -> new ProductNotFoundException("找不到對應的庫存資料"));

        //從DTO的stock拿值給stock entity的availableQuantity
        existingStock.setAvailableQuantity(productsDTO.getStock());
        
        stockRepository.save(existingStock);
        return productsRepository.save(existingProduct);
    }

    @Transactional
    public Products deleteProductById(Integer id) {
        Products deleteProducts = productsRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("資料有異動，找不到該商品，請重新整理畫面再做修改"));
        
        // 先把 order_details 的 product 設為 null，保留訂單紀錄但解除關聯
        orderDetailsRepository.findByProduct_ProductId(id).forEach(detail -> {
            detail.setProduct(null);
            orderDetailsRepository.save(detail);
        });

        // 再刪除庫存
        stockRepository.findById(id).ifPresent(stock -> stockRepository.delete(stock));

        // 最後刪除商品
        productsRepository.delete(deleteProducts);
        return deleteProducts;
    }
}
