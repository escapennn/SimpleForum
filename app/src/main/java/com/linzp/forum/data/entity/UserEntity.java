package com.linzp.forum.data.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;

/**
 * 用户表。
 * 注册用户信息本地持久化，密码经哈希处理后存储。
 */
@Entity(tableName = "user")
public class UserEntity implements Serializable {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    private long id;

    @ColumnInfo(name = "username")
    private String username;

    @ColumnInfo(name = "nickname")
    private String nickname;

    @ColumnInfo(name = "email")
    private String email;

    @ColumnInfo(name = "password_hash")
    private String passwordHash;

    @ColumnInfo(name = "avatar_url")
    private String avatarUrl;

    /** 个性签名 */
    @ColumnInfo(name = "signature")
    private String signature;

    @ColumnInfo(name = "register_time")
    private long registerTime;

    @ColumnInfo(name = "topic_count", defaultValue = "0")
    private int topicCount;

    @ColumnInfo(name = "reply_count", defaultValue = "0")
    private int replyCount;

    /** 经验值，用来算等级，先简单点 */
    @ColumnInfo(name = "exp", defaultValue = "0")
    private int exp;

    public UserEntity() {
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public long getRegisterTime() {
        return registerTime;
    }

    public void setRegisterTime(long registerTime) {
        this.registerTime = registerTime;
    }

    public int getTopicCount() {
        return topicCount;
    }

    public void setTopicCount(int topicCount) {
        this.topicCount = topicCount;
    }

    public int getReplyCount() {
        return replyCount;
    }

    public void setReplyCount(int replyCount) {
        this.replyCount = replyCount;
    }

    public int getExp() {
        return exp;
    }

    public void setExp(int exp) {
        this.exp = exp;
    }

    /**
     * 等级 = 经验 / 100 + 1，后面如果规则变了改这里就行
     */
    public int getLevel() {
        return exp / 100 + 1;
    }
}
