package com.example.clostino.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ProductUpdateRequest {

    @NotBlank(message = "商品名は必須です")
    @Size(max = 100, message = "商品名は100文字以内で入力してください")
    private String name;

    @NotNull(message = "価格は必須です")
    @Min(value = 0, message = "価格は0以上を指定してください")
    private Integer price;

    @Size(max = 1000, message = "説明は1000文字以内で入力してください")
    private String description;

    private String imageUrl;

    @NotNull(message = "在庫数は必須です")
    @Min(value = 0, message = "在庫数は0以上を指定してください")
    private Integer stock;
}