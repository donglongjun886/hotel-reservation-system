package com.example.hotel.order.dto;

import com.example.hotel.room.entity.Room;

public class AssignableRoomInfo {

    private Long id;
    private String roomNo;

    public static AssignableRoomInfo from(Room room) {
        AssignableRoomInfo info = new AssignableRoomInfo();
        info.id = room.getId();
        info.roomNo = room.getRoomNo();
        return info;
    }

    public Long getId() {
        return id;
    }

    public String getRoomNo() {
        return roomNo;
    }
}
