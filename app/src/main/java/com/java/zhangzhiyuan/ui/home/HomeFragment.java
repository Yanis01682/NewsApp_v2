package com.java.zhangzhiyuan.ui.home;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.activity.OnBackPressedCallback;

import com.java.zhangzhiyuan.R;
import com.java.zhangzhiyuan.adapter.NewsAdapter;
import com.java.zhangzhiyuan.databinding.FragmentHomeBinding;
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

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private RecyclerView recyclerView;
    private NewsAdapter newsAdapter;
    private List<NewsItem> newsList;
    private SwipeRefreshLayout swipeRefreshLayout;
    private SearchView searchView;
    private ImageButton btnBackToHome;

    private int currentPage = 1;
    private boolean isLoading = false;
    private final int PAGE_SIZE = 20;

    private String currentWords = "";
    private String currentCategory = "";
    private String currentStartDate = "2006-01-01";
    private String currentEndDate = "";
    private final Handler uiHandler = new Handler(Looper.getMainLooper());
    private TextView emptyViewText; // <--- 在类顶部声明
    private static final long MIN_REFRESH_DURATION = 500; // 最小刷新时长，单位毫秒
    private long refreshStartTime = 0;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getParentFragmentManager().setFragmentResultListener(AdvancedSearchDialogFragment.REQUEST_KEY, this, (requestKey, bundle) -> {
            currentWords = bundle.getString(AdvancedSearchDialogFragment.KEY_WORDS, "");
            currentCategory = bundle.getString(AdvancedSearchDialogFragment.KEY_CATEGORY, "");
            currentStartDate = bundle.getString(AdvancedSearchDialogFragment.KEY_START_DATE, "2018-01-01");
            if (currentStartDate == null || currentStartDate.isEmpty()) {
                currentStartDate = "2006-01-01";
            }
            currentEndDate = bundle.getString(AdvancedSearchDialogFragment.KEY_END_DATE, "");

            updateSearchUI();
            startRefresh();
        });
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        setupUI();
        emptyViewText = root.findViewById(R.id.empty_view_text); // <--- 初始化
        setupListeners();
        updateSearchUI();
        startRefresh();
        // vvv--- 在这里添加新的返回键处理逻辑 ---vvv
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // 检查当前是否处于搜索结果状态
                boolean isSearching = !currentWords.isEmpty() || !currentCategory.isEmpty() || !currentEndDate.isEmpty();

                if (isSearching) {
                    // 如果是，就重置状态，返回到普通首页
                    currentWords = "";
                    currentCategory = "";
                    currentStartDate = "2006-01-01"; // 恢复默认起始日期
                    currentEndDate = "";
                    updateSearchUI();
                    startRefresh();
                } else {
                    // 如果不是搜索状态，就执行默认的返回操作（通常是退出App）
                    // 我们通过禁用此回调并再次调用返回键来实现
                    setEnabled(false);
                    requireActivity().onBackPressed();
                }
            }
        });
        // ^^^--- 添加结束 ---^^^
        return root;
    }

