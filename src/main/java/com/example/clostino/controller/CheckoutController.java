package com.example.clostino.controller;

import com.example.clostino.dto.CartItemDisplay;
import com.example.clostino.dto.CheckoutForm;
import com.example.clostino.dto.OrderItemRequest;
import com.example.clostino.dto.OrderRequest;
import com.example.clostino.dto.PaymentIntentRequest;
import com.example.clostino.entity.Order;
import com.example.clostino.entity.Product;
import com.example.clostino.entity.User;
import com.example.clostino.exception.ResourceNotFoundException;
import com.example.clostino.service.OrderEmailService;
import com.example.clostino.service.OrderService;
import com.example.clostino.service.ProductService;
import com.example.clostino.service.StripeService;
import com.example.clostino.service.UserService;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/checkout")
public class CheckoutController {

    private static final String SESSION_CART = "cart";
    private static final String SESSION_CHECKOUT_SHIPPING = "checkoutShippingAddress";
    private static final String SESSION_PAYMENT_METHOD = "checkoutPaymentMethodId";

    private final ProductService productService;
    private final OrderService orderService;
    private final UserService userService;
    private final StripeService stripeService;
    private final OrderEmailService orderEmailService;
    private final String publishableKey;

    public CheckoutController(
            ProductService productService,
            OrderService orderService,
            UserService userService,
            StripeService stripeService,
            OrderEmailService orderEmailService,
            @Value("${stripe.publishable.key}") String publishableKey) {
        this.productService = productService;
        this.orderService = orderService;
        this.userService = userService;
        this.stripeService = stripeService;
        this.orderEmailService = orderEmailService;
        this.publishableKey = publishableKey;
    }

    @GetMapping
    public String showCheckout(Authentication authentication, HttpSession session,
                               Model model, RedirectAttributes redirectAttributes) {
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

        model.addAttribute("cartItems", checkoutData.items());
        model.addAttribute("subtotal", checkoutData.subtotal());
        model.addAttribute("tax", checkoutData.tax());
        model.addAttribute("total", checkoutData.total());
        // Stripe.js の初期化に使う公開可能キーをテンプレートへ渡す
        model.addAttribute("publishableKey", publishableKey);

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
            @RequestParam(value = "paymentMethodId", required = false) String paymentMethodId,
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
                model.addAttribute("cartItems", checkoutData.items());
                model.addAttribute("subtotal", checkoutData.subtotal());
                model.addAttribute("tax", checkoutData.tax());
                model.addAttribute("total", checkoutData.total());
            }
            model.addAttribute("publishableKey", publishableKey);
            return "checkout";
        }

        session.setAttribute(SESSION_CHECKOUT_SHIPPING, form.getShippingAddress());
        // PaymentMethod ID をセッションに保存しておき、決済完了時に使用する
        if (paymentMethodId != null && !paymentMethodId.isBlank()) {
            session.setAttribute(SESSION_PAYMENT_METHOD, paymentMethodId);
        }
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
        String paymentMethodId = (String) session.getAttribute(SESSION_PAYMENT_METHOD);
        Map<Long, Integer> cart = getCart(session);

        if (shippingAddress == null || cart == null || cart.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "セッションが切れました。最初からやり直してください。");
            return "redirect:/cart";
        }

        try {
            // 合計金額を計算する
            CheckoutData checkoutData = buildCheckoutData(cart);
            if (checkoutData == null) {
                redirectAttributes.addFlashAttribute("error", "カート内の商品情報を取得できませんでした。");
                return "redirect:/cart";
            }

            if (paymentMethodId != null && !paymentMethodId.isBlank()) {
                // Stripe に PaymentIntent を作成して決済を実行する
                PaymentIntentRequest piRequest = new PaymentIntentRequest(
                        BigDecimal.valueOf(checkoutData.total()),
                        "jpy",
                        "Clostino 注文 - " + loginUser.getEmail()
                );
                PaymentIntent paymentIntent = stripeService.createPaymentIntent(piRequest);

                // テストモードでは succeeded または requires_action になる
                if (!"succeeded".equals(paymentIntent.getStatus())
                        && !"requires_capture".equals(paymentIntent.getStatus())) {
                    redirectAttributes.addFlashAttribute("error",
                            "決済に失敗しました（ステータス: " + paymentIntent.getStatus() + "）");
                    return "redirect:/checkout/confirm";
                }
            } else {
                // PaymentMethod ID がない場合はモックとして 2 秒待機する（後方互換）
                Thread.sleep(2000);
            }

            // 注文を作成する
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

            // 注文確認メールを送信する（失敗しても注文処理は継続される）
            orderEmailService.sendOrderConfirmation(order, loginUser.getEmail());

            session.removeAttribute(SESSION_CART);
            session.removeAttribute(SESSION_CHECKOUT_SHIPPING);
            session.removeAttribute(SESSION_PAYMENT_METHOD);

            redirectAttributes.addFlashAttribute("message", "ご注文ありがとうございます！確認メールをお送りしました。");
            return "redirect:/orders/" + order.getId();

        } catch (StripeException e) {
            redirectAttributes.addFlashAttribute("error", "決済処理中にエラーが発生しました: " + e.getMessage());
            return "redirect:/checkout/confirm";
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