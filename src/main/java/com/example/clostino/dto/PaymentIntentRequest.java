package com.example.clostino.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Stripe PaymentIntent 作成リクエスト用 DTO。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentIntentRequest {

    /** 決済金額（円） */
    private BigDecimal amount;

    /** 通貨コード（例: "jpy"） */
    private String currency;

    /** 商品説明（Stripe ダッシュボードに表示される） */
    private String description;
}