package com.java.zhangzhiyuan.network;

import com.java.zhangzhiyuan.model.NewsResponse;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Url; // 确保导入了正确的 @Url 注解

public interface ApiService {

    /**
     * Kotlin中的 suspend fun getNewsByFullUrl(...) 翻译为Java的 Call<NewsResponse> getNewsByFullUrl(...)
     * @GET 注解为空，@Url 注解告诉Retrofit，这个方法的参数 "fullUrl" 就是请求的完整地址。
     */
    @GET
    Call<NewsResponse> getNewsByFullUrl(@Url String fullUrl);
}