package com.hivenote.common;

import java.io.Serializable;

/**
 * 统一响应结构体
 */
public class Result<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 请求是否成功
     */
    private Boolean success;

    /**
     * 响应消息
     */
    private String message;

    /**
     * 状态码
     */
    private Integer code;

    /**
     * 数据载体
     */
    private T result;

    /**
     * 时间戳
     */
    private long timestamp = System.currentTimeMillis();

    // ==========================================
    //  Getter 和 Setter 方法
    // ==========================================

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public T getResult() {
        return result;
    }

    public void setResult(T result) {
        this.result = result;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    // ==========================================
    //  静态构造方法
    // ==========================================

    /**
     * 成功 - 无数据返回
     */
    public static <T> Result<T> success() {
        Result<T> r = new Result<>();
        r.setSuccess(true);
        r.setCode(200);
        r.setMessage("操作成功");
        return r;
    }

    /**
     * 成功 - 带数据返回
     */
    public static <T> Result<T> success(T data) {
        Result<T> r = new Result<>();
        r.setSuccess(true);
        r.setCode(200);
        r.setResult(data);
        r.setMessage("操作成功");
        return r;
    }

    /**
     * 成功 - 带数据和自定义消息
     */
    public static <T> Result<T> success(T data, String msg) {
        Result<T> r = new Result<>();
        r.setSuccess(true);
        r.setCode(200);
        r.setResult(data);
        r.setMessage(msg);
        return r;
    }

    /**
     * 成功 - 自定义消息
     */
    public static <T> Result<T> success(String msg) {
        Result<T> r = new Result<>();
        r.setSuccess(true);
        r.setCode(200);
        r.setMessage(msg);
        return r;
    }

    /**
     * 失败 - 默认 code 400
     */
    public static <T> Result<T> error(String msg) {
        Result<T> r = new Result<>();
        r.setSuccess(false);
        r.setCode(400);
        r.setMessage(msg);
        return r;
    }

    /**
     * 失败 - 自定义 code
     */
    public static <T> Result<T> error(Integer code, String msg) {
        Result<T> r = new Result<>();
        r.setSuccess(false);
        r.setCode(code);
        r.setMessage(msg);
        return r;
    }

} // 唯一的一个结束大括号，必须在文件最后
