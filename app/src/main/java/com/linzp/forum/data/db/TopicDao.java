package com.linzp.forum.data.db;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.linzp.forum.data.entity.TopicEntity;

import java.util.List;

@Dao
public interface TopicDao {

    @Query("SELECT * FROM topic WHERE category_id = :categoryId " +
            "ORDER BY is_top DESC, create_time DESC")
    List<TopicEntity> queryByCategory(int categoryId);

    /** 首页聚合，全部板块一起按时间倒序，置顶排前面 */
    @Query("SELECT * FROM topic ORDER BY is_top DESC, create_time DESC")
    List<TopicEntity> queryAll();

    /** 首页分页用的两个查询，规则和上面保持一致，只是加了 LIMIT */
    @Query("SELECT * FROM topic ORDER BY is_top DESC, create_time DESC LIMIT :limit OFFSET :offset")
    List<TopicEntity> queryAllPaged(int limit, int offset);

    @Query("SELECT * FROM topic WHERE category_id = :categoryId " +
            "ORDER BY is_top DESC, create_time DESC LIMIT :limit OFFSET :offset")
    List<TopicEntity> queryByCategoryPaged(int categoryId, int limit, int offset);

    /** 下拉刷新用，只取比某个时间新的 */
    @Query("SELECT * FROM topic WHERE create_time > :since ORDER BY create_time DESC")
    List<TopicEntity> queryNewerThan(long since);

    @Query("SELECT * FROM topic WHERE id = :id LIMIT 1")
    TopicEntity queryById(long id);

    @Query("SELECT * FROM topic WHERE author_id = :userId ORDER BY create_time DESC")
    List<TopicEntity> queryByAuthor(long userId);

    /**
     * 标题或内容模糊搜索。
     * 用 LIKE 拼 % 是偷懒做法，数据量大了要换 FTS，先记个 TODO。
     */
    @Query("SELECT * FROM topic WHERE title LIKE '%' || :keyword || '%' " +
            "OR content LIKE '%' || :keyword || '%' ORDER BY create_time DESC")
    List<TopicEntity> search(String keyword);

    @Query("SELECT COUNT(*) FROM topic")
    int countAll();

    /** 消息页用：我发过的帖子一共收了多少赞 */
    @Query("SELECT IFNULL(SUM(like_count), 0) FROM topic WHERE author_id = :userId")
    int sumLikesOfMyTopics(long userId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(TopicEntity topic);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    List<Long> insertAll(List<TopicEntity> topics);

    @Update
    int update(TopicEntity topic);

    @Delete
    int delete(TopicEntity topic);

    @Query("DELETE FROM topic")
    void clearAll();

    @Query("UPDATE topic SET view_count = view_count + 1 WHERE id = :id")
    void increaseViewCount(long id);

    @Query("UPDATE topic SET reply_count = :count WHERE id = :id")
    void updateReplyCount(long id, int count);

    @Query("UPDATE topic SET like_count = like_count + :delta, liked = :liked WHERE id = :id")
    void updateLikeState(long id, int delta, int liked);
}
