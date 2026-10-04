package com.example.hotel.order.entity;

/** 订单状态机（PRD §6.2）：终态 COMPLETED / CANCELLED 无任何出口流转 */
public enum OrderStatus {

    CONFIRMED,
    CHECKED_IN,
    COMPLETED,
    CANCELLED
}
