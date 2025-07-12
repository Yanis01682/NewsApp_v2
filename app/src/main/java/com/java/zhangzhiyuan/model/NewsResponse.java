package com.java.zhangzhiyuan.model;
//这个文件是使用 Retrofit 和 Gson 进行网络请求的关键
//结构与新闻接口的返回结构一致
import com.google.gson.annotations.SerializedName;
import java.util.List;
@SuppressWarnings("unused")

public class NewsResponse {
//新闻列表
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
//新闻接口返回的json结构长这样
//{
//        "pageSize": 20,
//        "total": 5821,
//        "currentPage": 1,
//        "data": [
//        {
//        "newsID": "20240101...",
//        "title": "...",
//        "publisher": "...",
//        ...
//        },
//        { ... }
//        ]
//}