package com.example.interceptor;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class OriginInterceptor implements HandlerInterceptor {

    private static final String ALLOWED_ORIGIN = "https://aceryue0.github.io";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
        throws Exception {

        String origin = request.getHeader("Origin");

        if (origin == null || !origin.equals(ALLOWED_ORIGIN)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "403 Forbidden: Invalid Origin");
            return false;
        }

        return true;
    }
}
