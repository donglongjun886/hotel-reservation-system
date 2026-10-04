package com.example.hotel.order.dto;

import com.example.hotel.order.entity.HotelOrder;
import com.example.hotel.order.entity.OrderStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class OrderInfo {

    private String orderNo;
    private Long roomTypeId;
    private String roomTypeName;
    private LocalDate checkinDate;
    private LocalDate checkoutDate;
    private Integer nights;
    private String guestName;
    private String guestPhone;
    /** 订单总金额，单位：分 */
    private Long amount;
    private OrderStatus status;
    private LocalDateTime createdAt;

    public static OrderInfo from(HotelOrder order, String roomTypeName) {
        OrderInfo info = new OrderInfo();
        info.orderNo = order.getOrderNo();
        info.roomTypeId = order.getRoomTypeId();
        info.roomTypeName = roomTypeName;
        info.checkinDate = order.getCheckinDate();
        info.checkoutDate = order.getCheckoutDate();
        info.nights = order.getNights();
        info.guestName = order.getGuestName();
        info.guestPhone = order.getGuestPhone();
        info.amount = order.getAmount().movePointRight(2).longValueExact();
        info.status = order.getStatus();
        info.createdAt = order.getCreatedAt();
        return info;
    }

    public String getOrderNo() {
        return orderNo;
    }

    public Long getRoomTypeId() {
        return roomTypeId;
    }

    public String getRoomTypeName() {
        return roomTypeName;
    }

    public LocalDate getCheckinDate() {
        return checkinDate;
    }

    public LocalDate getCheckoutDate() {
        return checkoutDate;
    }

    public Integer getNights() {
        return nights;
    }

    public String getGuestName() {
        return guestName;
    }

    public String getGuestPhone() {
        return guestPhone;
    }

    public Long getAmount() {
        return amount;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
