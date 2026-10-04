package com.example.hotel.room.dto;

import com.example.hotel.room.entity.RoomType;

public class RoomTypeInfo {

    private Long id;
    private String name;
    /** 单价，单位：分 */
    private Long price;
    private String description;

    public static RoomTypeInfo from(RoomType roomType) {
        RoomTypeInfo info = new RoomTypeInfo();
        info.id = roomType.getId();
        info.name = roomType.getName();
        info.price = roomType.getPrice().movePointRight(2).longValueExact();
        info.description = roomType.getDescription();
        return info;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Long getPrice() {
        return price;
    }

    public String getDescription() {
        return description;
    }
}
