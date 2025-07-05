package com.java.zhangzhiyuan.ui.detail;

import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager2.widget.ViewPager2;

// ExoPlayer相关的导入
import androidx.media3.common.MediaItem;
import androidx.media3.common.PlaybackException;
import androidx.media3.common.Player;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory;
import androidx.media3.ui.PlayerView;

// 【关键导入】导入我们需要的“知情同意”注解
import androidx.media3.common.util.UnstableApi;

import com.java.zhangzhiyuan.R;
import com.java.zhangzhiyuan.db.AppDatabase;
import com.java.zhangzhiyuan.model.NewsItem;
import com.java.zhangzhiyuan.model.Summary;
import com.java.zhangzhiyuan.model.ZhipuRequest;
import com.java.zhangzhiyuan.model.ZhipuResponse;
import com.java.zhangzhiyuan.network.RetrofitClient;
import com.java.zhangzhiyuan.util.JwtTokenGenerator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

// --- 【核心修正】在这里，为整个类加上“知情同意”的注解 ---
@UnstableApi
public class NewsDetailActivity extends AppCompatActivity {

    private static final String TAG = "NewsDetailActivity";
    private static final String API_KEY = "805c28b7b77747a88617bfb68e53aca8.FJK3H1Frt1nhGVvR";

    // UI 控件
    private TextView titleTextView, sourceTextView, timeTextView, summaryTextView;
    private Toolbar toolbar;
    private ViewPager2 imageSliderPager;
    private LinearLayout indicatorContainer;
    private PlayerView playerView;

    // 播放器实例
    private ExoPlayer player;
    private String currentVideoUrl;
    private boolean playWhenReady = true;
    private long playbackPosition = 0;

