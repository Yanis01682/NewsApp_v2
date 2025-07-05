package com.java.zhangzhiyuan.network;

import com.java.zhangzhiyuan.model.NewsResponse;
import com.java.zhangzhiyuan.model.glm.GlmRequest;
import com.java.zhangzhiyuan.model.glm.GlmResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Query;
import retrofit2.http.Url;

public interface ApiService {

    @GET("svc/news/queryNewsList")
    Call<NewsResponse> getNews(
            @Query("size") String size,
            @Query("startDate") String startDate,
            // --- 这里是修正的地方 ---
            @Query("endDate") String endDate,
            @Query("words") String words,
            @Query("categories") String categories,
            @Query("page") String page
    );

    @POST
    Call<GlmResponse> getChatCompletion(@Url String url, @Header("Authorization") String token, @Body GlmRequest request);

}