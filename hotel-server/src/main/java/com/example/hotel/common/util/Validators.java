package com.example.hotel.common.util;

import java.util.regex.Pattern;

public final class Validators {

    private static final Pattern PHONE = Pattern.compile("^1[3-9]\\d{9}$");
    private static final Pattern ID_CARD = Pattern.compile("^\\d{17}[\\dXx]$");

    private Validators() {
    }

    public static boolean isPhone(String value) {
        return value != null && PHONE.matcher(value).matches();
    }

    public static boolean isIdCard(String value) {
        return value != null && ID_CARD.matcher(value).matches();
    }

    /** 分页参数：page ≥ 1，pageSize 1~50（超出上限按恶意/异常请求拒绝，防止全量拉取） */
    public static boolean isValidPage(int page, int pageSize) {
        return page >= 1 && pageSize >= 1 && pageSize <= 50;
    }
}
