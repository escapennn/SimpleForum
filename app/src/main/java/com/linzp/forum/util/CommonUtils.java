package com.linzp.forum.util;

import android.text.TextUtils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.regex.Pattern;

/**
 * 一些零散的校验和处理方法。
 * 没引三方库，够用就行。
 */
public final class CommonUtils {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    /** 用户名允许字母数字下划线，3-16 位 */
    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[A-Za-z0-9_]{3,16}$");

    private CommonUtils() {
    }

    public static boolean isValidEmail(String email) {
        if (TextUtils.isEmpty(email)) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    public static boolean isValidUsername(String username) {
        if (TextUtils.isEmpty(username)) {
            return false;
        }
        return USERNAME_PATTERN.matcher(username.trim()).matches();
    }

    public static boolean isValidPassword(String password) {
        // 暂时只卡长度，太复杂的规则注册体验不好
        return !TextUtils.isEmpty(password) && password.length() >= 6;
    }

    public static boolean isValidTitle(String title) {
        return !TextUtils.isEmpty(title) && title.trim().length() >= 5;
    }

    /**
     * 密码哈希，避免明文存储。
     * 如需更高安全性，可替换为 bcrypt / argon2 等带盐慢哈希算法。
     */
    public static String md5(String raw) {
        if (raw == null) {
            return "";
        }
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            byte[] bytes = digest.digest(raw.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                String hex = Integer.toHexString(b & 0xFF);
                if (hex.length() == 1) {
                    sb.append('0');
                }
                sb.append(hex);
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            // 正常机器都有 MD5，真出事就直接返回原文，不要崩
            return raw;
        }
    }

    /**
     * 超出长度截断加省略号
     */
    public static String ellipsis(String text, int maxLength) {
        if (text == null) {
            return "";
        }
        if (text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, maxLength) + "…";
    }

    /**
     * 昵称兜底：用户没填昵称就用用户名
     */
    public static String safeNickname(String nickname, String username) {
        if (!TextUtils.isEmpty(nickname)) {
            return nickname;
        }
        if (!TextUtils.isEmpty(username)) {
            return username;
        }
        return "匿名用户";
    }
}
