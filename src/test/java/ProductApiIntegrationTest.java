package com.example.clostino;

import com.example.clostino.dto.ProductCreateRequest;
import com.example.clostino.dto.ProductUpdateRequest;
import com.example.clostino.entity.Product;
import com.example.clostino.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
class ProductApiIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ProductRepository productRepository;

    @BeforeEach
    void setUp() {
        productRepository.deleteAll();
    }

    @Test
    @DisplayName("商品を作成できる")
    void createProduct_有効なリクエスト_商品が作成される() {
        // Given: リクエストデータを準備
        ProductCreateRequest request = new ProductCreateRequest();
        request.setName("テスト商品");
        request.setPrice(5000);
        request.setDescription("テスト用の商品説明");

        // When: POSTリクエストを送信
        ResponseEntity<Product> response = restTemplate.postForEntity(
            "/api/products",
            request,
            Product.class
        );

        // Then: レスポンスを検証
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getId());
        assertEquals("テスト商品", response.getBody().getName());

        // データベースにも保存されていることを確認
        assertTrue(productRepository.findById(response.getBody().getId()).isPresent());
    }

    @Test
    @DisplayName("商品一覧を取得できる")
    void getAllProducts_商品が存在する_商品リストを返す() {
        // Given: テストデータをデータベースに直接登録
        Product product1 = new Product();
        product1.setName("商品A");
        product1.setPrice(1000);
        productRepository.save(product1);

        Product product2 = new Product();
        product2.setName("商品B");
        product2.setPrice(2000);
        productRepository.save(product2);

        // When: GETリクエストを送信
        ResponseEntity<Product[]> response = restTemplate.getForEntity(
            "/api/products",
            Product[].class
        );

        // Then: レスポンスを検証
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().length);
    }

    @Test
    @DisplayName("商品詳細を取得できる")
    void getProductById_存在する商品ID_商品を返す() {
        // Given: テストデータを準備
        Product product = new Product();
        product.setName("テスト商品");
        product.setPrice(3000);
        Product savedProduct = productRepository.save(product);

        // When: GETリクエストを送信
        ResponseEntity<Product> response = restTemplate.getForEntity(
            "/api/products/" + savedProduct.getId(),
            Product.class
        );

        // Then: レスポンスを検証
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("テスト商品", response.getBody().getName());
    }

    @Test
    @DisplayName("商品を更新できる")
    void updateProduct_存在する商品_商品が更新される() {
        // Given: 既存の商品を作成
        Product product = new Product();
        product.setName("更新前商品");
        product.setPrice(1000);
        Product savedProduct = productRepository.save(product);

        // 更新用のリクエスト
        ProductUpdateRequest updateRequest = new ProductUpdateRequest();
        updateRequest.setName("更新後商品");
        updateRequest.setPrice(2000);

        // When: PUTリクエストを送信
        restTemplate.put(
            "/api/products/" + savedProduct.getId(),
            updateRequest
        );

        // Then: データベースで更新を確認
        Product updatedProduct = productRepository.findById(savedProduct.getId())
            .orElseThrow();
        assertEquals("更新後商品", updatedProduct.getName());
        assertEquals(2000, updatedProduct.getPrice());
    }

    @Test
    @DisplayName("商品を削除できる")
    void deleteProduct_存在する商品_商品が削除される() {
        // Given: 削除対象の商品を作成
        Product product = new Product();
        product.setName("削除対象商品");
        product.setPrice(1000);
        Product savedProduct = productRepository.save(product);

        // When: DELETEリクエストを送信
        restTemplate.delete("/api/products/" + savedProduct.getId());

        // Then: データベースから削除されていることを確認
        assertFalse(productRepository.findById(savedProduct.getId()).isPresent());
    }

    @Test
    @DisplayName("商品のCRUD操作が正しく動作する")
    void productCrudOperations() {
        // Create: 商品を作成
        ProductCreateRequest createRequest = new ProductCreateRequest();
        createRequest.setName("テスト商品");
        createRequest.setPrice(5000);

        ResponseEntity<Product> createResponse = restTemplate.postForEntity(
            "/api/products",
            createRequest,
            Product.class
        );

        assertEquals(HttpStatus.CREATED, createResponse.getStatusCode());
        Long productId = createResponse.getBody().getId();

        // Read: 商品を取得
        ResponseEntity<Product> readResponse = restTemplate.getForEntity(
            "/api/products/" + productId,
            Product.class
        );

        assertEquals(HttpStatus.OK, readResponse.getStatusCode());
        assertEquals("テスト商品", readResponse.getBody().getName());

        // Update: 商品を更新
        ProductUpdateRequest updateRequest = new ProductUpdateRequest();
        updateRequest.setName("更新後商品");
        updateRequest.setPrice(6000);

        restTemplate.put("/api/products/" + productId, updateRequest);

        Product updatedProduct = productRepository.findById(productId).orElseThrow();
        assertEquals("更新後商品", updatedProduct.getName());

        // Delete: 商品を削除
        restTemplate.delete("/api/products/" + productId);

        assertFalse(productRepository.findById(productId).isPresent());
    }
}