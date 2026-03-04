package com.example.demo.shop.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.shop.dto.ProductsDTO;
import com.example.demo.shop.entity.Products;
import com.example.demo.shop.entity.Stock;
import com.example.demo.shop.exception.ProductNotFoundException;
import com.example.demo.shop.repository.ProductsRepository;
import com.example.demo.shop.repository.StockRepository;

import jakarta.transaction.Transactional;
@Service
public class ProductsService {
    
    @Autowired
    private ProductsRepository productsRepository;
    @Autowired
    private StockRepository stockRepository;


    @Transactional //確認商品跟庫存同時成功或同時失敗
    public void addProductWithStock(ProductsDTO dto){  //ProductsDTO 來接資料，而不直接用 Products Entity。可以避免前端惡意傳送一些不該被修改的欄位（例如：建立時間 createdAt）。
        Products product = new Products();
        product.setProductName(dto.getProductName());
        product.setPrice(dto.getPrice());
        product.setProductDescription(dto.getProductDescription());
        product.setImage(dto.getImage());
        
        //儲存取得的商品
        Products saveProduct = productsRepository.save(product);

        Stock stock = new Stock();
        stock.setAvailableQuantity(dto.getStock());
        stock.setProduct(saveProduct); //因為在stock使用mapsID關聯productsID，所以這邊jpa會自動把saveproducts ID自動填入stock的productID

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
        
        stockRepository.findById(id).ifPresent(stock -> stockRepository.delete(stock));

        productsRepository.delete(deleteProducts);
        return deleteProducts;
    }
}
