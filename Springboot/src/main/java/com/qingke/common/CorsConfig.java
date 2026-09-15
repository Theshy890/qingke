package com.qingke.common;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

/**
 * CORS跨域配置
 */
@Configuration
public class CorsConfig {
    
    @Bean
    public CorsFilter corsFilter() {
        // 创建CORS配置对象
        CorsConfiguration config = new CorsConfiguration();
        
        // 设置允许的源，* 表示允许所有源
        config.addAllowedOriginPattern("*");
        
        // 设置允许携带认证信息
        config.setAllowCredentials(true);
        
        // 设置允许的HTTP方法
        config.addAllowedMethod("*");
        
        // 设置允许的请求头
        config.addAllowedHeader("*");
        
        // 设置暴露的响应头，主要用于前端获取自定义头
        config.addExposedHeader("*");
        
        // 设置预检请求的有效期，单位为秒
        config.setMaxAge(3600L);
        
        // 创建URL匹配源
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        
        // 对所有路径应用CORS配置
        source.registerCorsConfiguration("/**", config);
        
        // 创建并返回CorsFilter
        return new CorsFilter(source);
    }
}