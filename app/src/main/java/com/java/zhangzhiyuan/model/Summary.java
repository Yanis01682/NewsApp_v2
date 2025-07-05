package com.java.zhangzhiyuan.model;

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