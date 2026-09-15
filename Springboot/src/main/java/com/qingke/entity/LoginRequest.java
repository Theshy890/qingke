package com.qingke.entity;

import lombok.Data;

/**
 * 登录请求实体
 */
@Data
public class LoginRequest {
    /**
     * 账号
     */
    private String zh;
    
    /**
     * 密码
     */
    private String password;
    
    /**
     * 角色（user/admin）
     */
    private String role;
}
