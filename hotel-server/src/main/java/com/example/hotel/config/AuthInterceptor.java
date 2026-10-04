package com.example.hotel.config;

import com.example.hotel.auth.AuthService;
import com.example.hotel.common.context.LoginUser;
import com.example.hotel.common.context.UserContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    private final AuthService authService;

    public AuthInterceptor(AuthService authService) {
        this.authService = authService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {
        String path = request.getRequestURI();
        if (isPublic(request, path)) {
            return true;
        }
        String token = request.getHeader("X-Token");
        LoginUser user = token != null && !token.isBlank() ? authService.resolveUser(token) : null;
        if (user == null) {
            writeError(response, HttpServletResponse.SC_UNAUTHORIZED, "请先登录");
            return false;
        }
        if (path.startsWith("/api/admin/") && !user.isAdmin()) {
            writeError(response, HttpServletResponse.SC_FORBIDDEN, "无权限访问");
            return false;
        }
        UserContext.set(user);
        return true;
    }

    private boolean isPublic(HttpServletRequest request, String path) {
        if (path.equals("/api/auth/login") || path.equals("/api/auth/register")) {
            return true;
        }
        return HttpMethod.GET.matches(request.getMethod()) && path.startsWith("/api/room-types");
    }

    private void writeError(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json;charset=UTF-8");
        response.getOutputStream().write(
                ("{\"code\":" + status + ",\"message\":\"" + message + "\",\"data\":null}")
                        .getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        UserContext.clear();
    }
}
