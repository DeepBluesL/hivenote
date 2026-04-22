package com.hivenote.utils;

import cn.hutool.crypto.SecureUtil;

/**
 * 密码工具类
 * 使用 MD5 加密密码（生产环境建议使用 BCrypt）
 */
public class PasswordUtil {

    /**
     * 加密密码
     * @param password 明文密码
     * @return 加密后的密码
     */
    public static String encrypt(String password) {
        // 使用 Hutool 的 MD5 加密
        return SecureUtil.md5(password);
    }

    /**
     * 验证密码
     * @param inputPassword 用户输入的密码
     * @param storedPassword 数据库存储的加密密码
     * @return 是否匹配
     */
    public static boolean verify(String inputPassword, String storedPassword) {
        return encrypt(inputPassword).equals(storedPassword);
    }
}
