package com.market.security;

import com.market.common.model.UserDTO;
import com.market.common.util.JsonUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    public JwtAuthFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        String path = request.getRequestURI();
        if (!path.startsWith("/api/") || isPublicPath(path)) {
            chain.doFilter(request, response);
            return;
        }

        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            sendError(response, HttpServletResponse.SC_UNAUTHORIZED, "未登录");
            return;
        }

        String token = header.substring(7);
        if (!jwtUtil.validateToken(token)) {
            sendError(response, HttpServletResponse.SC_UNAUTHORIZED, "Token 已过期");
            return;
        }

        Claims claims = jwtUtil.parseToken(token);
        UserDTO user = new UserDTO();
        user.setId(jwtUtil.getUserId(claims));
        user.setUsername(jwtUtil.getUsername(claims));
        user.setRole(jwtUtil.getRole(claims));
        request.setAttribute("currentUser", user);

        chain.doFilter(request, response);
    }

    private boolean isPublicPath(String path) {
        return path.equals("/api/auth/login")
                || path.equals("/api/auth/register")
                || path.equals("/api/auth/refresh")
                || path.equals("/api/health")
                || path.startsWith("/api/products")
                || path.startsWith("/api/categories")
                || path.startsWith("/api/seckill/products");
    }

    private void sendError(HttpServletResponse response, int code, String msg) throws IOException {
        response.setStatus(code);
        response.setContentType("application/json;charset=UTF-8");
        Map<String, Object> err = new HashMap<>();
        err.put("code", code);
        err.put("msg", msg);
        err.put("data", null);
        response.getWriter().write(JsonUtil.toJson(err));
    }
}
