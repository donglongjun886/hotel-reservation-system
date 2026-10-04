package com.example.hotel.order.dto;

import java.util.List;

/** 入住预检结果（原型 P-A3）：打开订单详情页即展示"可办理/不可办理及原因" */
public class CheckInPrecheckInfo {

    private final boolean pass;
    /** 不可办理的原因列表（文案与办理入住时的报错一致，对齐原型 P-A3）；pass 为 true 时为空 */
    private final List<String> reasons;

    public CheckInPrecheckInfo(boolean pass, List<String> reasons) {
        this.pass = pass;
        this.reasons = reasons;
    }

    public boolean isPass() {
        return pass;
    }

    public List<String> getReasons() {
        return reasons;
    }
}
