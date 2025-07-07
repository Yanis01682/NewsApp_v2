package com.java.zhangzhiyuan.ui.category;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
    private int currentPage = 1;
    private boolean isLoading = false;
    // 核心修正：修改页面大小为20
    private final int PAGE_SIZE = 20;
    private final Handler uiHandler = new Handler(Looper.getMainLooper());


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

    private void startRefresh() {
        if (isLoading) {
            swipeRefreshLayout.setRefreshing(false);
            return;
        }
        isLoading = true;
        swipeRefreshLayout.setRefreshing(true);
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
                newsList.addAll(fetchedNews);
            } else if (!isRefresh) {
                Toast.makeText(getContext(), "没有更多新闻了", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(getContext(), "服务器响应错误: " + response.code(), Toast.LENGTH_LONG).show();
        }
        loadViewedNews();
        swipeRefreshLayout.setRefreshing(false);
        isLoading = false;
    }

    private void handleFailure(Throwable t, boolean isRefresh) {
        if (newsAdapter == null) return;
        if (!isRefresh && !newsList.isEmpty() && newsList.get(newsList.size() - 1) == null) {
            newsList.remove(newsList.size() - 1);
            newsAdapter.notifyItemRemoved(newsList.size());
        }
        swipeRefreshLayout.setRefreshing(false);
        isLoading = false;
        Toast.makeText(getContext(), "网络请求失败", Toast.LENGTH_LONG).show();
    }

    private void loadViewedNews() {
        if (getContext() == null) return;
        Executors.newSingleThreadExecutor().execute(() -> {
            List<HistoryRecord> records = AppDatabase.getDatabase(requireContext()).historyDao().getAll();
            Set<String> viewedIds = records.stream().map(r -> r.newsId).collect(Collectors.toSet());
            uiHandler.post(() -> {
                if (newsAdapter != null) {
                    newsAdapter.setViewedNewsIds(viewedIds);
                    newsAdapter.notifyDataSetChanged();
                }
            });
        });
    }
}