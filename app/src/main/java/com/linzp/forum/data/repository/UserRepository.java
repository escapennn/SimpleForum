package com.linzp.forum.data.repository;

import android.content.Context;
import android.text.TextUtils;

import com.linzp.forum.data.db.ForumDatabase;
import com.linzp.forum.data.db.UserDao;
import com.linzp.forum.data.entity.UserEntity;
import com.linzp.forum.util.CommonUtils;

import java.util.List;

/**
 * 用户相关的数据入口：注册、登录、资料修改。
 */
public class UserRepository {

    private final UserDao userDao;

    private static volatile UserRepository instance;

    private UserRepository(Context context) {
        this.userDao = ForumDatabase.getInstance(context).userDao();
    }

    public static UserRepository getInstance(Context context) {
        if (instance == null) {
            synchronized (UserRepository.class) {
                if (instance == null) {
                    instance = new UserRepository(context);
                }
            }
        }
        return instance;
    }

    /**
     * 数据库被关掉之后，这里缓存着的老 Dao 就不能用了，
     * 清空数据的时候跟着一起释放。
     */
    public static synchronized void releaseInstance() {
        instance = null;
    }

    public enum RegisterResult {
        SUCCESS,
        USERNAME_EXISTS,
        EMAIL_EXISTS,
        INVALID_USERNAME,
        INVALID_EMAIL,
        INVALID_PASSWORD
    }

    public RegisterResult register(String username, String nickname, String email, String password) {
        if (!CommonUtils.isValidUsername(username)) {
            return RegisterResult.INVALID_USERNAME;
        }
        if (!CommonUtils.isValidEmail(email)) {
            return RegisterResult.INVALID_EMAIL;
        }
        if (!CommonUtils.isValidPassword(password)) {
            return RegisterResult.INVALID_PASSWORD;
        }
        if (userDao.queryByUsername(username.trim()) != null) {
            return RegisterResult.USERNAME_EXISTS;
        }
        if (userDao.queryByEmail(email.trim()) != null) {
            return RegisterResult.EMAIL_EXISTS;
        }

        UserEntity user = new UserEntity();
        user.setUsername(username.trim());
        user.setNickname(TextUtils.isEmpty(nickname) ? username.trim() : nickname.trim());
        user.setEmail(email.trim());
        user.setPasswordHash(CommonUtils.md5(password));
        user.setSignature("");
        user.setAvatarUrl("");
        user.setRegisterTime(System.currentTimeMillis());
        user.setTopicCount(0);
        user.setReplyCount(0);
        user.setExp(0);

        long id = userDao.insert(user);
        return id > 0 ? RegisterResult.SUCCESS : RegisterResult.INVALID_USERNAME;
    }

    /**
     * 登录。账号可以是用户名也可以是邮箱。
     * 返回 null 表示账号或密码错误。
     */
    public UserEntity login(String account, String password) {
        if (TextUtils.isEmpty(account) || TextUtils.isEmpty(password)) {
            return null;
        }
        UserEntity user = userDao.queryByAccount(account.trim());
        if (user == null) {
            return null;
        }
        String inputHash = CommonUtils.md5(password);
        if (inputHash.equals(user.getPasswordHash())) {
            return user;
        }
        return null;
    }

    public UserEntity getUserById(long userId) {
        return userDao.queryById(userId);
    }

    public boolean updateNickname(long userId, String nickname) {
        if (TextUtils.isEmpty(nickname)) {
            return false;
        }
        userDao.updateNickname(userId, nickname.trim());
        return true;
    }

    public boolean updateSignature(long userId, String signature) {
        userDao.updateSignature(userId, signature == null ? "" : signature.trim());
        return true;
    }

    public void onTopicPublished(long userId) {
        userDao.increaseTopicCount(userId);
    }

    public void onReplyPublished(long userId) {
        userDao.increaseReplyCount(userId);
    }

    /**
     * 首次进入时把演示账号灌进去，否则没人能登录。
     * 做了同步保护，Application 里的异步初始化和登录页的调用同时来也不会插两份。
     */
    public synchronized void ensureDemoUsers() {
        if (userDao.countAll() > 0) {
            return;
        }
        List<UserEntity> users = MockDataProvider.buildUsers();
        for (UserEntity user : users) {
            userDao.insert(user);
        }
    }

    public String getDemoAccount() {
        return "admin";
    }
}
