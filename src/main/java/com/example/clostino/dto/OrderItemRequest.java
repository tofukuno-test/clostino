package com.example.clostino.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 注文明細リクエストDTO。
 * 注文する商品と数量を表す。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemRequest {

    /** 商品ID */
    @NotNull(message = "商品IDは必須です")
    private Long productId;

    /** 数量 */
    @NotNull(message = "数量は必須です")
    @Min(value = 1, message = "数量は1以上を指定してください")
    private Integer quantity;
}