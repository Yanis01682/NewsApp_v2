package com.java.zhangzhiyuan.db;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import com.java.zhangzhiyuan.model.Summary;

@Dao
public interface SummaryDao {
    @Query("SELECT * FROM summaries WHERE newsId = :newsId")
    Summary getSummaryById(String newsId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Summary summary);
}