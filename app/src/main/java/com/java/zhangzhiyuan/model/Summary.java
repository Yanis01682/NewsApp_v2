package com.java.zhangzhiyuan.model;
//存储新闻摘要
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.annotation.NonNull;

@Entity(tableName = "summaries")
public class Summary {
    @PrimaryKey
    @NonNull
    public String newsId;

    public String summaryText;
}