package com.example.hotel.auth;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.hotel.auth.dto.LoginRequest;
import com.example.hotel.auth.dto.LoginResponse;
import com.example.hotel.auth.dto.RegisterRequest;
import com.example.hotel.auth.dto.UserInfo;
import com.example.hotel.auth.entity.AuthToken;
import com.example.hotel.auth.entity.User;
import com.example.hotel.auth.mapper.AuthTokenMapper;
import com.example.hotel.auth.mapper.UserMapper;
import com.example.hotel.common.api.ErrorCode;
import com.example.hotel.common.context.LoginUser;
import com.example.hotel.common.context.UserContext;
import com.example.hotel.common.exception.BizException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class AuthService {

    private static final long TOKEN_VALID_DAYS = 7;

    private final UserMapper userMapper;
    private final AuthTokenMapper authTokenMapper;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public AuthService(UserMapper userMapper, AuthTokenMapper authTokenMapper) {
        this.userMapper = userMapper;
        this.authTokenMapper = authTokenMapper;
    }

    public void register(RegisterRequest request) {
        Long count = userMapper.selectCount(new QueryWrapper<User>().eq("login_name", request.getPhone()));
        if (count > 0) {
            throw new BizException(ErrorCode.PHONE_REGISTERED);
        }
        User user = new User();
        user.setLoginName(request.getPhone());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRole(LoginUser.ROLE_GUEST);
        try {
            userMapper.insert(user);
        } catch (DuplicateKeyException e) {
            // 并发注册同一手机号撞唯一索引：文案与先查一致（原型 P-C4）
            throw new BizException(ErrorCode.PHONE_REGISTERED);
        }
    }

    public LoginResponse login(LoginRequest request) {
        User user = userMapper.selectOne(new QueryWrapper<User>().eq("login_name", request.getLoginName()));
        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new BizException(ErrorCode.LOGIN_FAILED);
        }
        AuthToken authToken = new AuthToken();
        authToken.setToken(UUID.randomUUID().toString());
        authToken.setUserId(user.getId());
        authToken.setExpiresAt(LocalDateTime.now().plusDays(TOKEN_VALID_DAYS));
        authTokenMapper.insert(authToken);
        return new LoginResponse(authToken.getToken(), user.getRole(), user.getLoginName());
    }

    public void logout(String token) {
        if (token != null && !token.isBlank()) {
            authTokenMapper.deleteById(token);
        }
    }

    public UserInfo currentUser() {
        LoginUser loginUser = UserContext.require();
        return new UserInfo(loginUser.id(), loginUser.loginName(), loginUser.role());
    }

    public LoginUser resolveUser(String token) {
        AuthToken authToken = authTokenMapper.selectById(token);
        if (authToken == null) {
            return null;
        }
        if (authToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            authTokenMapper.deleteById(token);
            return null;
        }
        User user = userMapper.selectById(authToken.getUserId());
        if (user == null) {
            return null;
        }
        return new LoginUser(user.getId(), user.getLoginName(), user.getRole());
    }
}
