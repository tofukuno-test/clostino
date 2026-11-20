package com.example.clostino.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "products")
@Getter
@Setter
@NoArgsConstructor
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private Integer price;
    private String description;
    private String imageUrl;
    private Integer stock;

    // ビジネスロジック
    /**
     * 在庫があるかどうかを判定する
     * @return 在庫がある場合true、ない場合false
     */
    public boolean isInStock() {
        return this.stock != null && this.stock > 0;
    }
}