package com.example.clostino.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * 注文明細エンティティ。
 * 注文に含まれる個々の商品情報を保持する。
 */
@Entity
@Table(name = "order_items")
@Getter
@Setter
@NoArgsConstructor
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * この明細が属する注文。
     * 多くのOrderItemが1つのOrderに属する（多対1）。
     * @JsonIgnore で Order → OrderItem → Order の循環参照を防ぐ。
     */
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    /**
     * この明細で購入される商品。
     * 多くのOrderItemが1つのProductを参照する（多対1）。
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    /** 数量 */
    @Column(nullable = false)
    private Integer quantity;

    /** 小計（商品価格 × 数量） */
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal;

    /**
     * 商品と数量を指定して注文明細を作成し、小計を自動計算するコンストラクタ。
     */
    public OrderItem(Product product, Integer quantity) {
        this.product = product;
        this.quantity = quantity;
        if (product != null && product.getPrice() != null && quantity != null) {
            this.subtotal = BigDecimal.valueOf(product.getPrice())
                    .multiply(BigDecimal.valueOf(quantity));
        }
    }
}