package com.example.hotel.common.context;

public final class UserContext {

    private static final ThreadLocal<LoginUser> HOLDER = new ThreadLocal<>();

    private UserContext() {
    }

    public static void set(LoginUser user) {
        HOLDER.set(user);
    }

    public static LoginUser get() {
        return HOLDER.get();
    }

    public static LoginUser require() {
        LoginUser user = HOLDER.get();
        if (user == null) {
            throw new IllegalStateException("当前请求无登录用户");
        }
        return user;
    }

    public static void clear() {
        HOLDER.remove();
    }
}
