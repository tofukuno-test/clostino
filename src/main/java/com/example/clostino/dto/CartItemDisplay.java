package com.example.clostino.dto;

import com.example.clostino.entity.Product;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * カート表示用の1件分（商品＋数量）
 */
@Getter
@AllArgsConstructor
public class CartItemDisplay {

    private final Product product;
    private final int quantity;

    /** 小計（税抜） */
    public int getSubtotal() {
        return product.getPrice() * quantity;
    }
}