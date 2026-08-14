package com.linzp.forum.data.db;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.linzp.forum.data.entity.ReplyEntity;
import com.linzp.forum.data.entity.TopicEntity;
import com.linzp.forum.data.entity.UserEntity;

/**
 * 全局唯一的数据库实例。
 * 版本号改动记得写 Migration，不然用户数据会丢。
 */
@Database(
        entities = {TopicEntity.class, ReplyEntity.class, UserEntity.class},
        version = 1,
        exportSchema = false
)
public abstract class ForumDatabase extends RoomDatabase {

    private static final String DB_NAME = "simple_forum.db";

    private static volatile ForumDatabase instance;

    public abstract TopicDao topicDao();

    public abstract ReplyDao replyDao();

    public abstract UserDao userDao();

    public static ForumDatabase getInstance(Context context) {
        if (instance == null) {
            synchronized (ForumDatabase.class) {
                if (instance == null) {
                    instance = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    ForumDatabase.class,
                                    DB_NAME)
                            // 现在还在开发阶段，表结构会变，先不写 migration，直接重建
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return instance;
    }
}
