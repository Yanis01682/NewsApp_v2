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

    // Retrofit会将它与一个“基础URL”拼接起来，形成完整的请求地址。
    @GET("svc/news/queryNewsList")
    //getNews(...): 这是我们定义的方法名。
    //Call<NewsResponse>: 这是方法的返回值类型
    Call<NewsResponse> getNews(
            @Query("size") String size,
            @Query("startDate") String startDate,
            @Query("endDate") String endDate,
            @Query("words") String words,
            @Query("categories") String categories,
            @Query("page") String page
    );
    //当我们调用 getNews("20", "2024-01-01", ..., "1") 时，Retrofit在后台会自动为我们构建出类似下面这样的完整URL并发起请求：
    //https://api2.newsminer.net/svc/news/queryNewsList?size=20&startDate=2024-01-01&page=1

    // 用于调用智谱AI
    @POST("https://open.bigmodel.cn/api/paas/v4/chat/completions")
    Call<ZhipuResponse> getChatSummary(
            @Header("Authorization") String token,
            @Body ZhipuRequest requestBody
    );
}