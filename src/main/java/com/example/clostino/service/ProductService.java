package com.example.clostino.service;

import com.example.clostino.entity.Product;
import com.example.clostino.repository.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)  // クラスレベルでの設定
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    /**
     * 全商品を取得する（ページネーション対応・読み取り専用）
     */
    public Page<Product> findAll(Pageable pageable) {
        return productRepository.findAll(pageable);
    }

    /**
     * 全商品を取得する（リスト形式・読み取り専用）
     */
    public List<Product> findAll() {
        return productRepository.findAll();
    }

    /**
     * キーワードで商品を検索する（ページネーション対応・読み取り専用）
     */
    public Page<Product> searchProducts(String keyword, Pageable pageable) {
        return productRepository.findByNameContainingIgnoreCase(keyword, pageable);
    }

    /**
     * キーワードで商品を検索する（リスト形式・読み取り専用）
     */
    public List<Product> searchProducts(String keyword) {
        return productRepository.findByNameContainingIgnoreCase(keyword);
    }

    /**
     * IDで商品を取得する（読み取り専用）
     */
    public Optional<Product> findById(Long id) {
        return productRepository.findById(id);
    }

    /**
     * 商品を保存する（書き込みあり）
     */
    @Transactional  // メソッドレベルでの設定（readOnly = false）
    public Product saveProduct(Product product) {
        return productRepository.save(product);
    }

    /**
     * 商品の在庫を更新する（書き込みあり）
     */
    @Transactional
    public void updateStock(Long productId, int quantity) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("商品が見つかりません: " + productId));

        int newStock = product.getStock() - quantity;
        if (newStock < 0) {
            throw new RuntimeException("在庫が不足しています");
        }

        product.setStock(newStock);
        productRepository.save(product);
    }
}