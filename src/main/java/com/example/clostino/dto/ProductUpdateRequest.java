package com.example.clostino.dto;

import lombok.Data;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Data
public class ProductUpdateRequest {
    @NotBlank(message = "商品名は必須です")
    private String name;

    @NotNull(message = "価格は必須です")
    @Min(value = 0, message = "価格は0以上を指定してください")
    private Integer price;

    private String description;
    private String imageUrl;

    @NotNull(message = "在庫数は必須です")
    @Min(value = 0, message = "在庫数は0以上を指定してください")
    private Integer stock;
}