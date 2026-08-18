package com.linzp.forum.data.prefs;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.linzp.forum.data.entity.UserEntity;

/**
 * 登录态保存。只存必要的几个字段，不放整个 UserEntity 序列化，省得改字段就崩。
 */
public class UserSession {

    private static final String PREF_NAME = "forum_user_prefs";
    private static final String KEY_USER_ID = "login_user_id";
    private static final String KEY_USERNAME = "login_username";
    private static final String KEY_NICKNAME = "login_nickname";
    private static final String KEY_AVATAR = "login_avatar";
    private static final String KEY_REMEMBER = "remember_password";
    private static final String KEY_SAVED_ACCOUNT = "saved_account";
    private static final String KEY_SAVED_PWD = "saved_password";
    private static final String KEY_LOGIN_TIME = "last_login_time";

    private final SharedPreferences sp;

    private static volatile UserSession instance;

    private UserSession(Context context) {
        sp = context.getApplicationContext()
                .getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public static UserSession getInstance(Context context) {
        if (instance == null) {
            synchronized (UserSession.class) {
                if (instance == null) {
                    instance = new UserSession(context);
                }
            }
        }
        return instance;
    }

    public void saveLogin(UserEntity user) {
        if (user == null) {
            return;
        }
        sp.edit()
                .putLong(KEY_USER_ID, user.getId())
                .putString(KEY_USERNAME, user.getUsername())
                .putString(KEY_NICKNAME, user.getNickname())
                .putString(KEY_AVATAR, user.getAvatarUrl())
                .putLong(KEY_LOGIN_TIME, System.currentTimeMillis())
                .apply();
    }

    public void clearLogin() {
        // 清登录态但保留"记住密码"，不然下次登录还要重新输
        sp.edit()
                .remove(KEY_USER_ID)
                .remove(KEY_USERNAME)
                .remove(KEY_NICKNAME)
                .remove(KEY_AVATAR)
                .apply();
    }

    public boolean isLoggedIn() {
        return getUserId() > 0;
    }

    public long getUserId() {
        return sp.getLong(KEY_USER_ID, -1L);
    }

    public String getUsername() {
        return sp.getString(KEY_USERNAME, "");
    }

    public String getNickname() {
        return sp.getString(KEY_NICKNAME, "");
    }

    public String getAvatar() {
        return sp.getString(KEY_AVATAR, "");
    }

    public void updateNickname(String nickname) {
        sp.edit().putString(KEY_NICKNAME, nickname).apply();
    }

    public void updateAvatar(String avatarUrl) {
        sp.edit().putString(KEY_AVATAR, avatarUrl).apply();
    }

    public long getLastLoginTime() {
        return sp.getLong(KEY_LOGIN_TIME, 0L);
    }

    // ------- 记住密码相关 -------

    public void saveRememberedAccount(String account, String password) {
        sp.edit()
                .putBoolean(KEY_REMEMBER, true)
                .putString(KEY_SAVED_ACCOUNT, account)
                .putString(KEY_SAVED_PWD, password)
                .apply();
    }

    public void clearRememberedAccount() {
        sp.edit()
                .putBoolean(KEY_REMEMBER, false)
                .remove(KEY_SAVED_ACCOUNT)
                .remove(KEY_SAVED_PWD)
                .apply();
    }

    public boolean isRememberEnabled() {
        return sp.getBoolean(KEY_REMEMBER, false);
    }

    public String getSavedAccount() {
        return sp.getString(KEY_SAVED_ACCOUNT, "");
    }

    public String getSavedPassword() {
        return sp.getString(KEY_SAVED_PWD, "");
    }

    /**
     * 判断当前登录用户是不是帖子作者
     */
    public boolean isCurrentUser(long authorId) {
        long uid = getUserId();
        return uid > 0 && uid == authorId;
    }

    public String displayName() {
        String nickname = getNickname();
        if (!TextUtils.isEmpty(nickname)) {
            return nickname;
        }
        return getUsername();
    }
}
