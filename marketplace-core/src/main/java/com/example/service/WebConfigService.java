package com.example.service;

import com.example.interceptor.OriginInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.*;

@Configuration
public class WebConfigService implements WebMvcConfigurer {

    @Autowired
    private OriginInterceptor originInterceptor;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
            .allowedOriginPatterns("https://aceryue0.github.io")
            .allowedMethods("GET", "POST", "PUT", "DELETE")
            .allowCredentials(true);

        registry.addMapping("/api/marketplace/saveList")
            .allowedOriginPatterns("*")  // 允許所有來源
            .allowedMethods("POST")      // 僅允許 POST
            .allowCredentials(false);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(originInterceptor)
            .addPathPatterns("/**"); // 可自行排除某些 API
    }
}
