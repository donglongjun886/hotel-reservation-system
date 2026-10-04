package com.example.hotel.common.context;

public record LoginUser(Long id, String loginName, String role) {

    public static final String ROLE_GUEST = "GUEST";
    public static final String ROLE_ADMIN = "ADMIN";

    public boolean isAdmin() {
        return ROLE_ADMIN.equals(role);
    }
}
