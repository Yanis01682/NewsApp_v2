package com.java.zhangzhiyuan.network;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import okhttp3.OkHttpClient; // 导入OkHttpClient

public class RetrofitClient {

    private static Retrofit retrofit = null;

    // 这个基础URL只用于获取新闻列表
    private static final String BASE_URL = "https://api2.newsminer.net/";

    public static ApiService getApiService() {
        if (retrofit == null) {
            // 创建一个通用的OkHttpClient
            OkHttpClient client = new OkHttpClient.Builder().build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL) // 设置一个基础URL
                    .client(client) // 使用我们创建的client
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit.create(ApiService.class);
    }
}