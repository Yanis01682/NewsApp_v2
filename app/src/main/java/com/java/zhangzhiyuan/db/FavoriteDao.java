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
    //声明
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(FavoriteRecord record);

    @Delete
    void delete(FavoriteRecord record);
    //查询操作
    //从 favorite_records 表中选择所有列。
    //按 favoriteTime（收藏时间）字段进行降序（DESC）排列，确保最新的收藏记录排在最前面。
    @Query("SELECT * FROM favorite_records ORDER BY favoriteTime DESC")
    List<FavoriteRecord> getAll();

    @Query("SELECT * FROM favorite_records WHERE newsId = :newsId")
    FavoriteRecord getFavoriteById(String newsId);
}