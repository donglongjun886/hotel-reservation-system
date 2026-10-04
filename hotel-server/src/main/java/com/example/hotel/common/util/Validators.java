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
}
