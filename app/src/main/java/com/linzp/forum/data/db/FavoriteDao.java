package com.linzp.forum.data.db;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.linzp.forum.data.entity.FavoriteEntity;
import com.linzp.forum.data.entity.TopicEntity;

import java.util.List;

@Dao
public interface FavoriteDao {

    /** 某人是否收藏过某帖 */
    @Query("SELECT COUNT(*) FROM favorite WHERE user_id = :userId AND topic_id = :topicId")
    int countByUserAndTopic(long userId, long topicId);

    /** 我的收藏列表，按收藏时间倒序 */
    @Query("SELECT t.* FROM topic t INNER JOIN favorite f ON t.id = f.topic_id " +
            "WHERE f.user_id = :userId ORDER BY f.create_time DESC")
    List<TopicEntity> queryTopicsByUser(long userId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(FavoriteEntity favorite);

    @Query("DELETE FROM favorite WHERE user_id = :userId AND topic_id = :topicId")
    void deleteByUserAndTopic(long userId, long topicId);

    @Query("DELETE FROM favorite WHERE topic_id = :topicId")
    void deleteByTopic(long topicId);

    @Query("DELETE FROM favorite")
    void clearAll();
}
