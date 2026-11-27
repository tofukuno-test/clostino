package com.example.clostino.repository;

import com.example.clostino.entity.Order;
import com.example.clostino.entity.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    /**
     * すべての注文を、注文明細と商品情報を含めて取得する。
     * N+1問題を回避するためにEntityGraphを使用。
     */
    @EntityGraph(attributePaths = {"orderItems", "orderItems.product"})
    List<Order> findAll();

    /**
     * 指定されたIDの注文を、注文明細と商品情報を含めて取得する。
     */
    @EntityGraph(attributePaths = {"orderItems", "orderItems.product"})
    Optional<Order> findById(Long id);

    /**
     * 特定ユーザーの注文を、注文明細と商品情報を含めて取得する。
     */
    @EntityGraph(attributePaths = {"orderItems", "orderItems.product"})
    List<Order> findByUser(User user);
}