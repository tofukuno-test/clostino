package com.example.clostino.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

/**
 * 注文リクエストDTO。
 * 注文作成APIのリクエストボディを表す。
 */
@Data
@NoArgsConstructor
public class OrderRequest {

    /** 配送先住所 */
    @NotBlank(message = "配送先住所は必須です")
    private String shippingAddress;

    /** 注文商品リスト */
    @NotEmpty(message = "注文明細は1件以上必要です")
    @Valid
    private List<OrderItemRequest> items;
}