package com.example.clostino.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 注文エンティティ。
 * 1件の注文に関する情報を保持する。
 */
@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
@ToString(exclude = "orderItems")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 注文日時 */
    @Column(nullable = false)
    private LocalDateTime orderDateTime;

    /** 合計金額 */
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    /** 顧客名 */
    @Column(nullable = false)
    private String customerName;

    /** 配送先住所 */
    @Column(nullable = false)
    private String shippingAddress;

    /** 注文ステータス */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;

    /** 注文したユーザー */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    /**
     * この注文に含まれる注文明細リスト。
     * 1つのOrderが複数のOrderItemを持つ（1対多）。
     */
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<OrderItem> orderItems = new ArrayList<>();

    /**
     * エンティティ保存前に orderDateTime と status を自動設定する。
     * @NoArgsConstructor と共存させるため、コンストラクタではなく @PrePersist を使用する。
     */
    @PrePersist
    protected void onCreate() {
        this.orderDateTime = LocalDateTime.now();
        this.status = OrderStatus.PENDING;
    }

    /**
     * 注文明細を追加する。
     * 双方向の関連を適切に設定する。
     */
    public void addOrderItem(OrderItem item) {
        orderItems.add(item);
        item.setOrder(this);
    }

    /**
     * 注文明細を削除する。
     * 双方向の関連を適切に解除する。
     */
    public void removeOrderItem(OrderItem item) {
        orderItems.remove(item);
        item.setOrder(null);
    }

    /**
     * 合計金額を計算する（各注文明細の小計の合計）。
     */
    public void calculateTotalAmount() {
        this.totalAmount = orderItems.stream()
                .map(OrderItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}