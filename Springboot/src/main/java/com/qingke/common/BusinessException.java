package com.qingke.common;

/**
 * 业务逻辑异常类
 */
public class BusinessException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private int code;

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }

    public BusinessException(String message) {
        this(400, message);
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }
}
