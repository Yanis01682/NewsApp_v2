package com.java.zhangzhiyuan.db;
//结构相同于FavoriteDao
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import com.java.zhangzhiyuan.model.NoteRecord;
import java.util.List;

@Dao
public interface NoteDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(NoteRecord record);

    @Query("SELECT * FROM note_records WHERE newsId = :newsId")
    NoteRecord getNoteByNewsId(String newsId);

    @Query("SELECT * FROM note_records ORDER BY updateTime DESC")
    List<NoteRecord> getAll();
}