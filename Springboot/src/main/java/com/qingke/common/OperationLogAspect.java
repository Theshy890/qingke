package com.qingke.common;

import com.qingke.entity.OperationLog;
import com.qingke.entity.SysUser;
import com.qingke.service.OperationLogService;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.Date;

/**
 * 操作日志切面
 * 自动记录带有 @OperationLogAnnotation 注解的方法的操作日志
 */
@Aspect
@Component
public class OperationLogAspect {

    private static final Logger logger = LoggerFactory.getLogger(OperationLogAspect.class);

    @Autowired
    private OperationLogService operationLogService;
    
    /**
     * 定义切点：所有带有 @OperationLogAnnotation 注解的方法
     */
    @Pointcut("@annotation(com.qingke.common.OperationLogAnnotation)")
    public void logPointCut() {
    }
    
    /**
     * 方法执行成功后记录日志
     */
    @AfterReturning(pointcut = "logPointCut()", returning = "result")
    public void afterReturning(JoinPoint joinPoint, Object result) {
        try {
            // 获取注解信息
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            Method method = signature.getMethod();
            OperationLogAnnotation annotation = method.getAnnotation(OperationLogAnnotation.class);
            
            if (annotation == null) {
                return;
            }
            
            // 获取请求信息
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes == null) {
                return;
            }
            HttpServletRequest request = attributes.getRequest();
            
            // 获取当前用户信息（从请求头的 token 中解析）
            String username = "匿名用户";
            String role = "user";
            
            String token = request.getHeader("Authorization");
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
                try {
                    // 解析 token 获取用户信息
                    String zh = JwtUtil.getZhFromToken(token);
                    String userRole = JwtUtil.getRoleFromToken(token);
                    if (zh != null) {
                        username = zh;
                    }
                    if (userRole != null) {
                        role = userRole;
                    }
                } catch (Exception e) {
                    // Token 解析失败，使用默认值
                }
            }
            
            // 获取方法参数，构建详细描述
            String description = buildDescription(annotation, joinPoint);
            
            // 创建操作日志对象
            OperationLog log = new OperationLog();
            log.setUsername(username);
            log.setRole(role);
            log.setOperationType(annotation.type());
            log.setModule(annotation.module());
            log.setDescription(description);
            log.setIpAddress(getIpAddress(request));
            log.setOperationTime(new Date());
            log.setResult("success");
            
            // 异步保存日志（避免影响主业务性能）
            saveLogAsync(log);
            
        } catch (Exception e) {
            // 日志记录失败不影响主业务
            logger.error("操作日志记录失败：{}", e.getMessage());
        }
    }
    
    /**
     * 方法执行异常时记录日志
     */
    @AfterThrowing(pointcut = "logPointCut()", throwing = "e")
    public void afterThrowing(JoinPoint joinPoint, Exception e) {
        try {
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            Method method = signature.getMethod();
            OperationLogAnnotation annotation = method.getAnnotation(OperationLogAnnotation.class);
            
            if (annotation == null) {
                return;
            }
            
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes == null) {
                return;
            }
            HttpServletRequest request = attributes.getRequest();
            
            String username = "匿名用户";
            String role = "user";
            
            String token = request.getHeader("Authorization");
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
                try {
                    String zh = JwtUtil.getZhFromToken(token);
                    String userRole = JwtUtil.getRoleFromToken(token);
                    if (zh != null) username = zh;
                    if (userRole != null) role = userRole;
                } catch (Exception ex) {
                    // ignore
                }
            }
            
            String description = buildDescription(annotation, joinPoint);
            
            OperationLog log = new OperationLog();
            log.setUsername(username);
            log.setRole(role);
            log.setOperationType(annotation.type());
            log.setModule(annotation.module());
            log.setDescription(description);
            log.setIpAddress(getIpAddress(request));
            log.setOperationTime(new Date());
            log.setResult("fail");
            log.setErrorMessage(e.getMessage());
            
            saveLogAsync(log);
            
        } catch (Exception ex) {
            logger.error("异常日志记录失败：{}", ex.getMessage());
        }
    }
    
    /**
     * 构建详细描述
     */
    private String buildDescription(OperationLogAnnotation annotation, JoinPoint joinPoint) {
        String baseDescription = annotation.description();
        
        // 如果描述为空，根据操作类型生成默认描述
        if (baseDescription == null || baseDescription.trim().isEmpty()) {
            String module = annotation.module();
            String type = annotation.type();
            
            switch (type) {
                case "login":
                    baseDescription = "登录" + module;
                    break;
                case "logout":
                    baseDescription = "登出" + module;
                    break;
                case "add":
                    baseDescription = "添加了" + module;
                    break;
                case "update":
                    baseDescription = "修改了" + module;
                    break;
                case "delete":
                    baseDescription = "删除了" + module;
                    break;
                case "audit":
                    baseDescription = "审核了" + module;
                    break;
                case "query":
                    baseDescription = "查询了" + module;
                    break;
                default:
                    baseDescription = "操作了" + module;
            }
        }
        
        // 尝试从方法参数中提取更多信息
        Object[] args = joinPoint.getArgs();
        if (args != null && args.length > 0) {
            for (Object arg : args) {
                if (arg instanceof SysUser) {
                    SysUser user = (SysUser) arg;
                    if (user.getName() != null) {
                        baseDescription += "：" + user.getName();
                        break;
                    }
                } else if (arg instanceof String || arg instanceof Long || arg instanceof Integer) {
                    baseDescription += " (ID: " + arg + ")";
                    break;
                }
            }
        }
        
        return baseDescription;
    }
    
    /**
     * 获取客户端IP地址
     */
    private String getIpAddress(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 处理多个代理的情况
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
    
    /**
     * 异步保存日志
     */
    private void saveLogAsync(OperationLog log) {
        // 使用线程池异步保存，避免阻塞主业务
        new Thread(() -> {
            try {
                operationLogService.addLog(log);
            } catch (Exception e) {
                logger.error("异步保存日志失败：{}", e.getMessage());
            }
        }).start();
    }
}
