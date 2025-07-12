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
                    //它告诉Retrofit：“请使用 Gson 作为你的JSON解析工具。当你拿到服务器返回的JSON字符串后，
                    // 请用 Gson 自动把它转换成Java对象（比如 NewsResponse）”。
                    .addConverterFactory(GsonConverterFactory.create())
                    // 完成所有配置，创建出Retrofit实例。
                    .build();
        }
        return retrofit.create(ApiService.class);
    }
}