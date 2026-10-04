package com.example.hotel.auth;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.hotel.auth.entity.AuthToken;
import com.example.hotel.auth.entity.User;
import com.example.hotel.auth.mapper.AuthTokenMapper;
import com.example.hotel.auth.mapper.UserMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ThreadLocalRandom;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * HTTP/鉴权层用例：TC-A03（重复注册）、TC-A04（错误密码）、TC-A07/A08（401/403）、
 * TC-A10（登出失效）、TC-H01（统一响应体）、TC-H02（密码 BCrypt）、TC-H04（token 过期）。
 * 与 service 层测试互补：验证拦截器、统一响应包装与 HTTP 状态码约定。
 */
@SpringBootTest
@AutoConfigureMockMvc
class AuthApiTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private AuthTokenMapper authTokenMapper;

    private final List<String> createdPhones = new CopyOnWriteArrayList<>();

    @AfterEach
    void cleanup() {
        if (!createdPhones.isEmpty()) {
            List<User> users = userMapper.selectList(new QueryWrapper<User>().in("login_name", createdPhones));
            if (!users.isEmpty()) {
                authTokenMapper.delete(new QueryWrapper<AuthToken>()
                        .in("user_id", users.stream().map(User::getId).toList()));
            }
            userMapper.delete(new QueryWrapper<User>().in("login_name", createdPhones));
            createdPhones.clear();
        }
    }

    @Test
    void register_duplicatePhone_returns1002() throws Exception {
        // TC-A03：同一手机号重复注册，返回 1002 与原型文案
        String phone = newPhone();
        register(phone).andExpect(status().isOk()).andExpect(jsonPath("$.code").value(0));

        register(phone)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1002))
                .andExpect(jsonPath("$.message").value("该手机号已注册，请直接登录"))
                .andExpect(jsonPath("$.data").value(org.hamcrest.Matchers.nullValue()));
    }

    @Test
    void login_wrongPassword_returns1005() throws Exception {
        // TC-A04：密码错误返回 1005；用户不存在同样 1005（不泄露账号是否存在）
        String phone = newPhone();
        register(phone);

        login(phone, "WrongPass1")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1005))
                .andExpect(jsonPath("$.message").value("手机号或密码错误"));
        login("13900009999", "WrongPass1")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1005));
    }

    @Test
    void password_storedAsBCrypt() throws Exception {
        // TC-H02：密码不明文存储，入库为 BCrypt 密文
        String phone = newPhone();
        register(phone);

        User user = userMapper.selectOne(new QueryWrapper<User>().eq("login_name", phone));
        assertTrue(user.getPasswordHash().startsWith("$2"), () -> "非 BCrypt 密文: " + user.getPasswordHash());
        assertNotEquals("Test1234", user.getPasswordHash());
    }

    @Test
    void protectedApi_withoutToken_401() throws Exception {
        // TC-A07/H01：未登录访问受保护接口，HTTP 401 + 统一响应体（code/message/data）
        mockMvc.perform(get("/api/orders/mine"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401))
                .andExpect(jsonPath("$.message").value("请先登录"));
    }

    @Test
    void adminApi_withGuestToken_403() throws Exception {
        // TC-A08：住客 token 访问 /api/admin/**，HTTP 403
        String phone = newPhone();
        register(phone);
        String token = loginAndGetToken(phone);

        mockMvc.perform(get("/api/admin/orders").header("X-Token", token))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(403))
                .andExpect(jsonPath("$.message").value("无权限访问"));
    }

    @Test
    void logout_thenTokenInvalid_401() throws Exception {
        // TC-A10：登出后原 token 立即失效
        String phone = newPhone();
        register(phone);
        String token = loginAndGetToken(phone);

        mockMvc.perform(post("/api/auth/logout").header("X-Token", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
        mockMvc.perform(get("/api/auth/me").header("X-Token", token))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401));
    }

    @Test
    void expiredToken_401() throws Exception {
        // TC-H04：过期 token 访问受保护接口返回 401
        String phone = newPhone();
        register(phone);
        String token = loginAndGetToken(phone);
        // 直接把 token 过期时间改到过去
        AuthToken authToken = authTokenMapper.selectById(token);
        authToken.setExpiresAt(LocalDateTime.now().minusHours(1));
        authTokenMapper.updateById(authToken);

        mockMvc.perform(get("/api/auth/me").header("X-Token", token))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401));
    }

    @Test
    void publicApi_unifiedResponseBody() throws Exception {
        // TC-H01：正常接口同样走统一响应体 Result{code, message, data}
        mockMvc.perform(get("/api/room-types"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.message").value("ok"))
                .andExpect(jsonPath("$.data").isArray());
    }

    // ---------- 测试辅助 ----------

    private String newPhone() {
        String phone = "137" + String.format("%08d", ThreadLocalRandom.current().nextInt(100_000_000));
        createdPhones.add(phone);
        return phone;
    }

    private ResultActions register(String phone) throws Exception {
        return mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"phone\":\"" + phone + "\",\"password\":\"Test1234\"}"));
    }

    private ResultActions login(String phone, String password) throws Exception {
        return mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"loginName\":\"" + phone + "\",\"password\":\"" + password + "\"}"));
    }

    private String loginAndGetToken(String phone) throws Exception {
        MvcResult result = login(phone, "Test1234").andReturn();
        JsonNode body = objectMapper.readTree(result.getResponse().getContentAsString());
        return body.path("data").path("token").asText();
    }
}
