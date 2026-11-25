package com.example.clostino.controller;

import com.example.clostino.service.ProductService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

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
     * 商品一覧ページ
     */
    @GetMapping("/products")
    public String list(Model model) {
        // Serviceを通じてDBから全商品を取得
        model.addAttribute("products", productService.findAll());
        return "products";
    }
}
// ↓ 以下を既存のProductControllerクラスに追加する
@GetMapping("/products/{id}")
public String showProductDetail(@PathVariable Long id, Model model) {
    Product product = productService.findById(id);
    model.addAttribute("product", product);
    return "product-detail";
}