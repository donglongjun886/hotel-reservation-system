package com.example.hotel.order.dto;

import java.time.LocalDate;

/** 创建预订入参；不含 userId——订单归属一律取 token 中的 user_id（技术方案 §5.4） */
public class CreateOrderRequest {

    /** 客户端生成的幂等请求号（UUID），重复提交/重试时服务端据此去重 */
    private String requestNo;
    private Long roomTypeId;
    private LocalDate checkinDate;
    private LocalDate checkoutDate;
    private String guestName;
    private String guestPhone;

    public String getRequestNo() {
        return requestNo;
    }

    public void setRequestNo(String requestNo) {
        this.requestNo = requestNo;
    }

    public Long getRoomTypeId() {
        return roomTypeId;
    }

    public void setRoomTypeId(Long roomTypeId) {
        this.roomTypeId = roomTypeId;
    }

    public LocalDate getCheckinDate() {
        return checkinDate;
    }

    public void setCheckinDate(LocalDate checkinDate) {
        this.checkinDate = checkinDate;
    }

    public LocalDate getCheckoutDate() {
        return checkoutDate;
    }

    public void setCheckoutDate(LocalDate checkoutDate) {
        this.checkoutDate = checkoutDate;
    }

    public String getGuestName() {
        return guestName;
    }

    public void setGuestName(String guestName) {
        this.guestName = guestName;
    }

    public String getGuestPhone() {
        return guestPhone;
    }

    public void setGuestPhone(String guestPhone) {
        this.guestPhone = guestPhone;
    }
}
