package com.hivenote.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ServiceException.class)
    public Result<Object> handleServiceException(ServiceException e) {
        log.warn("Business exception: {}", e.getMsg());
        return Result.error(e.getCode(), e.getMsg());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public Result<Object> handleMessageNotReadable(HttpMessageNotReadableException e) {
        log.warn("Invalid request body: {}", e.getMessage());
        return Result.error(400, "请求参数格式错误");
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    public Result<Object> handleMissingRequestHeader(MissingRequestHeaderException e) {
        log.warn("Missing request header: {}", e.getHeaderName());
        if ("accessToken".equalsIgnoreCase(e.getHeaderName())) {
            return Result.error(401, "登录已过期，请重新登录");
        }
        return Result.error(400, "缺少必要请求头: " + e.getHeaderName());
    }

    @ExceptionHandler({
            MissingServletRequestParameterException.class,
            MethodArgumentTypeMismatchException.class,
            IllegalArgumentException.class
    })
    public Result<Object> handleBadRequest(Exception e) {
        log.warn("Invalid request parameter: {}", e.getMessage());
        return Result.error(400, "请求参数错误");
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public Result<Object> handleMethodNotSupported(HttpRequestMethodNotSupportedException e) {
        log.warn("Unsupported request method: {}", e.getMessage());
        return Result.error(405, "请求方法不支持");
    }

    @ExceptionHandler(DuplicateKeyException.class)
    public Result<Object> handleDuplicateKey(DuplicateKeyException e) {
        log.warn("Duplicate key", e);
        return Result.error(409, "数据已存在，请勿重复提交");
    }

    @ExceptionHandler(BadSqlGrammarException.class)
    public Result<Object> handleBadSqlGrammar(BadSqlGrammarException e) {
        log.error("SQL or database schema error", e);
        return Result.error(500, "数据库结构或SQL配置异常，请联系管理员");
    }

    @ExceptionHandler(DataAccessException.class)
    public Result<Object> handleDataAccess(DataAccessException e) {
        log.error("Database access error", e);
        return Result.error(500, "数据库访问异常，请联系管理员");
    }

    @ExceptionHandler(Exception.class)
    public Result<Object> handleException(Exception e) {
        log.error("Unexpected system error", e);
        return Result.error(500, "系统内部错误，请联系管理员");
    }
}
