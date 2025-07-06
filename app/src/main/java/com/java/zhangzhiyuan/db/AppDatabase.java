package com.java.zhangzhiyuan.db;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import com.java.zhangzhiyuan.model.FavoriteRecord;
import com.java.zhangzhiyuan.model.HistoryRecord;
import com.java.zhangzhiyuan.model.Summary;

// 数据库版本回退到 5，因为我们简化了结构
@Database(entities = {Summary.class, HistoryRecord.class, FavoriteRecord.class}, version = 5, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    public abstract SummaryDao summaryDao();
    public abstract HistoryDao historyDao();
    public abstract FavoriteDao favoriteDao();

    private static volatile AppDatabase INSTANCE;

    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "news_database")
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}