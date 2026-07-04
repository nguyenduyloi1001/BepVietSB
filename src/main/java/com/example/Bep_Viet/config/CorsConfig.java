package com.example.Bep_Viet.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig {

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**") // Áp dụng cho TẤT CẢ các API trong hệ thống
                        .allowedOrigins("http://localhost:3000") // 🔥 Cho phép cổng React gọi sang
                        .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS") // Cho phép tất cả các phương thức
                        .allowedHeaders("*") // Cho phép truyền mọi loại Header (nơi chứa chuỗi Bearer Token)
                        .allowCredentials(true); // Cho phép gửi kèm cookie/auth nếu sau này cần
            }
        };
    }
}