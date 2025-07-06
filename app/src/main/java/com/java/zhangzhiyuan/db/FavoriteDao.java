package com.java.zhangzhiyuan.db;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import com.java.zhangzhiyuan.model.FavoriteRecord;
import java.util.List;

@Dao
public interface FavoriteDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(FavoriteRecord record);

    @Delete
    void delete(FavoriteRecord record);

    @Query("SELECT * FROM favorite_records ORDER BY favoriteTime DESC")
    List<FavoriteRecord> getAll();

    @Query("SELECT * FROM favorite_records WHERE newsId = :newsId")
    FavoriteRecord getFavoriteById(String newsId);
}