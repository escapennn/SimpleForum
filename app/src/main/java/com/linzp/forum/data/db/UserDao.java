package com.linzp.forum.data.db;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.linzp.forum.data.entity.UserEntity;

@Dao
public interface UserDao {

    /** 登录用，用户名和邮箱都能登 */
    @Query("SELECT * FROM user WHERE username = :account OR email = :account LIMIT 1")
    UserEntity queryByAccount(String account);

    @Query("SELECT * FROM user WHERE id = :userId LIMIT 1")
    UserEntity queryById(long userId);

    @Query("SELECT * FROM user WHERE username = :username LIMIT 1")
    UserEntity queryByUsername(String username);

    @Query("SELECT * FROM user WHERE email = :email LIMIT 1")
    UserEntity queryByEmail(String email);

    @Query("SELECT COUNT(*) FROM user")
    int countAll();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(UserEntity user);

    @Update
    int update(UserEntity user);

    @Query("UPDATE user SET topic_count = topic_count + 1, exp = exp + 5 WHERE id = :userId")
    void increaseTopicCount(long userId);

    @Query("UPDATE user SET reply_count = reply_count + 1, exp = exp + 2 WHERE id = :userId")
    void increaseReplyCount(long userId);

    @Query("UPDATE user SET nickname = :nickname WHERE id = :userId")
    void updateNickname(long userId, String nickname);

    @Query("UPDATE user SET signature = :signature WHERE id = :userId")
    void updateSignature(long userId, String signature);
}
