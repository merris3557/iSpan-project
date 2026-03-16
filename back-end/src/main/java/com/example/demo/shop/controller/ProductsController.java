package com.example.demo.shop.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.shop.dto.ProductsDTO;
import com.example.demo.shop.entity.Products;
import com.example.demo.shop.entity.Stock;
import com.example.demo.shop.exception.ProductNotFoundException;
import com.example.demo.shop.repository.StockRepository;
import com.example.demo.shop.service.ProductsService;


@RestController
@RequestMapping("/api/products")
// @CrossOrigin(origins = "http://localhost:5173")
public class ProductsController {
    
    @Autowired
    private ProductsService productsService;

    @Autowired
    private StockRepository stockRepository;


    @PostMapping("/add")
    public ResponseEntity<?> addProduct(@RequestBody ProductsDTO productsDTO){

        try {
            productsService.addProductWithStock(productsDTO);

            return ResponseEntity.ok().body("{\"message\":\"商品與庫存新增成功\"}");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("新增失敗: " +e.getMessage());
        }


    }

    @GetMapping("/all")
    public ResponseEntity<List<Products>> getAllProducts(){
        List<Products> products = productsService.getAllProducts();
        return ResponseEntity.ok(products);
    }


    @PutMapping("/{id}")
    public ResponseEntity<?> updateProduct(@PathVariable("id") Integer id, @RequestBody ProductsDTO productsDTO){

        try {
            Products updatedProduct = productsService.updateProduct(id, productsDTO);

            return ResponseEntity.ok().body("{\"message\":\"商品修改成功\"}");
        } catch (ProductNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        
        
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("系統發生錯誤 " );
        }

    }


    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteProduct(@PathVariable("id") Integer id){
        try {
            productsService.deleteProductById(id);
            return ResponseEntity.ok().body("刪除成功");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("刪除失敗 " );
        }
    }


    @PutMapping("/updateStock")
    public ResponseEntity<?> updateStock(@RequestBody java.util.Map<String, Object> body) {
        try {
            Integer productId = (Integer) body.get("productId");
            String type = (String) body.get("type");
            Integer amount = (Integer) body.get("amount");

            Stock stock = stockRepository.findByProductId(productId)
                .orElseThrow(() -> new RuntimeException("找不到庫存"));

            if ("add".equals(type)) {
                stock.setAvailableQuantity(stock.getAvailableQuantity() + amount);
            } else if ("set".equals(type)) {
                stock.setAvailableQuantity(amount);
            }

            stockRepository.save(stock);
            return ResponseEntity.ok().body("{\"message\":\"庫存更新成功\"}");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("更新失敗: " + e.getMessage());
        }
    }


}