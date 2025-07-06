package com.java.zhangzhiyuan.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "favorite_records")
public class FavoriteRecord {
    @PrimaryKey
    @NonNull
    public String newsId;

    public String newsItemJson;

    public long favoriteTime;

    public FavoriteRecord() {}

    @Ignore
    public FavoriteRecord(@NonNull String newsId, String newsItemJson, long favoriteTime) {
        this.newsId = newsId;
        this.newsItemJson = newsItemJson;
        this.favoriteTime = favoriteTime;
    }
}