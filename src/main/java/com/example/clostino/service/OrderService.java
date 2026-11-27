package com.example.clostino.service;

import com.example.clostino.dto.OrderItemRequest;
import com.example.clostino.dto.OrderRequest;
import com.example.clostino.entity.Order;
import com.example.clostino.entity.OrderItem;
import com.example.clostino.entity.Product;
import com.example.clostino.entity.User;
import com.example.clostino.exception.ResourceNotFoundException;
import com.example.clostino.repository.OrderRepository;
import com.example.clostino.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 注文に関するビジネスロジックを提供するサービス。
 */
@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;

    public OrderService(OrderRepository orderRepository,
                       ProductRepository productRepository) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
    }

    /**
     * すべての注文を取得する（N+1問題対策済み）。
     */
    public List<Order> findAll() {
        return orderRepository.findAll();
    }

    /**
     * 指定されたIDの注文を取得する（N+1問題対策済み）。
     */
    public Order findById(Long id) {
        return orderRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException(
                "注文ID " + id + " が見つかりません"
            ));
    }

    /**
     * 指定されたユーザーの注文一覧を取得する（N+1問題対策済み）。
     */
    public List<Order> findByUser(User user) {
        return orderRepository.findByUser(user);
    }

    /**
     * 注文を作成する。
     *
     * @param request 注文リクエスト
     * @return 作成された注文
     */
    @Transactional
    public Order createOrder(OrderRequest request, User loginUser) {
        // 1. Orderエンティティを作成（デフォルトコンストラクタでorderDateTime・statusは初期化済み）
        Order order = new Order();
        order.setCustomerName(loginUser.getName());
        order.setShippingAddress(request.getShippingAddress());
        order.setUser(loginUser);

        // 2. 各注文明細を処理
        for (OrderItemRequest itemRequest : request.getItems()) {
            // 商品を取得
            Product product = productRepository.findById(itemRequest.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException(
                    "商品", "ID", itemRequest.getProductId()
                ));

            // 在庫チェック（注文確定時に在庫を減らすため、ここで不足していれば例外）
            if (product.getStock() < itemRequest.getQuantity()) {
                throw new IllegalStateException(
                    String.format("商品「%s」の在庫が不足しています（在庫: %d, 注文数: %d）",
                        product.getName(), product.getStock(), itemRequest.getQuantity()));
            }

            // 在庫を減らす
            product.setStock(product.getStock() - itemRequest.getQuantity());

            // OrderItemを作成（コンストラクタで小計を計算）
            OrderItem orderItem = new OrderItem(product, itemRequest.getQuantity());

            // 双方向関連を設定
            order.addOrderItem(orderItem);
        }

        // 3. 合計金額を計算（OrderのcalculateTotalAmountメソッドを使用）
        order.calculateTotalAmount();

        // 4. 注文を保存（CascadeによりOrderItemも自動保存）
        return orderRepository.save(order);
    }
}