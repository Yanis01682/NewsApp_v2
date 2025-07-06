package com.java.zhangzhiyuan.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "history_records")
public class HistoryRecord {
    @PrimaryKey
    @NonNull
    public String newsId;

    public String newsItemJson;

    public long viewTime;

    public HistoryRecord() {}

    @Ignore
    public HistoryRecord(@NonNull String newsId, String newsItemJson, long viewTime) {
        this.newsId = newsId;
        this.newsItemJson = newsItemJson;
        this.viewTime = viewTime;
    }
}