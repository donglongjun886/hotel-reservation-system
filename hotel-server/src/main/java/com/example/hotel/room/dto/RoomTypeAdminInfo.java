package com.example.hotel.room.dto;

import com.example.hotel.room.entity.RoomType;

public class RoomTypeAdminInfo {

    private Long id;
    private String name;
    /** 单价，单位：分 */
    private Long price;
    private String description;
    /** 当前房间数（P-A4 表格列） */
    private Integer roomCount;

    public static RoomTypeAdminInfo from(RoomType roomType, int roomCount) {
        RoomTypeAdminInfo info = new RoomTypeAdminInfo();
        info.id = roomType.getId();
        info.name = roomType.getName();
        info.price = roomType.getPrice().movePointRight(2).longValueExact();
        info.description = roomType.getDescription();
        info.roomCount = roomCount;
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

    public Integer getRoomCount() {
        return roomCount;
    }
}
