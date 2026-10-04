package com.example.hotel.auth.dto;

public class UserInfo {

    private Long id;
    private String loginName;
    private String role;

    public UserInfo(Long id, String loginName, String role) {
        this.id = id;
        this.loginName = loginName;
        this.role = role;
    }

    public Long getId() {
        return id;
    }

    public String getLoginName() {
        return loginName;
    }

    public String getRole() {
        return role;
    }
}
