package com.java.zhangzhiyuan.ui.category;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.java.zhangzhiyuan.R;
import com.java.zhangzhiyuan.adapter.NewsAdapter;
import com.java.zhangzhiyuan.db.AppDatabase;
import com.java.zhangzhiyuan.model.HistoryRecord;
import com.java.zhangzhiyuan.model.NewsItem;
import com.java.zhangzhiyuan.model.NewsResponse;
import com.java.zhangzhiyuan.network.ApiService;
import com.java.zhangzhiyuan.network.RetrofitClient;
import androidx.lifecycle.ViewModelProvider;
import android.view.View; // 确保引入
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CategoryNewsListFragment extends Fragment {

    private static final String ARG_CATEGORY = "category_value";
    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerView;
    private NewsAdapter newsAdapter;
    private List<NewsItem> newsList = new ArrayList<>();
    private String categoryValue;
    private CategoryViewModel categoryViewModel; // <--- 1. 在顶部声明

    private int currentPage = 1;
    private boolean isLoading = false;
    // 核心修正：修改页面大小为20
    private final int PAGE_SIZE = 20;
    private final Handler uiHandler = new Handler(Looper.getMainLooper());
    private TextView emptyViewText; // <--- 在类顶部声明
    private static final long MIN_REFRESH_DURATION = 500; // 最小刷新动画时长(毫秒)
    private long refreshStartTime = 0;



    public static CategoryNewsListFragment newInstance(String categoryValue) {
        CategoryNewsListFragment fragment = new CategoryNewsListFragment();
        Bundle args = new Bundle();
        args.putString(ARG_CATEGORY, categoryValue);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            categoryValue = getArguments().getString(ARG_CATEGORY);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_category_news_list, container, false);
        setupUI(view);
        emptyViewText = view.findViewById(R.id.empty_view_text); // <--- 初始化
        setupListeners();
        startRefresh();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadViewedNews();
    }

    private void setupUI(View view) {
        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh_layout);
        recyclerView = view.findViewById(R.id.recycler_view_news);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        // 核心修正：使用正确的构造函数
        newsAdapter = new NewsAdapter(getContext(), newsList);
        recyclerView.setAdapter(newsAdapter);
    }

    private void setupListeners() {
        swipeRefreshLayout.setOnRefreshListener(this::startRefresh);
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dy <= 0) return;
                LinearLayoutManager lm = (LinearLayoutManager) recyclerView.getLayoutManager();
                if (lm != null && !isLoading && !newsList.isEmpty() && lm.findLastCompletelyVisibleItemPosition() == newsList.size() - 1) {
                    loadMoreNews();
                }
            }
        });
    }
    // vvv--- 添加这个新方法 ---vvv
    public void scrollToTopAndRefresh() {
        // 确保视图组件都已存在
        if (recyclerView == null || swipeRefreshLayout == null) {
            return;
        }

        // vvv--- 使用正确的变量 categoryValue 来显示提示 ---vvv
        // 如果您点击分类Tab后看到了这个提示，说明指令已经成功传递到这里了
        //Toast.makeText(getContext(), categoryValue + " 刷新中...", Toast.LENGTH_SHORT).show();
        // ^^^--- 调试代码结束 ---^^^

        // 平滑地滚动到列表顶部
        recyclerView.scrollToPosition(0);

        // 延迟一小会再显示刷新动画，让滚动动画更自然
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (swipeRefreshLayout != null) {
                swipeRefreshLayout.setRefreshing(true);
                // 调用您已有的、真正开始网络请求的方法
                startRefresh();
            }
        }, 100);
    }
