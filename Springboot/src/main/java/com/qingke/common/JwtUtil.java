package com.qingke.common;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT工具类
 */
@Component
public class JwtUtil {
    
    // 生成密钥（从配置文件 jwt.secret 注入，见 application.yml）
    private static SecretKey KEY;
    
    // Token有效期（1天）
    private static final long EXPIRATION = 1 * 24 * 60 * 60 * 1000;
    
    @Value("${jwt.secret}")
    public void setSecret(String secret) {
        KEY = Keys.hmacShaKeyFor(secret.getBytes());
    }
    
    /**
     * 生成Token
     */
    public static String generateToken(Long userId, String zh, String role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("zh", zh);
        claims.put("role", role);
        
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(zh)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION))
                .signWith(KEY, SignatureAlgorithm.HS256)
                .compact();
    }
    
    /**
     * 解析Token
     */
    public static Claims parseToken(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(KEY)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * 验证Token是否有效
     */
    public static boolean validateToken(String token) {
        try {
            Claims claims = parseToken(token);
            return claims != null && claims.getExpiration().after(new Date());
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * 从Token中获取用户ID
     */
    public static Long getUserIdFromToken(String token) {
        Claims claims = parseToken(token);
        if (claims != null) {
            return claims.get("userId", Long.class);
        }
        return null;
    }
    
    /**
     * 从Token中获取账号
     */
    public static String getZhFromToken(String token) {
        Claims claims = parseToken(token);
        if (claims != null) {
            return claims.get("zh", String.class);
        }
        return null;
    }
    
    /**
     * 从token中获取用户名
     */
    public static String getUsernameFromToken(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(KEY)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            return claims.get("username", String.class);
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * 从token中获取角色
     */
    public static String getRoleFromToken(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(KEY)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            return claims.get("role", String.class);
        } catch (Exception e) {
            return null;
        }
    }
}
