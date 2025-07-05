package com.java.zhangzhiyuan.network;

// 导入你的数据模型
import com.java.zhangzhiyuan.model.NewsResponse;

// 导入 Retrofit 必须的库
//处理网络请求
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Retrofit 接口定义
 * 定义所有网络请求的方法
 */

//这是接口，是抽象方法的集合
public interface ApiService {

    /**
     * 根据指定的参数获取新闻列表
     * @param size 每页新闻篇数
     * @param startDate 开始时间
     * @param endDate 截止时间
     * @param words 关键词
     * @param categories 类型
     * @param page 页码
     * @return 一个 Retrofit 的 Call 对象，用于异步请求
     */
    @GET("svc/news/queryNewsList")
    Call<NewsResponse> getNews(
            @Query("size") String size,
            @Query("startDate") String startDate,
            @Query("endDate") String endDate,
            @Query("words") String words,
            @Query("categories") String categories,
            @Query("page") String page
    );

}