// ^^^--- 添加结束 ---^^^

    private void startRefresh() {
        if (isLoading) {
            if (swipeRefreshLayout != null) {
                swipeRefreshLayout.setRefreshing(false);
            }
            return;
        }
        isLoading = true;
        refreshStartTime = System.currentTimeMillis(); // 记录刷新开始时间
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setRefreshing(true);
        }
        currentPage = 1;
        fetchNews(true);
    }

    private void loadMoreNews() {
        if (isLoading) return;
        isLoading = true;
        currentPage++;
        uiHandler.post(() -> {
            if (newsAdapter != null) {
                newsList.add(null);
                newsAdapter.notifyItemInserted(newsList.size() - 1);
            }
        });
        uiHandler.postDelayed(() -> fetchNews(false), 300);
    }

    private void fetchNews(final boolean isRefresh) {
        ApiService apiService = RetrofitClient.getApiService();
        String size = String.valueOf(PAGE_SIZE);
        String page = String.valueOf(currentPage);
        // 核心修正：确保endDate总是当前时间
        String endDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
        String startDate = "2006-01-01";;

        Call<NewsResponse> call = apiService.getNews(size, startDate, endDate, "", categoryValue, page);
        call.enqueue(new Callback<NewsResponse>() {
            @Override
            public void onResponse(@NonNull Call<NewsResponse> call, @NonNull Response<NewsResponse> response) {
                uiHandler.post(() -> handleResponse(response, isRefresh));
            }
            @Override
            public void onFailure(@NonNull Call<NewsResponse> call, @NonNull Throwable t) {
                uiHandler.post(() -> handleFailure(t, isRefresh));
            }
        });
    }
    // vvv--- 添加这个新的UI更新方法 ---vvv
    private void updateUIVisibility() {
        if (recyclerView == null || emptyViewText == null) return;
        if (newsList.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            emptyViewText.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            emptyViewText.setVisibility(View.GONE);
        }
    }
    // ^^^--- 添加结束 ---^^^

    private void handleResponse(Response<NewsResponse> response, boolean isRefresh) {
        if (newsAdapter == null) return;

        if (!isRefresh && !newsList.isEmpty() && newsList.get(newsList.size() - 1) == null) {
            newsList.remove(newsList.size() - 1);
            newsAdapter.notifyItemRemoved(newsList.size());
        }

        if (response.isSuccessful() && response.body() != null) {
            List<NewsItem> fetchedNews = response.body().getData();
            if (isRefresh) {
                newsList.clear();
            }
            if (fetchedNews != null && !fetchedNews.isEmpty()) {
                // 这里是之前修改过的、基于URL的去重逻辑
                Set<String> existingUrls = newsList.stream()
                        .map(NewsItem::getUrl)
                        .filter(java.util.Objects::nonNull)
                        .collect(Collectors.toSet());
                for (NewsItem newItem : fetchedNews) {
                    if (newItem != null && newItem.getUrl() != null && !existingUrls.contains(newItem.getUrl())) {
                        newsList.add(newItem);
                        existingUrls.add(newItem.getUrl());
                    }
                }
            } else if (!isRefresh) {
                Toast.makeText(getContext(), "没有更多新闻了", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(getContext(), "服务器响应错误: " + response.code(), Toast.LENGTH_LONG).show();
        }

        loadViewedNews();

        long duration = System.currentTimeMillis() - refreshStartTime;
        if (duration < MIN_REFRESH_DURATION) {
            uiHandler.postDelayed(() -> {
                if (swipeRefreshLayout != null) { // 确保Fragment还存活
                    swipeRefreshLayout.setRefreshing(false);
                    isLoading = false;
                    updateUIVisibility();
                    newsAdapter.notifyDataSetChanged();
                }
            }, MIN_REFRESH_DURATION - duration);
        } else {
            swipeRefreshLayout.setRefreshing(false);
            isLoading = false;
            updateUIVisibility();
            newsAdapter.notifyDataSetChanged();
        }
    }

    private void handleFailure(Throwable t, boolean isRefresh) {
        if (newsAdapter == null) return;

        if (!isRefresh && !newsList.isEmpty() && newsList.get(newsList.size() - 1) == null) {
            newsList.remove(newsList.size() - 1);
            newsAdapter.notifyItemRemoved(newsList.size());
        }

        Toast.makeText(getContext(), "网络请求失败", Toast.LENGTH_LONG).show();

        long duration = System.currentTimeMillis() - refreshStartTime;
        if (duration < MIN_REFRESH_DURATION) {
            uiHandler.postDelayed(() -> {
                if (swipeRefreshLayout != null) {
                    swipeRefreshLayout.setRefreshing(false);
                    isLoading = false;
                    updateUIVisibility();
                }
            }, MIN_REFRESH_DURATION - duration);
        } else {
            swipeRefreshLayout.setRefreshing(false);
            isLoading = false;
            updateUIVisibility();
        }
    }

    private void loadViewedNews() {
        if (getContext() == null) return;
        Executors.newSingleThreadExecutor().execute(() -> {
            List<HistoryRecord> records = AppDatabase.getDatabase(requireContext()).historyDao().getAll();
            // 从历史记录的JSON中解析出URL作为已读ID
            Set<String> viewedIds = records.stream().map(r -> {
                NewsItem item = new com.google.gson.Gson().fromJson(r.newsItemJson, NewsItem.class);
                return item != null ? item.getUrl() : null;
            }).filter(java.util.Objects::nonNull).collect(Collectors.toSet());
            uiHandler.post(() -> {
                if (newsAdapter != null) {
                    newsAdapter.setViewedNewsIds(viewedIds);
                    newsAdapter.notifyDataSetChanged();
                }
            });
        });
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 初始化ViewModel，注意这里用 requireParentFragment() 来获取和父页面共享的同一个“告示板”
        categoryViewModel = new ViewModelProvider(requireParentFragment()).get(CategoryViewModel.class);

        // 订阅“告示板”
        categoryViewModel.getRefreshEvent().observe(getViewLifecycleOwner(), categoryNameToRefresh -> {
            // 当“告示板”上有新内容时，这里的代码就会被触发

            // 判断“告示”上的名字是不是自己的名字(categoryValue)
            if (categoryNameToRefresh != null && categoryNameToRefresh.equals(this.categoryValue)) {
                // 是自己的任务！执行刷新！
                scrollToTopAndRefresh();
            }
        });
    }
}