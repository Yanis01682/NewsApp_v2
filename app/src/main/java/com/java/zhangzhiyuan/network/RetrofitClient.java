package com.java.zhangzhiyuan.network;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
//创建url
public class RetrofitClient {

    private static Retrofit retrofit = null;

    // API服务器的基础URL
    private static final String BASE_URL = "https://api2.newsminer.net/";

    public static ApiService getApiService() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL) // 设置基础URL
                    .addConverterFactory(GsonConverterFactory.create()) // 添加Gson转换器
                    .build();
        }
        return retrofit.create(ApiService.class);
    }
}