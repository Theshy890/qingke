package com.qingke.common;

import lombok.Data;

import java.io.Serializable;

/**
 * 统一响应结果类
 */
@Data
public class Result<T> implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 响应状态码：200成功，其他失败
     */
    private int code;
    
    /**
     * 响应消息
     */
    private String message;
    
    /**
     * 响应数据
     */
    private T data;
    
    /**
     * 构造方法私有化
     */
    private Result() {
    }
    
    /**
     * 成功响应，带数据
     */
    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMessage("success");
        result.setData(data);
        return result;
    }
    
    /**
     * 成功响应，无数据
     */
    public static <T> Result<T> success() {
        return success(null);
    }
    
    /**
     * 失败响应
     */
    public static <T> Result<T> error(int code, String message) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMessage(message);
        result.setData(null);
        return result;
    }
    
    /**
     * 默认失败响应
     */
    public static <T> Result<T> error(String message) {
        return error(500, message);
    }
}