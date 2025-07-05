package com.java.zhangzhiyuan.ui.home;

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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private RecyclerView recyclerView;
    private NewsAdapter newsAdapter;
    // 这是最终要显示在界面上的列表
    private List<NewsItem> newsList;
    private static final String TAG = "HomeFragment";

    // --- 新增的成员变量，用于控制循环加载 ---
    private int currentPage = 1;
    private boolean isLoading = false;
    private final int TARGET_NEWS_COUNT = 15; // 我们的目标是加载15条新闻

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

            // 开始加载新闻
            startLoading();
        }
        return root;
    }

    /**
     * 启动加载流程的入口方法
     */
    private void startLoading() {
        // 重置状态
        newsList.clear();
        currentPage = 1;
        isLoading = false;
        newsAdapter.notifyDataSetChanged();
        // 开始循环获取新闻
        fetchNewsUntilFull();
    }

    /**
     * 核心方法：循环获取新闻，直到列表满15条为止
     */
    private void fetchNewsUntilFull() {
        // 如果正在加载，或者已经凑够了15条，就停止
        if (isLoading || newsList.size() >= TARGET_NEWS_COUNT) {
            return;
        }

        isLoading = true; // 标记为“正在加载”
        Log.d(TAG, "正在请求第 " + currentPage + " 页数据... (当前已有 " + newsList.size() + " 条新闻)");

        ApiService apiService = RetrofitClient.getApiService();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        String currentDateTime = sdf.format(new Date());

        String size = "20"; // 每次多请求一些(比如20)，可以减少请求次数，提高效率
        String startDate = "2024-06-20";
        String words = "";
        String categories = "";

        Call<NewsResponse> call = apiService.getNews(size, startDate, currentDateTime, words, categories, String.valueOf(currentPage));

        call.enqueue(new Callback<NewsResponse>() {
            @Override
            public void onResponse(@NonNull Call<NewsResponse> call, @NonNull Response<NewsResponse> response) {
                isLoading = false; // 加载结束

                if (response.isSuccessful() && response.body() != null) {
                    List<NewsItem> fetchedNews = response.body().getData();

                    // 如果服务器返回了数据
                    if (fetchedNews != null && !fetchedNews.isEmpty()) {

                        // 过滤并添加新闻，直到列表满15条
                        for (NewsItem item : fetchedNews) {
                            if (newsList.size() < TARGET_NEWS_COUNT && item.getImage() != null) {
                                newsList.add(item);
                            }
                        }

                        // 立刻刷新界面，让用户能看到加载进度
                        newsAdapter.notifyDataSetChanged();

                        // 如果还没凑够15条，就去请求下一页
                        if (newsList.size() < TARGET_NEWS_COUNT) {
                            currentPage++; // 页码+1
                            fetchNewsUntilFull(); // 递归调用，继续获取
                        } else {
                            Log.d(TAG, "加载完成！已成功获取15条带图片的新闻。");
                        }

                    } else {
                        // 如果服务器没有更多数据返回了，就停止加载
                        Log.d(TAG, "服务器在第 " + currentPage + " 页没有返回更多数据了。");
                        Toast.makeText(getContext(), "没有更多新闻了", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.e(TAG, "服务器响应错误: " + response.code());
                    Toast.makeText(getContext(), "服务器响应错误: " + response.code(), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<NewsResponse> call, @NonNull Throwable t) {
                isLoading = false; // 加载结束
                Log.e(TAG, "网络请求失败", t);
                Toast.makeText(getContext(), "网络请求失败，请检查网络连接", Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}