package com.example.hotel.common.api;

public enum ErrorCode {

    SOLD_OUT(1001, "该房型所选日期已订满"),
    PHONE_REGISTERED(1002, "手机号已注册"),
    CHECKIN_NOT_ALLOWED(1003, "不满足入住条件"),
    PARAM_INVALID(1004, "参数校验失败"),
    LOGIN_FAILED(1005, "手机号或密码错误"),
    ORDER_STATUS_CHANGED(1006, "订单状态已变更，请刷新查看"),
    NOT_FOUND(1007, "数据不存在"),
    SYSTEM_ERROR(5000, "系统异常，请稍后重试");

    private final int code;
    private final String defaultMessage;

    ErrorCode(int code, String defaultMessage) {
        this.code = code;
        this.defaultMessage = defaultMessage;
    }

    public int getCode() {
        return code;
    }

    public String getDefaultMessage() {
        return defaultMessage;
    }
}
