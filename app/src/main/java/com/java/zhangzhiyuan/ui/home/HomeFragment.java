package com.java.zhangzhiyuan.ui.home;

import android.net.Uri; // 引入Uri.Builder
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.java.zhangzhiyuan.databinding.FragmentHomeBinding;
import com.java.zhangzhiyuan.model.NewsItem;
import com.java.zhangzhiyuan.model.NewsResponse;
import com.java.zhangzhiyuan.network.ApiService;
import com.java.zhangzhiyuan.network.RetrofitClient;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private RecyclerView recyclerView;
    private NewsAdapter newsAdapter;
    private List<NewsItem> newsList;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        if (getContext() != null) {
            recyclerView = binding.recyclerViewNews;
            recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            newsList = new ArrayList<>();
            newsAdapter = new NewsAdapter(getContext(), newsList);
            recyclerView.setAdapter(newsAdapter);
            loadNews();
        }
        return root;
    }

    private void loadNews() {
        // 1. 手动构建一个完整的、我们确定能工作的URL
        // 我们使用之前从你提供的GitHub项目中分析出的、最可能成功的参数组合
        String baseUrl = "https://api2.newsminer.net/svc/news/queryNewsList";
        Uri.Builder builder = Uri.parse(baseUrl).buildUpon();
        builder.appendQueryParameter("size", "15");
        builder.appendQueryParameter("page", "1");
        builder.appendQueryParameter("category", "科技"); // 使用单数的 "category"

        String finalUrl = builder.build().toString();

        Log.d("HomeFragment", "手动构建的最终URL: " + finalUrl);

        // 2. 获取ApiService实例
        ApiService apiService = RetrofitClient.getApiService();

        // 3. 调用我们新的接口方法，把完整的URL传进去
        Call<NewsResponse> call = apiService.getNewsByFullUrl(finalUrl);

        // 4. 执行异步请求
        call.enqueue(new Callback<NewsResponse>() {
            @Override
            public void onResponse(@NonNull Call<NewsResponse> call, @NonNull Response<NewsResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<NewsItem> fetchedNews = response.body().getData();
                    if (fetchedNews != null && !fetchedNews.isEmpty()) {
                        newsList.clear();
                        newsList.addAll(fetchedNews);
                        newsAdapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(getContext(), "服务器成功响应，但没有数据", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.e("HomeFragment", "请求失败，响应码: " + response.code());
                    Toast.makeText(getContext(), "服务器响应错误: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<NewsResponse> call, @NonNull Throwable t) {
                Log.e("HomeFragment", "网络请求失败", t);
                Toast.makeText(getContext(), "网络请求失败，请检查连接", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}