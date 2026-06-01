package com.market.api.controller;

import com.market.common.model.R;
import com.market.common.model.UserDTO;
import com.market.security.JwtUtil;
import com.market.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin
public class AuthController {

    @Autowired
    private UserService userService;
    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/login")
    public R<Map<String, Object>> login(@RequestBody Map<String, String> body) {
        Map<String, Object> result = userService.login(body.get("username"), body.get("password"));
        if (result == null || !result.containsKey("token")) {
            return R.error(401, "用户名或密码错误");
        }
        String token = (String) result.get("token");
        UserDTO user = userService.getByToken(token);
        String accessToken = jwtUtil.generateAccessToken(user.getId(), user.getUsername(),
                user.getRole() != null ? user.getRole() : 0);
        String refreshToken = jwtUtil.generateRefreshToken(user.getId());
        return R.ok(Map.of("accessToken", accessToken, "refreshToken", refreshToken,
                "username", user.getUsername(), "role", String.valueOf(user.getRole() != null ? user.getRole() : 0)));
    }

    @PostMapping("/register")
    public R<Map<String, Object>> register(@RequestBody Map<String, String> body) {
        UserDTO user = userService.register(body.get("username"), body.get("password"),
                body.getOrDefault("phone", ""));
        if (user == null) return R.error(400, "注册失败");
        return R.ok(Map.of("username", user.getUsername()));
    }

    @PostMapping("/refresh")
    public R<Map<String, String>> refresh(@RequestBody Map<String, String> body) {
        String refreshToken = body.get("refreshToken");
        if (!jwtUtil.validateToken(refreshToken)) return R.error(401, "Refresh Token 已过期");
        io.jsonwebtoken.Claims claims = jwtUtil.parseToken(refreshToken);
        Long userId = jwtUtil.getUserId(claims);
        UserDTO user = userService.getById(userId);
        return R.ok(Map.of("accessToken", jwtUtil.generateAccessToken(user.getId(), user.getUsername(), user.getRole()),
                "refreshToken", jwtUtil.generateRefreshToken(user.getId())));
    }
}
