package com.java.zhangzhiyuan.db;
//结构相同于FavoriteDao
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import com.java.zhangzhiyuan.model.Summary;

@Dao
public interface SummaryDao {
    //存储以newsid作为唯一识别符
    @Query("SELECT * FROM summaries WHERE newsId = :newsId")
    Summary getSummaryById(String newsId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Summary summary);
}