package com.linzp.forum.data.db;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.linzp.forum.data.entity.ReplyEntity;

import java.util.List;

@Dao
public interface ReplyDao {

    @Query("SELECT * FROM reply WHERE topic_id = :topicId ORDER BY floor_no ASC")
    List<ReplyEntity> queryByTopic(long topicId);

    @Query("SELECT * FROM reply WHERE id = :id LIMIT 1")
    ReplyEntity queryById(long id);

    /** 分页取，详情页评论多的时候用 */
    @Query("SELECT * FROM reply WHERE topic_id = :topicId " +
            "ORDER BY floor_no ASC LIMIT :limit OFFSET :offset")
    List<ReplyEntity> queryByTopicPaged(long topicId, int limit, int offset);

    @Query("SELECT * FROM reply WHERE author_id = :userId ORDER BY create_time DESC")
    List<ReplyEntity> queryByAuthor(long userId);

    @Query("SELECT COUNT(*) FROM reply WHERE topic_id = :topicId")
    int countByTopic(long topicId);

    /** 取最大楼层，新评论楼层 +1 */
    @Query("SELECT IFNULL(MAX(floor_no), 0) FROM reply WHERE topic_id = :topicId")
    int queryMaxFloor(long topicId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(ReplyEntity reply);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    List<Long> insertAll(List<ReplyEntity> replies);

    @Update
    int update(ReplyEntity reply);

    @Delete
    int delete(ReplyEntity reply);

    @Query("DELETE FROM reply WHERE id = :replyId")
    void deleteById(long replyId);

    @Query("DELETE FROM reply WHERE topic_id = :topicId")
    void deleteByTopic(long topicId);

    @Query("UPDATE reply SET like_count = like_count + :delta, liked = :liked WHERE id = :id")
    void updateLikeState(long id, int delta, int liked);

    @Query("DELETE FROM reply")
    void clearAll();
}
