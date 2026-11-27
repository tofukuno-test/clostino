package com.example.clostino.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * チェックアウト（注文手続き）フォーム用DTO。
 * 配送先住所を入力する。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CheckoutForm {

    /** 配送先住所 */
    @NotBlank(message = "配送先住所は必須です")
    private String shippingAddress;
}