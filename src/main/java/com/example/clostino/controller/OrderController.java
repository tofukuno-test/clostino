package com.example.clostino.controller;

import com.example.clostino.entity.Order;
import com.example.clostino.entity.User;
import com.example.clostino.exception.ResourceNotFoundException;
import com.example.clostino.service.OrderService;
import com.example.clostino.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

/**
 * 注文のWeb画面用コントローラー（注文履歴の表示）
 */
@Controller
public class OrderController {

    private final OrderService orderService;
    private final UserService userService;

    public OrderController(OrderService orderService, UserService userService) {
        this.orderService = orderService;
        this.userService = userService;
    }

    /** 注文一覧（注文履歴）画面 GET /orders */
    @GetMapping("/orders")
    public String list(Authentication authentication, Model model) {
        User loginUser = getCurrentUser(authentication);
        List<Order> orders = orderService.findByUser(loginUser);
        model.addAttribute("orders", orders);
        return "orders";
    }

    /** 注文詳細画面 GET /orders/{id} */
    @GetMapping("/orders/{id}")
    public String detail(@PathVariable Long id, Authentication authentication, Model model) {
        User loginUser = getCurrentUser(authentication);
        Order order = orderService.findById(id);

        // 他ユーザーの注文は閲覧不可
        if (!order.getUser().getId().equals(loginUser.getId())) {
            return "redirect:/orders";
        }

        model.addAttribute("order", order);
        return "order-detail";
    }

    private User getCurrentUser(Authentication authentication) {
        String email = authentication.getName();
        return userService.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("ユーザーが見つかりません: " + email));
    }
}