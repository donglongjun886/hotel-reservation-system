package com.example.hotel.room.dto;

import com.example.hotel.room.entity.Room;

public class RoomAdminInfo {

    private Long id;
    private String roomNo;
    private Long roomTypeId;
    private String roomTypeName;
    /** 当前状态：true = 入住中（被已入住订单占用），false = 空闲 */
    private Boolean occupied;

    public static RoomAdminInfo from(Room room, String roomTypeName) {
        RoomAdminInfo info = new RoomAdminInfo();
        info.id = room.getId();
        info.roomNo = room.getRoomNo();
        info.roomTypeId = room.getRoomTypeId();
        info.roomTypeName = roomTypeName;
        info.occupied = Boolean.TRUE.equals(room.getOccupied());
        return info;
    }

    public Long getId() {
        return id;
    }

    public String getRoomNo() {
        return roomNo;
    }

    public Long getRoomTypeId() {
        return roomTypeId;
    }

    public String getRoomTypeName() {
        return roomTypeName;
    }

    public Boolean getOccupied() {
        return occupied;
    }
}
