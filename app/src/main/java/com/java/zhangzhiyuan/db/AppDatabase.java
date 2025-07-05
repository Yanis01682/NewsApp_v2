package com.java.zhangzhiyuan.db;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import com.java.zhangzhiyuan.model.Summary;

@Database(entities = {Summary.class}, version = 1, exportSchema = false) // <-- 在这里添加
public abstract class AppDatabase extends RoomDatabase {
    public abstract SummaryDao summaryDao();

    private static volatile AppDatabase INSTANCE;

    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "news_database")
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}