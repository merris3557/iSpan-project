package com.example.demo.admin;

public enum AdminPosition {
    SUPER_ADMIN("總管理員"),
    HUMAN_RESOURCE("人事"),
    CUSTOMER_SERVICE("客服"),
    SHOP_MANAGER("電商");

    private final String description;

    AdminPosition(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
