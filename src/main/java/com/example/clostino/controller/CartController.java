package com.example.clostino.controller;

import com.example.clostino.dto.CartItemDisplay;
import com.example.clostino.entity.Product;
import com.example.clostino.service.ProductService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.*;

@Controller
public class CartController {

    private static final String SESSION_CART = "cart";
    private final ProductService productService;

    public CartController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping("/cart")
    public String viewCart(HttpSession session, Model model) {
        @SuppressWarnings("unchecked")
        Map<Long, Integer> cart = (Map<Long, Integer>) session.getAttribute(SESSION_CART);
        if (cart == null || cart.isEmpty()) {
            model.addAttribute("cartItems", List.<CartItemDisplay>of());
            model.addAttribute("total", 0);
            return "cart";
        }
        List<CartItemDisplay> items = new ArrayList<>();
        int total = 0;
        for (Map.Entry<Long, Integer> entry : cart.entrySet()) {
            Product product = productService.findById(entry.getKey());
            int qty = entry.getValue();
            items.add(new CartItemDisplay(product, qty));
            total += product.getPrice() * qty;
        }
        model.addAttribute("cartItems", items);
        model.addAttribute("total", total);
        return "cart";
    }

    @PostMapping("/cart/add")
    public String addToCart(@RequestParam Long productId,
                            @RequestParam(defaultValue = "1") Integer quantity,
                            HttpSession session, RedirectAttributes redirectAttributes) {
        if (quantity == null || quantity < 1) quantity = 1;
        // 商品を取得し、在庫をチェックする（ProductService#findById は「トランザクション管理を理解する」パートで実装済み）
        Product product = productService.findById(productId);
        if (product.getStock() < quantity) {
            redirectAttributes.addFlashAttribute("error", "在庫が不足しています。");
            return "redirect:/products/" + productId;
        }
        @SuppressWarnings("unchecked")
        Map<Long, Integer> cart = (Map<Long, Integer>) session.getAttribute(SESSION_CART);
        if (cart == null) {
            cart = new LinkedHashMap<>();
            session.setAttribute(SESSION_CART, cart);
        }
        cart.merge(productId, quantity, Integer::sum);
        redirectAttributes.addFlashAttribute("message", product.getName() + " をカートに追加しました。");
        return "redirect:/cart";
    }

    @PostMapping("/cart/update")
    public String updateCart(@RequestParam Long productId,
                             @RequestParam Integer quantity,
                             HttpSession session, RedirectAttributes redirectAttributes) {
        @SuppressWarnings("unchecked")
        Map<Long, Integer> cart = (Map<Long, Integer>) session.getAttribute(SESSION_CART);
        if (cart == null || !cart.containsKey(productId)) {
            return "redirect:/cart";
        }
        // 数量が0以下の場合はカートから削除する
        if (quantity <= 0) {
            cart.remove(productId);
            redirectAttributes.addFlashAttribute("message", "商品をカートから削除しました。");
            return "redirect:/cart";
        }
        // 在庫数を超える数量は設定できない
        Product product = productService.findById(productId);
        if (quantity > product.getStock()) {
            redirectAttributes.addFlashAttribute("error",
                    "「" + product.getName() + "」の在庫が不足しています。（在庫数: " + product.getStock() + "）");
            return "redirect:/cart";
        }
        cart.put(productId, quantity);
        redirectAttributes.addFlashAttribute("message", "数量を更新しました。");
        return "redirect:/cart";
    }

    @PostMapping("/cart/remove")
    public String removeFromCart(@RequestParam Long productId,
                                 HttpSession session, RedirectAttributes redirectAttributes) {
        @SuppressWarnings("unchecked")
        Map<Long, Integer> cart = (Map<Long, Integer>) session.getAttribute(SESSION_CART);
        if (cart != null) {
            cart.remove(productId);
        }
        redirectAttributes.addFlashAttribute("message", "商品をカートから削除しました。");
        return "redirect:/cart";
    }

    @PostMapping("/cart/clear")
    public String clearCart(HttpSession session) {
        session.removeAttribute(SESSION_CART);
        return "redirect:/cart";
    }
}