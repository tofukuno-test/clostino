package com.example.clostino.controller;

import com.example.clostino.dto.ProductCreateRequest;
import com.example.clostino.dto.ProductUpdateRequest;
import com.example.clostino.entity.Product;
import com.example.clostino.service.ProductService;
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

import java.util.List;

@RestController
@RequestMapping("/api")
public class ProductApiController {

    private final ProductService productService;

    public ProductApiController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping("/products")
    public List<Product> getAllProducts() {
        return productService.findAll();
    }

    @GetMapping("/products/{id}")
    public Product getProductById(@PathVariable Long id) {
        return productService.findById(id)
                .orElseThrow(() -> new RuntimeException("商品が見つかりません: id=" + id));
    }

    @PostMapping("/products")
    public ResponseEntity<Product> createProduct(@RequestBody ProductCreateRequest request) {
        // DTOからエンティティに変換
        Product product = new Product();
        product.setName(request.getName());
        product.setPrice(request.getPrice());
        product.setDescription(request.getDescription());
        product.setImageUrl(request.getImageUrl());
        product.setStock(request.getStock());

        // データベースに保存
        Product savedProduct = productService.saveProduct(product);

        // レスポンスを返す
        return ResponseEntity.status(HttpStatus.CREATED).body(savedProduct);
    }

    @PutMapping("/products/{id}")
    public ResponseEntity<Product> updateProduct(
            @PathVariable Long id,
            @RequestBody ProductUpdateRequest request) {

        // 既存の商品を取得
        Product product = productService.findById(id)
                .orElseThrow(() -> new RuntimeException("商品が見つかりません: id=" + id));

        // フィールドを更新
        product.setName(request.getName());
        product.setPrice(request.getPrice());
        product.setDescription(request.getDescription());
        product.setImageUrl(request.getImageUrl());
        product.setStock(request.getStock());

        // 保存
        Product updatedProduct = productService.saveProduct(product);

        return ResponseEntity.ok(updatedProduct);
    }

    @DeleteMapping("/products/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }
}