    private AppDatabase db;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    interface SummaryCallback { void onSuccess(String summary); void onFailure(String error); }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news_detail);
        initViews();
        setupToolbar();
        db = AppDatabase.getDatabase(this);
        NewsItem newsItem = (NewsItem) getIntent().getSerializableExtra("news_item");
        if (newsItem != null) {
            populateViews(newsItem);
            handleImageGallery(newsItem);
            this.currentVideoUrl = newsItem.getVideo();
            loadSummary(newsItem.getNewsID(), newsItem.getContent());
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            initializePlayer();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if ((Build.VERSION.SDK_INT < Build.VERSION_CODES.N && player == null)) {
            initializePlayer();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            releasePlayer();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            releasePlayer();
        }
    }

    private void initializePlayer() {
        if (currentVideoUrl != null && !currentVideoUrl.isEmpty()) {
            try {
                // 使用 DefaultMediaSourceFactory，它可以智能识别并处理各种视频格式
                DefaultMediaSourceFactory mediaSourceFactory = new DefaultMediaSourceFactory(this);

                player = new ExoPlayer.Builder(this)
                        .setMediaSourceFactory(mediaSourceFactory)
                        .build();

                playerView.setPlayer(player);
                playerView.setVisibility(View.VISIBLE);

                MediaItem mediaItem = MediaItem.fromUri(Uri.parse(currentVideoUrl));

                player.addListener(new Player.Listener() {
                    @Override
                    public void onPlayerError(@NonNull PlaybackException error) {
                        Log.e(TAG, "播放器错误: ", error);
                        Toast.makeText(NewsDetailActivity.this, "播放器错误: " + error.getCause().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });

                player.setMediaItem(mediaItem);
                player.setPlayWhenReady(playWhenReady);
                player.seekTo(playbackPosition);
                player.prepare();

            } catch (Exception e) {
                Log.e(TAG, "初始化播放器时发生严重错误", e);
                playerView.setVisibility(View.GONE);
                Toast.makeText(this, "视频播放失败: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        } else {
            playerView.setVisibility(View.GONE);
        }
    }

    private void releasePlayer() {
        if (player != null) {
            playWhenReady = player.getPlayWhenReady();
            playbackPosition = player.getCurrentPosition();
            player.release();
            player = null;
            playerView.setPlayer(null);
        }
    }

    // --- 以下是其他已经调通的、无需修改的代码 ---
    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        titleTextView = findViewById(R.id.news_detail_title);
        sourceTextView = findViewById(R.id.news_detail_source);
        timeTextView = findViewById(R.id.news_detail_time);
        summaryTextView = findViewById(R.id.news_detail_summary);
        indicatorContainer = findViewById(R.id.image_slider_indicator_container);
        imageSliderPager = findViewById(R.id.image_slider_pager);
        playerView = findViewById(R.id.player_view);
    }
    private void loadSummary(final String newsId, final String newsContent) {
        summaryTextView.setText("摘要生成中，请稍候...");
        executorService.execute(() -> {
            Summary summary = db.summaryDao().getSummaryById(newsId);
            if (summary != null) { runOnUiThread(() -> summaryTextView.setText(summary.summaryText)); }
            else {
                fetchSummaryFromGlmApi(newsContent, new SummaryCallback() {
                    @Override
                    public void onSuccess(String generatedSummary) {
                        String indentedSummary = "\u3000\u3000" + generatedSummary.replace("【GLM模拟摘要】", "").trim();
                        Summary newSummary = new Summary();
                        newSummary.newsId = newsId;
                        newSummary.summaryText = indentedSummary;
                        executorService.execute(() -> db.summaryDao().insert(newSummary));
                        runOnUiThread(() -> summaryTextView.setText(indentedSummary));
                    }
                    @Override
                    public void onFailure(String error) { runOnUiThread(() -> summaryTextView.setText(error)); }
                });
            }
        });
    }
    private void fetchSummaryFromGlmApi(final String content, final SummaryCallback callback) {
        String token = JwtTokenGenerator.generateToken(API_KEY, 3600 * 1000);
        ZhipuRequest.ChatMessage message = new ZhipuRequest.ChatMessage("user", "请为以下新闻内容生成一段100到150字左右的摘要，请直接返回摘要内容，不要包含“好的”、“当然”等多余的词语：\n\n" + content);
        ZhipuRequest requestBody = new ZhipuRequest("glm-4", Collections.singletonList(message));
        RetrofitClient.getApiService().getChatSummary("Bearer " + token, requestBody).enqueue(new retrofit2.Callback<ZhipuResponse>() {
            @Override
            public void onResponse(@NonNull retrofit2.Call<ZhipuResponse> call, @NonNull retrofit2.Response<ZhipuResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try { callback.onSuccess(response.body().choices.get(0).message.content); }
                    catch (Exception e) { callback.onFailure("摘要解析失败"); }
                } else { callback.onFailure("摘要生成失败，响应码: " + response.code()); }
            }
            @Override
            public void onFailure(@NonNull retrofit2.Call<ZhipuResponse> call, @NonNull Throwable t) { callback.onFailure("摘要生成失败：网络异常"); }
        });
    }
    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) { getSupportActionBar().setDisplayHomeAsUpEnabled(true); getSupportActionBar().setDisplayShowTitleEnabled(false); }
    }
    private void populateViews(NewsItem newsItem) {
        titleTextView.setText(newsItem.getTitle());
        sourceTextView.setText(newsItem.getPublisher());
        timeTextView.setText(newsItem.getPublishTime());
    }
    private void handleImageGallery(NewsItem newsItem) {
        String rawImageUrls = newsItem.getRawImageUrls();
        if (rawImageUrls != null && rawImageUrls.startsWith("[") && rawImageUrls.endsWith("]")) {
            String urlsInsideBrackets = rawImageUrls.substring(1, rawImageUrls.length() - 1);
            if (!urlsInsideBrackets.trim().isEmpty()) {
                String[] urlArray = urlsInsideBrackets.split(",");
                List<String> cleanedImageUrls = new ArrayList<>();
                for (String url : urlArray) { if (!url.trim().isEmpty()) { cleanedImageUrls.add(url.trim()); } }
                if (cleanedImageUrls.size() > 0) {
                    ImageSliderAdapter adapter = new ImageSliderAdapter(cleanedImageUrls);
                    imageSliderPager.setAdapter(adapter);
                    imageSliderPager.setVisibility(View.VISIBLE);
                    if (cleanedImageUrls.size() > 1) {
                        indicatorContainer.setVisibility(View.VISIBLE);
                        createIndicators(cleanedImageUrls.size());
                        imageSliderPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
                            @Override
                            public void onPageSelected(int position) { super.onPageSelected(position); updateIndicatorState(position); }
                        });
                        updateIndicatorState(0);
                    } else { indicatorContainer.setVisibility(View.GONE); }
                    return;
                }
            }
        }
        imageSliderPager.setVisibility(View.GONE);
        indicatorContainer.setVisibility(View.GONE);
    }
    private void createIndicators(int count) {
        indicatorContainer.removeAllViews();
        for (int i = 0; i < count; i++) {
            ImageView indicator = new ImageView(this);
            indicator.setImageResource(R.drawable.tab_selector);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.setMargins(8, 0, 8, 0);
            indicator.setLayoutParams(params);
            indicatorContainer.addView(indicator);
        }
    }
    private void updateIndicatorState(int position) {
        for (int i = 0; i < indicatorContainer.getChildCount(); i++) {
            View child = indicatorContainer.getChildAt(i);
            if (child instanceof ImageView) { child.setSelected(i == position); }
        }
    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) { finish(); return true; }
        return super.onOptionsItemSelected(item);
    }
}