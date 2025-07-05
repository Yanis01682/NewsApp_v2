package com.java.zhangzhiyuan.network;

import com.java.zhangzhiyuan.model.NewsResponse;
import com.java.zhangzhiyuan.model.ZhipuRequest;  // 导入我们刚创建的类
import com.java.zhangzhiyuan.model.ZhipuResponse; // 导入我们刚创建的类

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface ApiService {

    // 您的获取新闻列表的接口，保持不变
    @GET("svc/news/queryNewsList")
    Call<NewsResponse> getNews(
            @Query("size") String size,
            @Query("startDate") String startDate,
            @Query("endDate") String endDate,
            @Query("words") String words,
            @Query("categories") String categories,
            @Query("page") String page
    );

    // --- 新增下面的方法，用于调用智谱AI ---
    @POST("https://open.bigmodel.cn/api/paas/v4/chat/completions")
    Call<ZhipuResponse> getChatSummary(
            @Header("Authorization") String token,
            @Body ZhipuRequest requestBody
    );
}