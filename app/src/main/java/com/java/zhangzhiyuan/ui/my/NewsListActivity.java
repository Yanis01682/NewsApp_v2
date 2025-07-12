package com.java.zhangzhiyuan.ui.my;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.java.zhangzhiyuan.R;
import com.java.zhangzhiyuan.adapter.NewsAdapter;
import com.java.zhangzhiyuan.adapter.NoteAdapter; // 导入
import com.java.zhangzhiyuan.db.AppDatabase;
import com.java.zhangzhiyuan.model.FavoriteRecord;
import com.java.zhangzhiyuan.model.HistoryRecord;
import com.java.zhangzhiyuan.model.NewsItem;
import com.java.zhangzhiyuan.model.NoteRecord; // 导入

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class NewsListActivity extends AppCompatActivity {
    public static final String EXTRA_TYPE = "list_type";
    public static final String TYPE_HISTORY = "history";
    public static final String TYPE_FAVORITES = "favorites";
    public static final String TYPE_NOTES = "notes";

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private RecyclerView recyclerView;
    private final Gson gson = new Gson();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news_list);

        Toolbar toolbar = findViewById(R.id.toolbar_news_list);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        recyclerView = findViewById(R.id.rv_news_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    protected void onResume() {
        super.onResume();
        String type = getIntent().getStringExtra(EXTRA_TYPE);
        if (type == null) {
            finish();
            return;
        }

        switch (type) {
            case TYPE_HISTORY:
                if (getSupportActionBar() != null) getSupportActionBar().setTitle("历史记录");
                loadHistory();
                break;
            case TYPE_FAVORITES:
                if (getSupportActionBar() != null) getSupportActionBar().setTitle("我的收藏");
                loadFavorites();
                break;
            case TYPE_NOTES: // 新增
                if (getSupportActionBar() != null) getSupportActionBar().setTitle("我的笔记");
                loadNotes();
                break;
        }
    }

    private void loadHistory() {
        //所有数据库操作都在子线程中执行。
        executorService.execute(() -> {
            List<HistoryRecord> records = AppDatabase.getDatabase(this).historyDao().getAll();
            Set<String> viewedIds = records.stream().map(r -> r.newsId).collect(Collectors.toSet());
            List<NewsItem> items = records.stream()
                    .map(r -> gson.fromJson(r.newsItemJson, NewsItem.class))
                    .collect(Collectors.toList());

            runOnUiThread(() -> {
                NewsAdapter adapter = new NewsAdapter(this, items);
                adapter.setViewedNewsIds(viewedIds);
                recyclerView.setAdapter(adapter);
            });
        });
    }

    private void loadFavorites() {
        executorService.execute(() -> {
            List<HistoryRecord> allHistory = AppDatabase.getDatabase(this).historyDao().getAll();
            Set<String> viewedIds = allHistory.stream().map(r -> r.newsId).collect(Collectors.toSet());
            List<FavoriteRecord> records = AppDatabase.getDatabase(this).favoriteDao().getAll();
            List<NewsItem> items = records.stream()
                    .map(r -> gson.fromJson(r.newsItemJson, NewsItem.class))
                    .collect(Collectors.toList());

            runOnUiThread(() -> {
                NewsAdapter adapter = new NewsAdapter(this, items);
                adapter.setViewedNewsIds(viewedIds);
                recyclerView.setAdapter(adapter);
            });
        });
    }

    // 新增方法
    private void loadNotes() {
        executorService.execute(() -> {
            List<NoteRecord> records = AppDatabase.getDatabase(this).noteDao().getAll();
            runOnUiThread(() -> {
                NoteAdapter adapter = new NoteAdapter(this, records);
                recyclerView.setAdapter(adapter);
            });
        });
    }
}