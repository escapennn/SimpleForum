package com.linzp.forum.data.model;

/**
 * 帖子列表里的点赞/评论/浏览这种小数据用个简单封装，后面换网络请求也方便。
 */
public class TopicStats {

    private int viewCount;
    private int likeCount;
    private int replyCount;

    public TopicStats(int viewCount, int likeCount, int replyCount) {
        this.viewCount = viewCount;
        this.likeCount = likeCount;
        this.replyCount = replyCount;
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

    /**
     * 浏览量超过 1000 就显示成 1.2k，不然列表里数字太长
     */
    public static String formatCount(int count) {
        if (count < 1000) {
            return String.valueOf(count);
        }
        if (count < 10000) {
            return String.format("%.1fk", count / 1000f);
        }
        return (count / 10000) + "w";
    }
}
