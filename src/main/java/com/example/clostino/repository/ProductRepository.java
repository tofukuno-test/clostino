package com.example.clostino.repository;

import com.example.clostino.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    // TODO: カスタムクエリメソッドを追加してください

}