package com.linzp.forum.data.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * 收藏记录。
 * 一个用户对一篇帖子只能收藏一次，user_id + topic_id 联合查重。
 * 帖子删了收藏跟着没了（CASCADE）。
 */
@Entity(
        tableName = "favorite",
        foreignKeys = @ForeignKey(
                entity = TopicEntity.class,
                parentColumns = "id",
                childColumns = "topic_id",
                onDelete = ForeignKey.CASCADE
        ),
        indices = {@Index("topic_id"), @Index("user_id")}
)
public class FavoriteEntity {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    private long id;

    @ColumnInfo(name = "topic_id")
    private long topicId;

    @ColumnInfo(name = "user_id")
    private long userId;

    @ColumnInfo(name = "create_time")
    private long createTime;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getTopicId() {
        return topicId;
    }

    public void setTopicId(long topicId) {
        this.topicId = topicId;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }
}