//    @Override
//    public void onResume() {
//        super.onResume();
//        loadViewedNews();
//    }
    @Override
    public void onResume() {
        super.onResume();
        // 核心修改：在 onResume 时检查新闻列表是否为空。
        // 如果为空，说明数据可能在后台被回收，或者首次加载失败，此时需要重新加载。
        if (newsList == null || newsList.isEmpty()) {
            startRefresh();
        } else {
            // 如果列表有数据，只更新已读状态，避免不必要的网络请求。
            loadViewedNews();
        }
    }
    // vvv--- 添加这个新的UI更新方法 ---vvv
    private void updateUIVisibility() {
        if (binding == null) return;
        if (newsList.isEmpty()) {
            binding.recyclerViewNews.setVisibility(View.GONE);
            emptyViewText.setVisibility(View.VISIBLE);
        } else {
            binding.recyclerViewNews.setVisibility(View.VISIBLE);
            emptyViewText.setVisibility(View.GONE);
        }
    }
    // ^^^--- 添加结束 ---^^^

    private void setupUI() {
        if (getContext() == null) return;
        recyclerView = binding.recyclerViewNews;
        swipeRefreshLayout = binding.swipeRefreshLayout;
        searchView = binding.searchView;
        btnBackToHome = binding.btnBackToHome;
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        newsList = new ArrayList<>();
        newsAdapter = new NewsAdapter(getContext(), newsList);
        recyclerView.setAdapter(newsAdapter);

        // Bug 2: 彻底移除SearchView的下划线
        try {
            int searchPlateId = searchView.getContext().getResources().getIdentifier("android:id/search_plate", null, null);
            View searchPlate = searchView.findViewById(searchPlateId);
            if (searchPlate != null) {
                searchPlate.setBackgroundColor(Color.TRANSPARENT);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
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

        searchView.setOnClickListener(v -> showAdvancedSearchDialog());
        searchView.setOnQueryTextFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                showAdvancedSearchDialog();
                searchView.clearFocus();
            }
        });

        btnBackToHome.setOnClickListener(v -> {
            currentWords = "";
            currentCategory = "";
            currentStartDate = "2006-01-01";
            currentEndDate = "";
            updateSearchUI();
            startRefresh();
        });
    }

    private void showAdvancedSearchDialog() {
        new AdvancedSearchDialogFragment().show(getParentFragmentManager(), "SEARCH_DIALOG");
    }

    private void updateSearchUI() {
        if (binding == null) return;

        boolean isSearching = !currentWords.isEmpty() || !currentCategory.isEmpty() || !currentEndDate.isEmpty();

        if (isSearching) {
            binding.btnBackToHome.setVisibility(View.VISIBLE);
            String hint = currentWords;
            if (hint.isEmpty()) {
                hint = currentCategory;
            }
            if (hint.isEmpty()) {
                hint = "高级搜索结果";
            }
            binding.searchView.setQueryHint(hint);
        } else {
            binding.btnBackToHome.setVisibility(View.GONE);
            binding.searchView.setQueryHint("搜索新闻");
        }
    }
    // vvv--- 添加这个新方法 ---vvv
    public void scrollToTopAndRefresh() {
        if (binding == null) return; // 确保Fragment视图存在
        binding.recyclerViewNews.scrollToPosition(0);
        // 延迟一小段时间再执行刷新，可以给滚动动画留出时间，体验更好
        new Handler(Looper.getMainLooper()).postDelayed(this::startRefresh, 100);
    }
// ^^^--- 添加结束 ---^^^

    private void startRefresh() {
        if (isLoading) {
            if (binding != null && binding.swipeRefreshLayout != null) {
                binding.swipeRefreshLayout.setRefreshing(false);
            }
            return;
        }

        // vvv--- 在这里补上重置页码的关键代码 ---vvv
        currentPage = 1;
        // ^^^--- 添加结束 ---^^^

        isLoading = true;
        refreshStartTime = System.currentTimeMillis();
        if (binding != null && binding.swipeRefreshLayout != null) {
            binding.swipeRefreshLayout.setRefreshing(true);
        }

        // 请求网络数据
        fetchNews(true);
    }

    private void loadMoreNews() {
        if (isLoading) return;
        isLoading = true;
        currentPage++;
        uiHandler.post(() -> {
            if (binding != null && newsAdapter != null) {
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
        String endDate = currentEndDate.isEmpty()
                ? new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date())
                : currentEndDate;

        Call<NewsResponse> call = apiService.getNews(size, currentStartDate, endDate, currentWords, currentCategory, page);


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
        if (binding == null) return;

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
            }
        } else {
            Toast.makeText(getContext(), "服务器响应错误: " + response.code(), Toast.LENGTH_LONG).show();
        }

        long duration = System.currentTimeMillis() - refreshStartTime;
        if (duration < MIN_REFRESH_DURATION) {
            uiHandler.postDelayed(() -> {
                if (binding != null) {
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
        if (binding == null) return;

        if (!isRefresh && !newsList.isEmpty() && newsList.get(newsList.size() - 1) == null) {
            newsList.remove(newsList.size() - 1);
            newsAdapter.notifyItemRemoved(newsList.size());
        }

        Toast.makeText(getContext(), "网络请求失败", Toast.LENGTH_LONG).show();

        long duration = System.currentTimeMillis() - refreshStartTime;
        if (duration < MIN_REFRESH_DURATION) {
            uiHandler.postDelayed(() -> {
                if (binding != null) {
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
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
