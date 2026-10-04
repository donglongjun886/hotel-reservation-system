package com.example.hotel.auth;

import com.example.hotel.auth.dto.LoginRequest;
import com.example.hotel.auth.dto.LoginResponse;
import com.example.hotel.auth.dto.RegisterRequest;
import com.example.hotel.auth.dto.UserInfo;
import com.example.hotel.common.api.Result;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public Result<Void> register(@Valid @RequestBody RegisterRequest request) {
        authService.register(request);
        return Result.ok();
    }

    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return Result.ok(authService.login(request));
    }

    @PostMapping("/logout")
    public Result<Void> logout(HttpServletRequest request) {
        authService.logout(request.getHeader("X-Token"));
        return Result.ok();
    }

    @GetMapping("/me")
    public Result<UserInfo> me() {
        return Result.ok(authService.currentUser());
    }
}
