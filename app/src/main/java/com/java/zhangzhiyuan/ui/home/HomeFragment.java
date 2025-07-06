package com.java.zhangzhiyuan.ui.home;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.SearchView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
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
    private String currentStartDate = "2018-01-01";
    private String currentEndDate = "";
    private final Handler uiHandler = new Handler(Looper.getMainLooper());

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getParentFragmentManager().setFragmentResultListener(AdvancedSearchDialogFragment.REQUEST_KEY, this, (requestKey, bundle) -> {
            currentWords = bundle.getString(AdvancedSearchDialogFragment.KEY_WORDS, "");
            currentCategory = bundle.getString(AdvancedSearchDialogFragment.KEY_CATEGORY, "");
            currentStartDate = bundle.getString(AdvancedSearchDialogFragment.KEY_START_DATE, "2018-01-01");
            if (currentStartDate == null || currentStartDate.isEmpty()) {
                currentStartDate = "2018-01-01";
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
        setupListeners();
        updateSearchUI();
        startRefresh();
        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadViewedNews();
    }

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
            currentStartDate = "2018-01-01";
            currentEndDate = "";
            updateSearchUI();
            startRefresh();
        });
    }

    // 核心修正：补全这个缺失的方法
    private void showAdvancedSearchDialog() {
        new AdvancedSearchDialogFragment().show(getParentFragmentManager(), "SEARCH_DIALOG");
    }

    private void updateSearchUI() {
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
                ? new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date())
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
        if (binding == null) return; // 防闪退
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
            } else if (isRefresh) {
                Toast.makeText(getContext(), "没有找到相关新闻", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "没有更多新闻了", Toast.LENGTH_SHORT).show();
            }
            loadViewedNews();
        } else {
            Toast.makeText(getContext(), "服务器响应错误: " + response.code(), Toast.LENGTH_LONG).show();
        }
        swipeRefreshLayout.setRefreshing(false);
        isLoading = false;
    }

    private void handleFailure(Throwable t, boolean isRefresh) {
        if (binding == null) return; // 防闪退
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}