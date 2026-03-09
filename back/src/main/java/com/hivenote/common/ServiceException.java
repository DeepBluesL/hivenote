package com.hivenote.common;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 自定义业务异常
 * 用途：在 Service 层直接抛出，触发事务回滚，并由 GlobalExceptionHandler 统一捕获
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ServiceException extends RuntimeException {

    private String msg;
    private Integer code = 500;

    public ServiceException(String msg) {
        super(msg);
        this.msg = msg;
    }

    public ServiceException(String msg, Throwable e) {
        super(msg, e);
        this.msg = msg;
    }
}