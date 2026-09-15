package com.qingke.entity;

import lombok.Data;

/**
 * 登录响应实体
 */
@Data
public class LoginResponse {
    /**
     * JWT Token
     */
    private String token;
    
    /**
     * 用户信息
     */
    private SysUser userInfo;
    
    public LoginResponse(String token, SysUser userInfo) {
        this.token = token;
        this.userInfo = userInfo;
    }
}
