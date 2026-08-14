package com.linzp.forum.data.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;

/**
 * 论坛帖子实体。
 * 本地先用 Room 缓存，后面接服务端接口时把 syncTime 用上做增量拉取。
 */
@Entity(tableName = "topic")
public class TopicEntity implements Serializable {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    private long id;

    @ColumnInfo(name = "title")
    private String title;

    @ColumnInfo(name = "content")
    private String content;

    /** 板块 id，暂时写死几个：1-综合 2-技术 3-生活 4-求助 */
    @ColumnInfo(name = "category_id")
    private int categoryId;

    @ColumnInfo(name = "author_id")
    private long authorId;

    @ColumnInfo(name = "author_name")
    private String authorName;

    @ColumnInfo(name = "author_avatar")
    private String authorAvatar;

    @ColumnInfo(name = "create_time")
    private long createTime;

    @ColumnInfo(name = "update_time")
    private long updateTime;

    @ColumnInfo(name = "view_count")
    private int viewCount;

    @ColumnInfo(name = "like_count")
    private int likeCount;

    @ColumnInfo(name = "reply_count")
    private int replyCount;

    /** 置顶 / 精华，列表里会加个标记 */
    @ColumnInfo(name = "is_top", defaultValue = "0")
    private int isTop;

    @ColumnInfo(name = "is_essence", defaultValue = "0")
    private int isEssence;

    /** 本地是否已点赞，服务端回来的是汇总值，这个是给当前用户看的 */
    @ColumnInfo(name = "liked", defaultValue = "0")
    private int liked;

    public TopicEntity() {
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
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

    public long getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(long updateTime) {
        this.updateTime = updateTime;
    }

    public int getViewCount() {
        return viewCount;
    }

    public void setViewCount(int viewCount) {
        this.viewCount = viewCount;
    }

    public int getLikeCount() {
        return likeCount;
    }

    public void setLikeCount(int likeCount) {
        this.likeCount = likeCount;
    }

    public int getReplyCount() {
        return replyCount;
    }

    public void setReplyCount(int replyCount) {
        this.replyCount = replyCount;
    }

    public int getIsTop() {
        return isTop;
    }

    public void setIsTop(int isTop) {
        this.isTop = isTop;
    }

    public int getIsEssence() {
        return isEssence;
    }

    public void setIsEssence(int isEssence) {
        this.isEssence = isEssence;
    }

    public int getLiked() {
        return liked;
    }

    public void setLiked(int liked) {
        this.liked = liked;
    }

    public boolean isTopTopic() {
        return isTop == 1;
    }

    public boolean isEssenceTopic() {
        return isEssence == 1;
    }

    public boolean isLiked() {
        return liked == 1;
    }
}
