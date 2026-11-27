package com.example.clostino.service;

import com.example.clostino.entity.Product;
import com.example.clostino.exception.ResourceNotFoundException;
import com.example.clostino.repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)  // クラスレベルでの設定
public class ProductService {

    private static final Logger logger = LoggerFactory.getLogger(ProductService.class);

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
     * 指定されたIDの商品を取得する。
     *
     * @param id 商品ID
     * @return 商品エンティティ
     * @throws ResourceNotFoundException 商品が見つからない場合
     */
    public Product findById(Long id) {
        logger.debug("商品検索開始: id={}", id);

        return productRepository.findById(id)
            .orElseThrow(() -> {
                logger.warn("商品が見つかりません: id={}", id);
                return new ResourceNotFoundException("商品ID " + id + " が見つかりません");
            });
    }

    /**
     * 商品を保存する（書き込みあり）
     */
    @Transactional  // メソッドレベルでの設定（readOnly = false）
    public Product saveProduct(Product product) {
        logger.info("商品を保存します: name={}", product.getName());
        Product saved = productRepository.save(product);
        logger.info("商品を保存しました: id={}", saved.getId());
        return saved;
    }

    /**
     * 商品を削除する（書き込みあり）
     */
    @Transactional
    public void deleteProduct(Long id) {
        if (!productRepository.existsById(id)) {
            throw new RuntimeException("商品が見つかりません: " + id);
        }
        productRepository.deleteById(id);
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