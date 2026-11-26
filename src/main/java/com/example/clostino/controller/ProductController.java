package com.example.clostino.controller;

import com.example.clostino.entity.Product;
import com.example.clostino.service.ProductService;
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

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping("/")
    public String index() {
        return "redirect:/products";
    }

    /**
     * 商品一覧・検索ページ（ページネーション対応）
     */
    @GetMapping("/products")
    public String listProducts(
            @RequestParam(required = false) String keyword,
            @PageableDefault(size = 12, sort = "id") Pageable pageable,
            Model model) {

        Page<Product> productPage;
        if (keyword != null && !keyword.isEmpty()) {
            productPage = productService.searchProducts(keyword, pageable);
        } else {
            productPage = productService.findAll(pageable);
        }

        model.addAttribute("products", productPage.getContent());
        model.addAttribute("page", productPage);
        model.addAttribute("keyword", keyword);
        return "products";
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