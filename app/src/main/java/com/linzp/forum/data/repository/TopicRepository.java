package com.linzp.forum.data.repository;

import android.content.Context;
import android.text.TextUtils;

import com.linzp.forum.data.db.ForumDatabase;
import com.linzp.forum.data.db.ReplyDao;
import com.linzp.forum.data.db.TopicDao;
import com.linzp.forum.data.entity.ReplyEntity;
import com.linzp.forum.data.entity.TopicEntity;
import com.linzp.forum.util.CommonUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 帖子相关的数据入口。
 * Activity 只跟这里打交道，不直接碰 Dao，后面换网络实现改动面小一点。
 */
public class TopicRepository {

    private final TopicDao topicDao;
    private final ReplyDao replyDao;

    private static volatile TopicRepository instance;

    private TopicRepository(Context context) {
        ForumDatabase db = ForumDatabase.getInstance(context);
        this.topicDao = db.topicDao();
        this.replyDao = db.replyDao();
    }

    public static TopicRepository getInstance(Context context) {
        if (instance == null) {
            synchronized (TopicRepository.class) {
                if (instance == null) {
                    instance = new TopicRepository(context);
                }
            }
        }
        return instance;
    }

    /**
     * 首页列表。数据库是空的话先灌 mock 数据，这样第一次装完就有东西看。
     */
    public List<TopicEntity> loadHomeTopics(int categoryId) {
        ensureDataReady();
        if (categoryId <= 0) {
            return topicDao.queryAll();
        }
        return topicDao.queryByCategory(categoryId);
    }

    /** 下拉刷新：先不管网络，直接把库里的最新数据读出来 */
    public List<TopicEntity> refreshHomeTopics(int categoryId) {
        if (categoryId <= 0) {
            return topicDao.queryAll();
        }
        return topicDao.queryByCategory(categoryId);
    }

    /**
     * 打开详情页时调这个，会把浏览量 +1。
     */
    public TopicEntity getTopicDetail(long topicId) {
        TopicEntity topic = topicDao.queryById(topicId);
        if (topic != null) {
            topicDao.increaseViewCount(topicId);
            // 本地也 +1，免得返回列表看到数字没变，用户以为没生效
            topic.setViewCount(topic.getViewCount() + 1);
        }
        return topic;
    }

    /**
     * 只读地拿一条帖子，不产生任何副作用。
     * 之前有的地方（比如点赞回调、我的回复列表查标题）误用了 getTopicDetail，
     * 结果点个赞浏览量也跟着涨，这里单独拆出来。
     */
    public TopicEntity getTopicById(long topicId) {
        return topicDao.queryById(topicId);
    }

    public String getTopicTitle(long topicId) {
        TopicEntity topic = topicDao.queryById(topicId);
        return topic == null ? null : topic.getTitle();
    }

    public List<ReplyEntity> getReplies(long topicId) {
        return replyDao.queryByTopic(topicId);
    }

    public List<TopicEntity> searchTopics(String keyword) {
        if (TextUtils.isEmpty(keyword)) {
            return new ArrayList<>();
        }
        return topicDao.search(keyword.trim());
    }

    public List<TopicEntity> getTopicsByAuthor(long authorId) {
        return topicDao.queryByAuthor(authorId);
    }

    public List<ReplyEntity> getRepliesByAuthor(long authorId) {
        return replyDao.queryByAuthor(authorId);
    }

    /**
     * 发新帖。返回新帖 id，失败返回 -1。
     */
    public long publishTopic(TopicEntity topic) {
        if (topic == null || TextUtils.isEmpty(topic.getTitle())) {
            return -1;
        }
        long now = System.currentTimeMillis();
        topic.setCreateTime(now);
        topic.setUpdateTime(now);
        topic.setViewCount(0);
        topic.setLikeCount(0);
        topic.setReplyCount(0);
        topic.setLiked(0);
        topic.setIsTop(0);
        topic.setIsEssence(0);
        return topicDao.insert(topic);
    }

    /**
     * 发评论。楼层号取当前最大楼层 +1。
     * 没加事务，同一秒并发发两条理论上会撞楼层号，实际单人操作碰不到。
     */
    public long addReply(long topicId, ReplyEntity reply) {
        if (reply == null || TextUtils.isEmpty(reply.getContent())) {
            return -1;
        }
        int maxFloor = replyDao.queryMaxFloor(topicId);
        reply.setTopicId(topicId);
        reply.setFloorNo(maxFloor + 1);
        reply.setCreateTime(System.currentTimeMillis());
        if (reply.getLikeCount() <= 0) {
            reply.setLikeCount(0);
        }
        long id = replyDao.insert(reply);

        // 同步一下帖子的回复数
        int total = replyDao.countByTopic(topicId);
        topicDao.updateReplyCount(topicId, total);
        return id;
    }

    /**
     * 点赞 / 取消点赞。
     * 注意这里取反是拿数据库里的当前值判断的，不是拿界面上的，
     * 之前只信界面状态连续点会串，改过一次。
     */
    public boolean toggleLike(long topicId) {
        TopicEntity topic = topicDao.queryById(topicId);
        if (topic == null) {
            return false;
        }
        boolean nowLiked = !topic.isLiked();
        int delta = nowLiked ? 1 : -1;
        // 点赞数不会掉到负数，做一层保护
        if (!nowLiked && topic.getLikeCount() <= 0) {
            delta = 0;
        }
        topicDao.updateLikeState(topicId, delta, nowLiked ? 1 : 0);
        return nowLiked;
    }

    /**
     * 评论的点赞。逻辑和帖子点赞一样，取反以数据库为准。
     */
    public boolean toggleReplyLike(long replyId) {
        ReplyEntity reply = replyDao.queryById(replyId);
        if (reply == null) {
            return false;
        }
        boolean nowLiked = !reply.isLiked();
        int delta = nowLiked ? 1 : -1;
        if (!nowLiked && reply.getLikeCount() <= 0) {
            delta = 0;
        }
        replyDao.updateLikeState(replyId, delta, nowLiked ? 1 : 0);
        return nowLiked;
    }

    public boolean deleteTopic(long topicId) {
        TopicEntity topic = topicDao.queryById(topicId);
        if (topic == null) {
            return false;
        }
        replyDao.deleteByTopic(topicId);
        topicDao.delete(topic);
        return true;
    }

    public int getTotalTopicCount() {
        return topicDao.countAll();
    }

    /**
     * 没数据就初始化一次。
     * 判断条件用帖子数而不是用户数，因为帖子是主表。
     * 加了 synchronized：首页和板块页可能同时首次加载，不加锁会插两份。
     */
    private synchronized void ensureDataReady() {
        if (topicDao.countAll() > 0) {
            return;
        }
        List<TopicEntity> topics = MockDataProvider.buildTopics();
        topicDao.insertAll(topics);

        // insertAll 之后实体上才有自增 id，评论依赖 topicId，所以必须放在后面
        List<ReplyEntity> replies = MockDataProvider.buildReplies(topics);
        if (!replies.isEmpty()) {
            replyDao.insertAll(replies);
        }

        // 把帖子的回复数按实际评论数对齐
        for (TopicEntity topic : topics) {
            int count = replyDao.countByTopic(topic.getId());
            topicDao.updateReplyCount(topic.getId(), count);
        }
    }

    /** 设置页的"清空数据"用，方便调试 */
    public void clearAllData() {
        topicDao.clearAll();
    }
}
