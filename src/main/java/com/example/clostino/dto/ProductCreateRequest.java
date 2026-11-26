package com.example.clostino.dto;

import lombok.Data;

@Data
public class ProductCreateRequest {

    private String name;
    private Integer price;
    private String description;
    private String imageUrl;
    private Integer stock;
}