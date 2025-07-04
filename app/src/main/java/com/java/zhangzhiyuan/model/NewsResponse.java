package com.java.zhangzhiyuan.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class NewsResponse {

    @SerializedName("data")
    private List<NewsItem> data;
    private int pageSize;
    private int total;
    private int currentPage;

    // Getters
    public List<NewsItem> getData() { return data; }
    public int getPageSize() { return pageSize; }
    public int getTotal() { return total; }
    public int getCurrentPage() { return currentPage; }
}