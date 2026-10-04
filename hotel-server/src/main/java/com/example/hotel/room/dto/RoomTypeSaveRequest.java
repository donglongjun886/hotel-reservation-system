package com.example.hotel.room.dto;

import java.math.BigDecimal;

public class RoomTypeSaveRequest {

    private String name;
    /** 单价，单位：分 */
    private Long price;
    private String description;

    /** 分 → 元换算收敛在 DTO 序列化层 */
    public BigDecimal toPriceYuan() {
        return price == null ? null : BigDecimal.valueOf(price, 2);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getPrice() {
        return price;
    }

    public void setPrice(Long price) {
        this.price = price;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
