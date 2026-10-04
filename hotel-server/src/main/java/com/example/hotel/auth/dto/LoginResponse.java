package com.example.hotel.auth.dto;

public class LoginResponse {

    private String token;
    private String role;
    private String loginName;

    public LoginResponse(String token, String role, String loginName) {
        this.token = token;
        this.role = role;
        this.loginName = loginName;
    }

    public String getToken() {
        return token;
    }

    public String getRole() {
        return role;
    }

    public String getLoginName() {
        return loginName;
    }
}
