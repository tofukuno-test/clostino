package com.example.clostino.controller;

import com.example.clostino.dto.OrderRequest;
import com.example.clostino.entity.Order;
import com.example.clostino.entity.User;
import com.example.clostino.exception.ResourceNotFoundException;
import com.example.clostino.service.OrderService;
import com.example.clostino.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

/**
 * 注文に関するREST APIを提供するコントローラー。
 *
 * <p>認証情報は Spring Security の {@link Authentication} から取得する。
 * セッションからユーザーを取り出す必要はない。</p>
 */
@RestController
@RequestMapping("/api/orders")
public class OrderApiController {

    private final OrderService orderService;
    private final UserService userService;

    public OrderApiController(OrderService orderService, UserService userService) {
        this.orderService = orderService;
        this.userService = userService;
    }

    /**
     * すべての注文を取得する。
     *
     * @return 注文リスト
     */
    @GetMapping
    public ResponseEntity<List<Order>> getAllOrders() {
        List<Order> orders = orderService.findAll();
        return ResponseEntity.ok(orders);
    }

    /**
     * 指定されたIDの注文を取得する。
     *
     * @param id 注文ID
     * @return 注文詳細
     */
    @GetMapping("/{id}")
    public ResponseEntity<Order> getOrder(@PathVariable Long id) {
        Order order = orderService.findById(id);
        return ResponseEntity.ok(order);
    }

    /**
     * 注文を作成する。
     *
     * @param request        注文リクエスト
     * @param authentication Spring Security が注入する認証情報
     * @return 作成された注文（Locationヘッダーに新規リソースのURLを含む）
     */
    @PostMapping
    public ResponseEntity<Order> createOrder(
            @Valid @RequestBody OrderRequest request,
            Authentication authentication) {

        // 未認証チェック（SecurityConfig で保護されているが念のため）
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // Spring Security から現在のログインユーザーを取得
        String email = authentication.getName();
        User loginUser = userService.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("ユーザーが見つかりません: " + email));

        Order savedOrder = orderService.createOrder(request, loginUser);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(savedOrder.getId())
                .toUri();

        return ResponseEntity.created(location).body(savedOrder);
    }
}