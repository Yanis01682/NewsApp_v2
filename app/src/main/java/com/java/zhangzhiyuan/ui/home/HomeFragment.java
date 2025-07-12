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
    //用来引用布局中那个用于显示新闻列表的 RecyclerView 组件
    //回收站
    private RecyclerView recyclerView;
    //负责将新闻数据“填充”到 RecyclerView 中
    private NewsAdapter newsAdapter;
    private List<NewsItem> newsList;
    //用于引用布局中那个可以实现“下拉刷新”功能的容器。
    private SwipeRefreshLayout swipeRefreshLayout;
    //用于引用布局中的那个“搜索框”视图。
    private SearchView searchView;
    //用于引用那个在搜索模式下才会出现的“返回主页”的箭头按钮
    private ImageButton btnBackToHome;
    //当上拉加载更多时，这个值会递增。
    private int currentPage = 1;
    //当它为 true 时，表示当前正在加载数据（比如正在发起网络请求），此时应该阻止用户重复触发加载操作（比如连续多次下拉刷新）。加载完成后，它会被设回 false。
    private boolean isLoading = false;
    private final int PAGE_SIZE = 20;

    private String currentWords = "";
    private String currentCategory = "";
    private String currentStartDate = "2006-01-01";
    private String currentEndDate = "";
    private final Handler uiHandler = new Handler(Looper.getMainLooper());
    //用于引用那个在列表为空时显示的“暂无内容”的文本视图。
    private TextView emptyViewText;
    private boolean isInitialDataLoaded = false;
    //用来记录当前 Fragment 是处于“普通浏览模式”还是“搜索结果模式”。
    private boolean isInSearchState = false;
    //用于备份主列表数据的缓存列表。在进入搜索模式前，会把 newsList 的内容存到这里。
    private final List<NewsItem> mainFeedCache = new ArrayList<>();
    //记录用户在退出搜索前，主列表滚动到了哪个位置的哪个像素点，以便能够完美地恢复
    private int lastScrollPosition = 0;
    private int lastScrollOffset = 0;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getParentFragmentManager().setFragmentResultListener(AdvancedSearchDialogFragment.REQUEST_KEY, this, (requestKey, bundle) -> {
            if (!isInSearchState) {
                //把当前主列表的状态备份下来。
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
    //@NonNull 注解表示 inflater 这个参数永远不会是 null
    //inflater（加载器）就是用来将XML“打印”成View对象的“打印机”
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        setupUI();
        //初始化用于显示“暂无内容”的TextView
        emptyViewText = root.findViewById(R.id.empty_view_text);
        //监听器。监听用户做了什么动作
        setupListeners();
        updateSearchUI();
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), new OnBackPressedCallback(true) {
            @Override
            //点击返回按钮时的逻辑
            public void handleOnBackPressed() {
                if (isInSearchState) {
                    //恢复主列表
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
    //退出搜索模式时
    private void restoreMainFeedState() {
        //如果Fragment视图已经被销毁，则不执行任何操作。
        if (binding == null || newsAdapter == null) return;

        currentWords = "";
        currentCategory = "";
        currentStartDate = "2006-01-01";
        currentEndDate = "";
        isInSearchState = false;

        newsList.clear();
        newsList.addAll(mainFeedCache);
        //重新绘制整个列表。
        newsAdapter.notifyDataSetChanged();
        //精确恢复滚动位置
        //使得索引号为lastScrollPosition的列表项显示在列表的顶部
        // 并且其顶边与列表内容区的顶边之间的像素偏移量正好是我们之前记录的lastScrollOffset。
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
        //下拉监听
        swipeRefreshLayout.setOnRefreshListener(this::startRefresh);
        //滚动监听
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            //用于检测用户是否滑到了列表底部，如果是，则调用loadMoreNews()
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dy <= 0) return;
                LinearLayoutManager lm = (LinearLayoutManager) recyclerView.getLayoutManager();
                if (lm != null && !isLoading && !newsList.isEmpty() && lm.findLastCompletelyVisibleItemPosition() == newsList.size() - 1) {
                    loadMoreNews();
                }
            }
        });
        //与搜索框交互时，弹出高级搜索框
        searchView.setOnClickListener(v -> showAdvancedSearchDialog());
        searchView.setOnQueryTextFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                showAdvancedSearchDialog();
                searchView.clearFocus();
            }
        });
        //为返回按钮设置点击监听。点击后，如果当前在搜索状态，就调用restoreMainFeedState()。
        btnBackToHome.setOnClickListener(v -> {
            if (isInSearchState) {
                restoreMainFeedState();
            }
        });
    }

    private void updateSearchUI() {
        if (binding == null) return;
        //如果在搜索模式下：显示那个返回箭头
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
    //处理网络请求
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
            //如果获取到的新闻列表不为空，则遍历这个新列表去重
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
    //处理网络请求失败
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
    //可视化ui
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
    //在onCreateView中被调用
    private void setupUI() {
        if (getContext() == null) return;
        recyclerView = binding.recyclerViewNews;
        swipeRefreshLayout = binding.swipeRefreshLayout;
        searchView = binding.searchView;
        btnBackToHome = binding.btnBackToHome;
        //LinearLayoutManager会使列表呈线性的、垂直滚动的样式。
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

    //显示高级搜索的对话框
    private void showAdvancedSearchDialog() {
        new AdvancedSearchDialogFragment().show(getParentFragmentManager(), "SEARCH_DIALOG");
    }
    //scrollToTopAndRefresh
    public void scrollToTopAndRefresh() {
        if (binding == null) return;
        binding.recyclerViewNews.scrollToPosition(0);
        new Handler(Looper.getMainLooper()).postDelayed(this::startRefresh, 100);
    }
    //上拉加载
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
        //延迟300毫秒
        uiHandler.postDelayed(() -> fetchNews(false), 300);
    }

    private void fetchNews(final boolean isRefresh) {
        //它接收一个布尔参数 isRefresh，用来区分这次调用是“下拉刷新”还是“上拉加载更多”。
        Executors.newSingleThreadExecutor().execute(() -> {
            if (getContext() == null) {
                uiHandler.post(() -> handleFailure(new IllegalStateException("Context is null"), isRefresh));
                return;
            }
            //拿到已读新闻的列表
            List<HistoryRecord> records = AppDatabase.getDatabase(requireContext()).historyDao().getAll();
            //将HistoryRecord列表转换成一个只含url的set
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
    //在onResume中被调用，用于更新已读新闻的状态。
    private void loadViewedNews() {
        if (getContext() == null) return;
        Executors.newSingleThreadExecutor().execute(() -> {
            //从Room数据库中获取所有的历史记录。
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
    //Fragment的视图被销毁时被调用
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}