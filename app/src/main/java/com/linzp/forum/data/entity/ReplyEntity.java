package com.linzp.forum.data.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.io.Serializable;

/**
 * 帖子回复（评论）。
 * topicId 外键关联 TopicEntity，删帖时级联删评论。
 */
@Entity(
        tableName = "reply",
        foreignKeys = @ForeignKey(
                entity = TopicEntity.class,
                parentColumns = "id",
                childColumns = "topic_id",
                onDelete = ForeignKey.CASCADE
        ),
        indices = {@Index("topic_id")}
)
public class ReplyEntity implements Serializable {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    private long id;

    @ColumnInfo(name = "topic_id")
    private long topicId;

    @ColumnInfo(name = "content")
    private String content;

    @ColumnInfo(name = "author_id")
    private long authorId;

    @ColumnInfo(name = "author_name")
    private String authorName;

    @ColumnInfo(name = "author_avatar")
    private String authorAvatar;

    @ColumnInfo(name = "create_time")
    private long createTime;

    /** 楼层号，从 1 开始，发帖本身算 0 楼 */
    @ColumnInfo(name = "floor_no", defaultValue = "1")
    private int floorNo;

    /** 回复的目标用户，为空表示直接回复楼主 */
    @ColumnInfo(name = "reply_to_name")
    private String replyToName;

    @ColumnInfo(name = "like_count", defaultValue = "0")
    private int likeCount;

    /** 当前用户是否点过赞，0 否 1 是 */
    @ColumnInfo(name = "liked", defaultValue = "0")
    private int liked;

    public ReplyEntity() {
    }

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

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public long getAuthorId() {
        return authorId;
    }

    public void setAuthorId(long authorId) {
        this.authorId = authorId;
    }

    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public String getAuthorAvatar() {
        return authorAvatar;
    }

    public void setAuthorAvatar(String authorAvatar) {
        this.authorAvatar = authorAvatar;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public int getFloorNo() {
        return floorNo;
    }

    public void setFloorNo(int floorNo) {
        this.floorNo = floorNo;
    }

    public String getReplyToName() {
        return replyToName;
    }

    public void setReplyToName(String replyToName) {
        this.replyToName = replyToName;
    }

    public int getLikeCount() {
        return likeCount;
    }

    public void setLikeCount(int likeCount) {
        this.likeCount = likeCount;
    }

    public boolean isLiked() {
        return liked == 1;
    }

    public void setLiked(int liked) {
        this.liked = liked;
    }
}
