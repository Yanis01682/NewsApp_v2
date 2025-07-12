package com.java.zhangzhiyuan.db;
//结构同于FavoriteDao
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import com.java.zhangzhiyuan.model.HistoryRecord;
import java.util.List;

@Dao
public interface HistoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(HistoryRecord record);

    @Query("SELECT * FROM history_records ORDER BY viewTime DESC")
    List<HistoryRecord> getAll();

    @Query("SELECT * FROM history_records WHERE newsId = :newsId")
    HistoryRecord getHistoryById(String newsId);
}