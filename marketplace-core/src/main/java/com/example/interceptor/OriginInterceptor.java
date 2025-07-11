package com.example.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Set;

@Component
public class OriginInterceptor implements HandlerInterceptor {
    private static final Set<String> ALLOWED_ORIGINS = Set.of("https://aceryue0.github.io");

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String origin = request.getHeader("Origin");

        if (origin != null && !ALLOWED_ORIGINS.contains(origin)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN); // 403
            return false;
        }
        return true;
    }
}
