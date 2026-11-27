package com.example.clostino.service;

import com.example.clostino.entity.Product;
import com.example.clostino.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    private Product testProduct;

    @BeforeEach
    void setUp() {
        testProduct = new Product();
        testProduct.setId(1L);
        testProduct.setName("Tシャツ");
        testProduct.setPrice(3000);
        testProduct.setDescription("綿100%のTシャツ");
    }

    @Test
    @DisplayName("全商品を取得できる")
    void findAll_商品が存在する場合_商品リストを返す() {
        // Given
        Product product1 = new Product();
        product1.setId(1L);
        product1.setName("Tシャツ");

        Product product2 = new Product();
        product2.setId(2L);
        product2.setName("ジーンズ");

        when(productRepository.findAll())
            .thenReturn(Arrays.asList(product1, product2));

        // When
        List<Product> result = productService.findAll();

        // Then
        assertEquals(2, result.size());
        verify(productRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("商品IDで商品を取得できる")
    void findById_存在する商品ID_商品を返す() {
        // Given
        when(productRepository.findById(1L))
            .thenReturn(Optional.of(testProduct));

        // When
        Product result = productService.findById(1L).orElseThrow();

        // Then
        assertNotNull(result);
        assertEquals("Tシャツ", result.getName());
    }

    @Test
    @DisplayName("存在しない商品IDの場合は空のOptionalを返す")
    void findById_存在しない商品ID_空のOptionalを返す() {
        // Given
        when(productRepository.findById(999L))
            .thenReturn(Optional.empty());

        // When
        Optional<Product> result = productService.findById(999L);

        // Then
        assertTrue(result.isEmpty());
    }
}