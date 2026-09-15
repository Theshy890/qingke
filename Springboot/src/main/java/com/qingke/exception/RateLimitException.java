package com.qingke.exception;

/**
 * API 限流异常（HTTP 429）
 */
public class RateLimitException extends Exception {
    public RateLimitException(String message) {
        super(message);
    }
}
