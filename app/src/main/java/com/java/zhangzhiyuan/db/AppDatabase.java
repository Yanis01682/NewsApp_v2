package com.java.zhangzhiyuan.db;
//数据库
import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import com.java.zhangzhiyuan.model.FavoriteRecord;
import com.java.zhangzhiyuan.model.HistoryRecord;
import com.java.zhangzhiyuan.model.NoteRecord; // 导入
import com.java.zhangzhiyuan.model.Summary;

// 增加NoteRecord.class, 版本升级到 6
//每次你修改了数据库的结构（比如增加或删除了一张表、一个字段），你都必须增加这个版本号。
//Room会根据版本号的变化来执行数据迁移（migration），防止用户在更新App后丢失数据。
//在这个项目中，因为 fallbackToDestructiveMigration() 被调用，所以当版本更新时，旧的数据库会被直接销毁并重建。
@Database(entities = {Summary.class, HistoryRecord.class, FavoriteRecord.class, NoteRecord.class}, version = 6, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    public abstract SummaryDao summaryDao();
    public abstract HistoryDao historyDao();
    public abstract FavoriteDao favoriteDao();
    public abstract NoteDao noteDao(); // 新增

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