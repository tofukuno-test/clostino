package com.example.clostino.entity;

/**
 * 注文ステータス
 */
public enum OrderStatus {
    PENDING("注文受付"),
    CONFIRMED("注文確定"),
    SHIPPED("発送済み"),
    DELIVERED("配達完了"),
    CANCELLED("キャンセル");

    private final String displayName;

    OrderStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}