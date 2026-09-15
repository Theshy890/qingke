package com.qingke.config;

import com.qingke.interceptor.AdminAuthInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web配置类：注册拦截器（CORS 统一由 CorsConfig 的 CorsFilter 处理）
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private AdminAuthInterceptor adminAuthInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 策略调整：只拦截明确的管理端接口，其他接口放行
        // 用户可以更新自己的信息，但只有管理员可以查看/管理所有用户
        registry.addInterceptor(adminAuthInterceptor)
                .addPathPatterns(
                        "/api/sys-user/page",              // 用户管理列表（仅管理员）
                        "/api/sys-user/add",               // 添加用户（仅管理员）
                        "/api/sys-user/delete/**",         // 删除用户（仅管理员）
                        "/api/sys-user/batch-delete",      // 批量删除（仅管理员）
                        "/api/sys-user/status/**",         // 修改状态（仅管理员）
                        "/api/plant-category/**",          // 植物分类管理
                        "/api/plant-knowledge/add",        // 添加植物知识
                        "/api/plant-knowledge/update",     // 更新植物知识
                        "/api/plant-knowledge/delete/**",  // 删除植物知识
                        "/api/community-post/audit/**",    // 帖子审核
                        "/api/community-post/delete/**",   // 删除帖子（管理员）
                        "/api/system-error/**",            // 系统错误管理
                        "/api/operation-log/**",           // 操作日志
                        "/api/sys-notice/update",          // 更新公告
                        "/api/sys-notice/delete/**"        // 删除公告
                );
    }
}
