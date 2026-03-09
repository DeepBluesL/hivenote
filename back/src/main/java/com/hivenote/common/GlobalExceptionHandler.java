package com.hivenote.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器
 * 只要代码里抛出了异常，都会走到这里，转成标准 JSON 返回给前端
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 捕获业务异常 (ServiceException)
     * 例如：throw new ServiceException("学号已存在");
     */
    @ExceptionHandler(ServiceException.class)
    public Result<Object> handleServiceException(ServiceException e) {
        log.error("业务异常: {}", e.getMsg());
        return Result.error(e.getCode(), e.getMsg());
    }

    /**
     * 捕获系统其它未知异常 (Exception)
     * 例如：空指针 NullPointerException
     */
    @ExceptionHandler(Exception.class)
    public Result<Object> handleException(Exception e) {
        log.error("系统异常", e);
        return Result.error(500, "系统内部错误，请联系管理员");
    }
}