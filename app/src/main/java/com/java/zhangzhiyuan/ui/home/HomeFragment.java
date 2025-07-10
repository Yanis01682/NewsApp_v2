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
    private TextView emptyViewText;
    private boolean isInitialDataLoaded = false;

    private boolean isInSearchState = false;
    private final List<NewsItem> mainFeedCache = new ArrayList<>();
    private int lastScrollPosition = 0;
    private int lastScrollOffset = 0;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getParentFragmentManager().setFragmentResultListener(AdvancedSearchDialogFragment.REQUEST_KEY, this, (requestKey, bundle) -> {
            if (!isInSearchState) {
                saveMainFeedState();
            }
            currentWords = bundle.getString(AdvancedSearchDialogFragment.KEY_WORDS, "");
            currentCategory = bundle.getString(AdvancedSearchDialogFragment.KEY_CATEGORY, "");
            currentStartDate = bundle.getString(AdvancedSearchDialogFragment.KEY_START_DATE, "2018-01-01");
            if (currentStartDate == null || currentStartDate.isEmpty()) {
                currentStartDate = "2006-01-01";
            }
            currentEndDate = bundle.getString(AdvancedSearchDialogFragment.KEY_END_DATE, "");

            isInSearchState = true;
            updateSearchUI();
            startRefresh();
        });
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        setupUI();
        emptyViewText = root.findViewById(R.id.empty_view_text);
        setupListeners();
        updateSearchUI();
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (isInSearchState) {
                    restoreMainFeedState();
                } else {
                    setEnabled(false);
                    requireActivity().onBackPressed();
                }
            }
        });
        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!isInitialDataLoaded) {
            startRefresh();
            isInitialDataLoaded = true;
        } else if (!isInSearchState) {
            loadViewedNews();
        }
    }

    private void saveMainFeedState() {
        if (recyclerView != null && newsList != null) {
            mainFeedCache.clear();
            mainFeedCache.addAll(newsList);
            LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
            if (layoutManager != null) {
                lastScrollPosition = layoutManager.findFirstVisibleItemPosition();
                View firstVisibleItem = layoutManager.findViewByPosition(lastScrollPosition);
                lastScrollOffset = (firstVisibleItem != null) ? firstVisibleItem.getTop() - recyclerView.getPaddingTop() : 0;
            }
        }
    }

    private void restoreMainFeedState() {
        if (binding == null || newsAdapter == null) return;

        currentWords = "";
        currentCategory = "";
        currentStartDate = "2006-01-01";
        currentEndDate = "";
        isInSearchState = false;

        newsList.clear();
        newsList.addAll(mainFeedCache);
        newsAdapter.notifyDataSetChanged();

        if (recyclerView != null && lastScrollPosition >= 0 && lastScrollPosition < newsList.size()) {
            ((LinearLayoutManager) recyclerView.getLayoutManager()).scrollToPositionWithOffset(lastScrollPosition, lastScrollOffset);
        }

        updateSearchUI();
        updateUIVisibility();

        isLoading = false;
        if (binding.swipeRefreshLayout.isRefreshing()) {
            binding.swipeRefreshLayout.setRefreshing(false);
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
            if (isInSearchState) {
                restoreMainFeedState();
            }
        });
    }

    private void updateSearchUI() {
        if (binding == null) return;

        if (isInSearchState) {
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
            if (binding != null) binding.swipeRefreshLayout.setRefreshing(false);
            return;
        }
        isLoading = true;
        currentPage = 1;

        if (binding != null) {
            if (!binding.swipeRefreshLayout.isRefreshing()) {
                binding.swipeRefreshLayout.setRefreshing(true);
            }
            if (isInSearchState) {
                if (newsList != null) newsList.clear();
                if (newsAdapter != null) newsAdapter.notifyDataSetChanged();
                binding.emptyViewText.setText("搜索中...");
                binding.recyclerViewNews.setVisibility(View.GONE);
                binding.emptyViewText.setVisibility(View.VISIBLE);
                binding.recyclerViewNews.scrollToPosition(0);
            }
        }

        fetchNews(true);
    }

    private void handleResponse(Response<NewsResponse> response, boolean isRefresh) {
        if (binding == null) return;

        if (!isRefresh && !newsList.isEmpty() && newsList.get(newsList.size() - 1) == null) {
            newsList.remove(newsList.size() - 1);
            if (newsAdapter != null) newsAdapter.notifyItemRemoved(newsList.size());
        }

        if (response.isSuccessful() && response.body() != null) {
            List<NewsItem> fetchedNews = response.body().getData();
            if (isRefresh) {
                newsList.clear();
            }
            if (fetchedNews != null && !fetchedNews.isEmpty()) {
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

        binding.swipeRefreshLayout.setRefreshing(false);
        isLoading = false;
        updateUIVisibility();
        if(newsAdapter != null) {
            newsAdapter.notifyDataSetChanged();
        }
    }

    private void handleFailure(Throwable t, boolean isRefresh) {
        if (binding == null) return;

        if (!isRefresh && !newsList.isEmpty() && newsList.get(newsList.size() - 1) == null) {
            newsList.remove(newsList.size() - 1);
            if (newsAdapter != null) newsAdapter.notifyItemRemoved(newsList.size());
        }

        Toast.makeText(getContext(), "网络请求失败", Toast.LENGTH_LONG).show();

        binding.swipeRefreshLayout.setRefreshing(false);
        isLoading = false;
        updateUIVisibility();
    }
    private void updateUIVisibility() {
        if (binding == null) return;
        if (newsList.isEmpty()) {
            binding.recyclerViewNews.setVisibility(View.GONE);
            binding.emptyViewText.setText("暂无内容，请检查网络或下拉刷新");
            binding.emptyViewText.setVisibility(View.VISIBLE);
        } else {
            binding.recyclerViewNews.setVisibility(View.VISIBLE);
            binding.emptyViewText.setVisibility(View.GONE);
        }
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
    private void showAdvancedSearchDialog() {
        new AdvancedSearchDialogFragment().show(getParentFragmentManager(), "SEARCH_DIALOG");
    }

    public void scrollToTopAndRefresh() {
        if (binding == null) return;
        binding.recyclerViewNews.scrollToPosition(0);
        new Handler(Looper.getMainLooper()).postDelayed(this::startRefresh, 100);
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
        Executors.newSingleThreadExecutor().execute(() -> {
            if (getContext() == null) {
                uiHandler.post(() -> handleFailure(new IllegalStateException("Context is null"), isRefresh));
                return;
            }
            List<HistoryRecord> records = AppDatabase.getDatabase(requireContext()).historyDao().getAll();
            Set<String> viewedIds = records.stream().map(r -> {
                NewsItem item = new com.google.gson.Gson().fromJson(r.newsItemJson, NewsItem.class);
                return item != null ? item.getUrl() : null;
            }).filter(java.util.Objects::nonNull).collect(Collectors.toSet());

            uiHandler.post(() -> {
                if (newsAdapter != null) {
                    newsAdapter.setViewedNewsIds(viewedIds);
                }

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
            });
        });
    }

    private void loadViewedNews() {
        if (getContext() == null) return;
        Executors.newSingleThreadExecutor().execute(() -> {
            List<HistoryRecord> records = AppDatabase.getDatabase(requireContext()).historyDao().getAll();
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