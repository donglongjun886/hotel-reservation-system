package com.example.hotel.order.dto;

public class CheckInRequest {

    /** 住客身份证号（18 位，末位可为 X） */
    private String idCard;
    /** 分配的房间号 */
    private String roomNo;

    public String getIdCard() {
        return idCard;
    }

    public void setIdCard(String idCard) {
        this.idCard = idCard;
    }

    public String getRoomNo() {
        return roomNo;
    }

    public void setRoomNo(String roomNo) {
        this.roomNo = roomNo;
    }
}
