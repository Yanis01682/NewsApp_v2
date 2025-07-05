package com.java.zhangzhiyuan.ui.home;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

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
//构建home的布局
    private FragmentHomeBinding binding;
    private RecyclerView recyclerView;
    private NewsAdapter newsAdapter;
    //新闻列表
    private List<NewsItem> newsList;
    //下拉和上拉
    private SwipeRefreshLayout swipeRefreshLayout;

    private static final String TAG = "HomeFragment";

    private int currentPage = 1;
    private boolean isLoading = false;
    private final int PAGE_SIZE = 15;
    private String sessionEndDate;
    private final Handler uiHandler = new Handler(Looper.getMainLooper());

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        setupUI();
        setupListeners();
        // 首次进入自动刷新
        uiHandler.post(this::startRefresh);

        return root;
    }

    private void setupUI() {
        if (getContext() == null) return;
        recyclerView = binding.recyclerViewNews;
        swipeRefreshLayout = binding.swipeRefreshLayout;
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);
        newsList = new ArrayList<>();
        newsAdapter = new NewsAdapter(getContext(), newsList);
        recyclerView.setAdapter(newsAdapter);
    }
    //上拉
    private void setupListeners() {
        swipeRefreshLayout.setOnRefreshListener(this::startRefresh);
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                // 必须是向上滚动时才触发
                if (dy <= 0) return;

                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                if (layoutManager == null || newsList.isEmpty()) return;

                int totalItemCount = layoutManager.getItemCount();
                int lastVisibleItemPosition = layoutManager.findLastCompletelyVisibleItemPosition();

                if (!isLoading && totalItemCount > 0 && lastVisibleItemPosition == totalItemCount - 1) {
                    loadMoreNews();
                }
            }
        });
    }
//下拉
    private void startRefresh() {
        if (isLoading) {
            swipeRefreshLayout.setRefreshing(false);
            return;
        }
        Log.d(TAG, "==> 开始下拉刷新...");
        isLoading = true;
        sessionEndDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
        swipeRefreshLayout.setRefreshing(true);
        currentPage = 1;
        fetchNews(true);
    }

    private void loadMoreNews() {
        if (isLoading) return;
        Log.d(TAG, "==> 开始上拉加载更多...");
        isLoading = true;
        currentPage++;

        // 【关键修复】
        // 步骤1：在UI线程中，立即向列表末尾添加 null 来代表加载项
        uiHandler.post(() -> {
            newsList.add(null);
            newsAdapter.notifyItemInserted(newsList.size() - 1);
        });

        // 步骤2：延迟一小段时间（给UI足够的时间去绘制圈圈），然后再去请求网络
        uiHandler.postDelayed(() -> fetchNews(false), 300); // 延迟300毫秒
    }
//重新获取新闻
    private void fetchNews(final boolean isRefresh) {
        ApiService apiService = RetrofitClient.getApiService();
        String size = String.valueOf(PAGE_SIZE);
        String startDate = "2020-06-20";

        Log.d(TAG, "    请求网络: page=" + currentPage + ", isRefresh=" + isRefresh);

        //这一步是默认搜索！！！
        Call<NewsResponse> call = apiService.getNews(size, startDate, sessionEndDate, "", "", String.valueOf(currentPage));

        call.enqueue(new Callback<NewsResponse>() {
            @Override
            public void onResponse(@NonNull Call<NewsResponse> call, @NonNull Response<NewsResponse> response) {
                uiHandler.post(() -> handleResponse(response, isRefresh));
            }

            @Override
            public void onFailure(@NonNull Call<NewsResponse> call, @NonNull Throwable t) {
                Log.e(TAG, "    网络请求失败", t);
                uiHandler.post(() -> handleFailure(t, isRefresh));
            }
        });
    }

    private void handleResponse(Response<NewsResponse> response, boolean isRefresh) {
        Log.d(TAG, "    处理网络响应: isRefresh=" + isRefresh);
        // 首先，移除加载圈圈（如果是加载更多操作）
        if (!isRefresh && !newsList.isEmpty() && newsList.get(newsList.size() - 1) == null) {
            int lastPosition = newsList.size() - 1; // 先记下被移除项的位置
            newsList.remove(lastPosition);
            newsAdapter.notifyItemRemoved(lastPosition); // 【核心修正】通知Adapter移除正确位置的项
        }

        if (response.isSuccessful() && response.body() != null) {
            List<NewsItem> fetchedNews = response.body().getData();
            if (fetchedNews != null && !fetchedNews.isEmpty()) {
                if (isRefresh) {
                    newsList.clear();
                    newsList.addAll(fetchedNews);
                    // 刷新时，必须用这个，安全可靠
                    newsAdapter.notifyDataSetChanged();
                    Log.d(TAG, "    刷新成功，列表大小: " + newsList.size());
                } else {
                    int insertPosition = newsList.size();
                    newsList.addAll(fetchedNews);
                    // 加载更多时，用这个，效率高
                    newsAdapter.notifyItemRangeInserted(insertPosition, fetchedNews.size());
                    Log.d(TAG, "    加载更多成功，列表大小: " + newsList.size());
                }
            } else if (!isRefresh) {
                Toast.makeText(getContext(), "没有更多新闻了", Toast.LENGTH_SHORT).show();
            }
        } else {
            Log.e(TAG, "    服务器响应错误: " + response.code());
            Toast.makeText(getContext(), "服务器响应错误: " + response.code(), Toast.LENGTH_LONG).show();
        }

        // 最后，重置所有状态
        swipeRefreshLayout.setRefreshing(false);
        isLoading = false;
        Log.d(TAG, "==> 所有操作完成, isLoading = false");
    }

    private void handleFailure(Throwable t, boolean isRefresh) {
        // 同样，失败时也要移除加载圈圈
        if (!isRefresh && !newsList.isEmpty() && newsList.get(newsList.size() - 1) == null) {
            newsList.remove(newsList.size() - 1);
            newsAdapter.notifyItemRemoved(newsList.size());
        }
        swipeRefreshLayout.setRefreshing(false);
        isLoading = false;
        Log.d(TAG, "==> 操作失败, isLoading = false");
        Toast.makeText(getContext(), "网络请求失败", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        uiHandler.removeCallbacksAndMessages(null);
        binding = null;
    }
}