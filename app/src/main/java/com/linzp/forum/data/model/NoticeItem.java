package com.linzp.forum.data.model;

/**
 * 消息页一条通知。
 * 目前只有"谁回复了我的帖子"这一种，字段先按这个来，
 * 以后加点赞通知再补 type。
 */
public class NoticeItem {

    private long topicId;
    private String topicTitle;
    private String fromName;
    private String content;
    private long createTime;

    public long getTopicId() {
        return topicId;
    }

    public void setTopicId(long topicId) {
        this.topicId = topicId;
    }

    public String getTopicTitle() {
        return topicTitle;
    }

    public void setTopicTitle(String topicTitle) {
        this.topicTitle = topicTitle;
    }

    public String getFromName() {
        return fromName;
    }

    public void setFromName(String fromName) {
        this.fromName = fromName;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }
}
