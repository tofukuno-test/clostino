package com.example.clostino.repository;

import com.example.clostino.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    // キーワード検索（リスト形式）
    List<Product> findByNameContainingIgnoreCase(String keyword);

    // キーワード検索（ページネーション対応）
    Page<Product> findByNameContainingIgnoreCase(String keyword, Pageable pageable);
}