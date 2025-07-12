package com.java.zhangzhiyuan.model;
//存储笔记
import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "note_records")
public class NoteRecord {
    @PrimaryKey
    @NonNull
    public String newsId;

    public String newsItemJson; // 复用新闻对象，方便展示标题

    public String noteContent; // 笔记内容

    public long updateTime; // 更新时间
}