package com.example.clostino.controller;

import com.example.clostino.dto.CartItemDisplay;
import com.example.clostino.dto.CheckoutForm;
import com.example.clostino.dto.OrderItemRequest;
import com.example.clostino.dto.OrderRequest;
import com.example.clostino.entity.Order;
import com.example.clostino.entity.Product;
import com.example.clostino.entity.User;
import com.example.clostino.exception.ResourceNotFoundException;
import com.example.clostino.service.OrderEmailService;
import com.example.clostino.service.OrderService;
import com.example.clostino.service.ProductService;
import com.example.clostino.service.UserService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/checkout")
public class CheckoutController {

    private static final String SESSION_CART = "cart";
    private static final String SESSION_CHECKOUT_SHIPPING = "checkoutShippingAddress";

    private final ProductService productService;
    private final OrderService orderService;
    private final UserService userService;
    private final OrderEmailService orderEmailService;

    public CheckoutController(ProductService productService, OrderService orderService,
                              UserService userService, OrderEmailService orderEmailService) {
        this.productService = productService;
        this.orderService = orderService;
        this.userService = userService;
        this.orderEmailService = orderEmailService;
    }

    @GetMapping
    public String showCheckout(Authentication authentication, HttpSession session,
                               Model model, RedirectAttributes redirectAttributes) {
        // Spring Security が認証済みであることを保証する
        // （SecurityConfig で /checkout は anyRequest().authenticated() に含まれるため自動リダイレクト）
        User loginUser = getCurrentUser(authentication);

        Map<Long, Integer> cart = getCart(session);
        if (cart == null || cart.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "カートが空です。商品を追加してからお進みください。");
            return "redirect:/cart";
        }

        CheckoutData checkoutData = buildCheckoutData(cart);
        if (checkoutData == null) {
            redirectAttributes.addFlashAttribute("error", "カート内の商品情報を取得できませんでした。");
            return "redirect:/cart";
        }

        model.addAttribute("cartItems", checkoutData.items);
        model.addAttribute("subtotal", checkoutData.subtotal);
        model.addAttribute("tax", checkoutData.tax);
        model.addAttribute("total", checkoutData.total);

        if (!model.containsAttribute("checkoutForm")) {
            String defaultAddress = loginUser.getAddress();
            model.addAttribute("checkoutForm", new CheckoutForm(defaultAddress != null ? defaultAddress : ""));
        }

