package com.example.clostino.controller;

import com.example.clostino.entity.Product;
import com.example.clostino.service.ProductService;
import jakarta.servlet.http.HttpSession;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ProductController {

    private final ProductService productService;

    // セッションに保存するキーの定数
    private static final String SESSION_KEY_KEYWORD = "searchKeyword";

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping("/")
    public String index() {
        return "redirect:/products";
    }

    /**
     * 商品一覧・検索ページ（ページネーション・セッション維持対応）
     */
    @GetMapping("/products")
    public String listProducts(
            @RequestParam(required = false) String keyword,
            @PageableDefault(size = 12, sort = "id") Pageable pageable,
            HttpSession session,
            Model model) {

        // 検索キーワードの決定ロジック
        String effectiveKeyword = determineKeyword(keyword, session);

        Page<Product> productPage;
        if (effectiveKeyword != null && !effectiveKeyword.isEmpty()) {
            productPage = productService.searchProducts(effectiveKeyword, pageable);
        } else {
            productPage = productService.findAll(pageable);
        }

        model.addAttribute("products", productPage.getContent());
        model.addAttribute("page", productPage);
        model.addAttribute("keyword", effectiveKeyword);
        return "products";
    }

    /**
     * 検索キーワードを決定し、セッションを更新する
     */
    private String determineKeyword(String requestKeyword, HttpSession session) {
        if (requestKeyword != null) {
            // URLパラメータでキーワードが指定された場合
            if (requestKeyword.isEmpty()) {
                // 空文字の場合はクリア（「クリア」ボタンが押された）
                session.removeAttribute(SESSION_KEY_KEYWORD);
                return null;
            } else {
                // キーワードがある場合はセッションに保存
                session.setAttribute(SESSION_KEY_KEYWORD, requestKeyword);
                return requestKeyword;
            }
        } else {
            // URLパラメータがない場合はセッションから復元
            return (String) session.getAttribute(SESSION_KEY_KEYWORD);
        }
    }

    /**
     * 商品詳細ページ
     */
    @GetMapping("/products/{id}")
    public String showProductDetail(@PathVariable Long id, Model model) {
        Product product = productService.findById(id)
                .orElseThrow(() -> new RuntimeException("商品が見つかりません: id=" + id));
        model.addAttribute("product", product);
        return "product-detail";
    }
}