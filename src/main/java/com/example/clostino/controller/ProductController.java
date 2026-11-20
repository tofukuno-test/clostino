package com.example.clostino.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 商品コントローラー（Webページ用）
 */
@Controller
public class ProductController {

    /**
     * トップページ（商品一覧へリダイレクト）
     */
    @GetMapping("/")
    public String index() {
        return "redirect:/products";
    }

    // TODO: 商品一覧、商品詳細、検索機能を実装してください

    /**
     * 商品一覧ページ
     */
    @GetMapping("/products")
    public String list(Model model) {
        // 仮の商品データを作成
       List<Map<String, Object>> products = Arrays.asList(
            Map.of(
                "id", 1,
                "name", "オーガニックコットンTシャツ",
                "price", 3980,
                "description", "肌触りの良いオーガニックコットン100%のTシャツです。",
                "imageUrl", "https://placehold.jp/300x200.jpg",
                "stock", 10
            ),
            Map.of(
                "id", 2,
                "name", "リネンブレンドパンツ",
                "price", 5980,
                "description", "通気性に優れたリネン混素材の快適なパンツです。",
                "imageUrl", "https://placehold.jp/300x200.jpg",
                "stock", 5
            ),
            Map.of(
                "id", 3,
                "name", "ウールニットカーディガン",
                "price", 8980,
                "description", "上質なウールを使用した暖かいカーディガンです。",
                "imageUrl", "https://placehold.jp/300x200.jpg",
                "stock", 0
            )
        );

        // Modelに商品データを追加
        model.addAttribute("products", products);

        return "products";
    }
}