        return "checkout";
    }

    @PostMapping
    public String confirmCheckout(
            @Valid @ModelAttribute("checkoutForm") CheckoutForm form,
            BindingResult bindingResult,
            Authentication authentication,
            HttpSession session,
            Model model,
            RedirectAttributes redirectAttributes) {

        Map<Long, Integer> cart = getCart(session);
        if (cart == null || cart.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "カートが空です。");
            return "redirect:/cart";
        }

        if (bindingResult.hasErrors()) {
            CheckoutData checkoutData = buildCheckoutData(cart);
            if (checkoutData != null) {
                model.addAttribute("cartItems", checkoutData.items);
                model.addAttribute("subtotal", checkoutData.subtotal);
                model.addAttribute("tax", checkoutData.tax);
                model.addAttribute("total", checkoutData.total);
            }
            return "checkout";
        }

        session.setAttribute(SESSION_CHECKOUT_SHIPPING, form.getShippingAddress());
        return "redirect:/checkout/confirm";
    }

    @GetMapping("/confirm")
    public String showConfirm(Authentication authentication, HttpSession session,
                              Model model, RedirectAttributes redirectAttributes) {
        User loginUser = getCurrentUser(authentication);

        String shippingAddress = (String) session.getAttribute(SESSION_CHECKOUT_SHIPPING);
        if (shippingAddress == null || shippingAddress.isBlank()) {
            redirectAttributes.addFlashAttribute("error", "配送先情報がありません。最初からやり直してください。");
            return "redirect:/checkout";
        }

        Map<Long, Integer> cart = getCart(session);
        if (cart == null || cart.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "カートが空です。");
            return "redirect:/cart";
        }

        CheckoutData checkoutData = buildCheckoutData(cart);
        if (checkoutData == null) {
            redirectAttributes.addFlashAttribute("error", "カート内の商品情報を取得できませんでした。");
            return "redirect:/cart";
        }

        model.addAttribute("cartItems", checkoutData.items);
        model.addAttribute("subtotal", checkoutData.subtotal);
        model.addAttribute("tax", checkoutData.tax);
        model.addAttribute("total", checkoutData.total);
        model.addAttribute("shippingAddress", shippingAddress);
        model.addAttribute("customerName", loginUser.getName());

        return "checkout-confirm";
    }

    @PostMapping("/complete")
    public String completeCheckout(Authentication authentication, HttpSession session,
                                   RedirectAttributes redirectAttributes) {
        User loginUser = getCurrentUser(authentication);

        String shippingAddress = (String) session.getAttribute(SESSION_CHECKOUT_SHIPPING);
        Map<Long, Integer> cart = getCart(session);

        if (shippingAddress == null || cart == null || cart.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "セッションが切れました。最初からやり直してください。");
            return "redirect:/cart";
        }

        try {
            // 決済処理のシミュレーション（実際の決済ゲートウェイ呼び出しを模倣）
            Thread.sleep(2000);

            OrderRequest orderRequest = new OrderRequest();
            orderRequest.setShippingAddress(shippingAddress);
            List<OrderItemRequest> items = new ArrayList<>();
            for (Map.Entry<Long, Integer> entry : cart.entrySet()) {
                OrderItemRequest item = new OrderItemRequest();
                item.setProductId(entry.getKey());
                item.setQuantity(entry.getValue());
                items.add(item);
            }
            orderRequest.setItems(items);

            Order order = orderService.createOrder(orderRequest, loginUser);

            // 注文確認メールを送信（Mailtrap のダッシュボードで受信を確認できる）
            orderEmailService.sendOrderConfirmation(order, loginUser.getEmail());

            session.removeAttribute(SESSION_CART);
            session.removeAttribute(SESSION_CHECKOUT_SHIPPING);

            redirectAttributes.addFlashAttribute("message", "ご注文ありがとうございます！注文確認メールをお送りしました。");
            return "redirect:/orders/" + order.getId();

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            redirectAttributes.addFlashAttribute("error", "決済処理中にエラーが発生しました。");
            return "redirect:/checkout/confirm";
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/checkout/confirm";
        } catch (ResourceNotFoundException e) {
            redirectAttributes.addFlashAttribute("error", "カート内の商品が見つかりません。カートを確認してください。");
            return "redirect:/cart";
        }
    }

    private User getCurrentUser(Authentication authentication) {
        String email = authentication.getName();
        return userService.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("ユーザーが見つかりません: " + email));
    }

    @SuppressWarnings("unchecked")
    private Map<Long, Integer> getCart(HttpSession session) {
        return (Map<Long, Integer>) session.getAttribute(SESSION_CART);
    }

    private CheckoutData buildCheckoutData(Map<Long, Integer> cart) {
        List<CartItemDisplay> items = new ArrayList<>();
        int subtotal = 0;
        for (Map.Entry<Long, Integer> entry : cart.entrySet()) {
            try {
                Product product = productService.findById(entry.getKey());
                int qty = entry.getValue();
                items.add(new CartItemDisplay(product, qty));
                subtotal += product.getPrice() * qty;
            } catch (ResourceNotFoundException e) {
                return null;
            }
        }
        int tax = (int) (subtotal * 0.1);
        int total = subtotal + tax;
        return new CheckoutData(items, subtotal, tax, total);
    }

    private record CheckoutData(List<CartItemDisplay> items, int subtotal, int tax, int total) {
    }
}