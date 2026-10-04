package com.example.hotel.room.dto;

/** 各房型日期区间剩余可订量（P-C1 列表页，公开接口） */
public class RoomTypeAvailability {

    private Long roomTypeId;
    /** 区间 [checkin, checkout) 剩余可订量（逐日最小值） */
    private Integer available;

    public RoomTypeAvailability(Long roomTypeId, Integer available) {
        this.roomTypeId = roomTypeId;
        this.available = available;
    }

    public Long getRoomTypeId() {
        return roomTypeId;
    }

    public Integer getAvailable() {
        return available;
    }
}
