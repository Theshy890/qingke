package com.qingke.common;

import com.qingke.entity.SystemError;
import com.qingke.mapper.SystemErrorMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import jakarta.servlet.http.HttpServletRequest;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;

/**
 * 全局异常处理器 - 捕获异常并持久化到 system_error 表
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @Autowired
    private SystemErrorMapper systemErrorMapper;

    @ExceptionHandler(Exception.class)
    @ResponseBody
    public Result<String> handleException(Exception e, HttpServletRequest request) {
        logger.error("全局异常 - {} : {}", request.getRequestURI(), e.getMessage(), e);
        saveErrorToDb(e, request);
        return Result.error(500, "服务器内部错误: " + e.getMessage());
    }

    @ExceptionHandler(BusinessException.class)
    @ResponseBody
    public Result<String> handleBusinessException(BusinessException e, HttpServletRequest request) {
        logger.warn("业务异常 - {} : {}", request.getRequestURI(), e.getMessage());
        saveErrorToDb(e, request);
        return Result.error(e.getCode(), e.getMessage());
    }

    private void saveErrorToDb(Exception e, HttpServletRequest request) {
        try {
            SystemError error = new SystemError();
            error.setModule(extractModule(request.getRequestURI()));
            error.setErrorType(e.getClass().getSimpleName());
            error.setErrorMessage(truncate(e.getMessage(), 500));
            error.setStackTrace(truncate(getStackTrace(e), 2000));
            error.setRequestUrl(truncate(request.getRequestURI(), 500));
            error.setRequestMethod(request.getMethod());
            error.setCreateTime(new Date());
            systemErrorMapper.insert(error);
        } catch (Exception ex) {
            logger.error("异常入库失败: {}", ex.getMessage());
        }
    }

    private String extractModule(String uri) {
        if (uri == null) return "未知";
        String[] parts = uri.split("/");
        return parts.length >= 3 ? parts[2] : uri;
    }

    private String getStackTrace(Exception e) {
        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        return sw.toString();
    }

    private String truncate(String str, int maxLen) {
        if (str == null) return null;
        return str.length() > maxLen ? str.substring(0, maxLen) + "..." : str;
    }
}
