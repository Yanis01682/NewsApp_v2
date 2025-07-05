package com.java.zhangzhiyuan.ui.detail;
//新闻详情页
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhipu.oapi.ClientV4;
import com.zhipu.oapi.Constants;
import com.zhipu.oapi.service.v4.model.ChatCompletionRequest;
import com.zhipu.oapi.service.v4.model.ChatMessage;
import com.zhipu.oapi.service.v4.model.ChatMessageRole;
import com.zhipu.oapi.service.v4.model.ModelApiResponse;
import com.java.zhangzhiyuan.R;
import com.java.zhangzhiyuan.db.AppDatabase;
import com.java.zhangzhiyuan.model.NewsItem;
import com.java.zhangzhiyuan.model.Summary;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NewsDetailActivity extends AppCompatActivity {

    private static final String TAG = "NewsDetailActivity";
    private static final String API_KEY = "805c28b7b77747a88617bfb68e53aca8.FJK3H1Frt1nhGVvR"; // 替换为你的API Key
    private static final String requestIdTemplate = "thu-%d";
    private static final ClientV4 client = new ClientV4.Builder(API_KEY).build();
    private TextView titleTextView, sourceTextView, timeTextView, summaryTextView;
    private VideoView videoView;
    private Toolbar toolbar;
    private ViewPager2 imageSliderPager;
    private LinearLayout indicatorContainer;

    //实现摘要的本地存储
    private AppDatabase db;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    //构建详情界面
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
            handleVideo(newsItem);
            loadSummary(newsItem.getNewsID(), newsItem.getContent());
        }
    }
//如果本地数据库有摘要，加载，没有再调用API
    private void loadSummary(final String newsId, final String newsContent) {
        summaryTextView.setText("摘要生成中，请稍候...");
        //contentTextView.setVisibility(View.GONE); // 隐藏原文

        executorService.execute(() -> {
            Summary summary = db.summaryDao().getSummaryById(newsId);
            if (summary != null) {
                Log.d(TAG, "从数据库加载摘要成功");
                runOnUiThread(() -> summaryTextView.setText(summary.summaryText));
            } else {
                Log.d(TAG, "数据库无摘要，开始调用API");
                String generatedSummary = fetchSummaryFromGlmApi(newsContent);

                if (!generatedSummary.contains("失败")) {
                    Summary newSummary = new Summary();
                    newSummary.newsId = newsId;
                    newSummary.summaryText = generatedSummary;
                    db.summaryDao().insert(newSummary);
                    Log.d(TAG, "API调用成功，摘要已存入数据库");
                } else {
                    Log.e(TAG, "API调用失败或返回错误信息");
                }
                runOnUiThread(() -> summaryTextView.setText(generatedSummary));
            }
        });
    }
    //调用API生成摘要
    private String fetchSummaryFromGlmApi(String content) {
        String apiKey = "805c28b7b77747a88617bfb68e53aca8.FJK3H1Frt1nhGVvR";

        ClientV4 client = new ClientV4.Builder(apiKey).build();


        List<ChatMessage> messages = new ArrayList<>();
        ChatMessage chatMessage = new ChatMessage(ChatMessageRole.USER.value(),
                "请为以下新闻内容生成一段100到150字左右的摘要，请直接返回摘要内容，不要包含“好的”、“当然”等多余的词语：\n\n" + content);
        //这是你向大模型输入的话
        messages.add(chatMessage);
        String requestId = String.format(requestIdTemplate, System.currentTimeMillis());

        ChatCompletionRequest chatCompletionRequest = ChatCompletionRequest.builder()
                .model(Constants.ModelChatGLM4)
                .stream(Boolean.FALSE)
                .invokeMethod(Constants.invokeMethod)
                .messages(messages)
                .requestId(requestId)
                .build();
        try {
            ModelApiResponse response = client.invokeModelApi(chatCompletionRequest);

            if (response.isSuccess() && response.getData() != null) {
                return response.getData().getChoices().get(0).getMessage().getContent().toString();
            } else {
                Log.e(TAG, "API调用失败: " + response.getMsg());
                return "摘要生成失败：" + response.getMsg();
            }
        } catch (Exception e) {
            Log.e(TAG, "调用API发生异常", e);
            return "摘要生成失败：发生异常";
        }
    }

    //
    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        titleTextView = findViewById(R.id.news_detail_title);
        sourceTextView = findViewById(R.id.news_detail_source);
        timeTextView = findViewById(R.id.news_detail_time);
        videoView = findViewById(R.id.news_detail_video);
        summaryTextView = findViewById(R.id.news_detail_summary);
        indicatorContainer = findViewById(R.id.image_slider_indicator_container);
        imageSliderPager = findViewById(R.id.image_slider_pager);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
    }

    private void populateViews(NewsItem newsItem) {
        titleTextView.setText(newsItem.getTitle());
        sourceTextView.setText(newsItem.getPublisher());
        timeTextView.setText(newsItem.getPublishTime());
        //contentTextView.setText(newsItem.getContent());
    }
    private void handleImageGallery(NewsItem newsItem) {
        String rawImageUrls = newsItem.getRawImageUrls();
        if (rawImageUrls != null && rawImageUrls.startsWith("[") && rawImageUrls.endsWith("]")) {
            String urlsInsideBrackets = rawImageUrls.substring(1, rawImageUrls.length() - 1);
            if (!urlsInsideBrackets.trim().isEmpty()) {
                String[] urlArray = urlsInsideBrackets.split(",");
                List<String> cleanedImageUrls = new ArrayList<>();
                for (String url : urlArray) {
                    if (!url.trim().isEmpty()) {
                        cleanedImageUrls.add(url.trim());
                    }
                }
                if (cleanedImageUrls.size() <= 1) {
                    imageSliderPager.setVisibility(View.GONE);
                    indicatorContainer.setVisibility(View.GONE);
                    return;
                }
                ImageSliderAdapter adapter = new ImageSliderAdapter(cleanedImageUrls);
                imageSliderPager.setAdapter(adapter);
                imageSliderPager.setVisibility(View.VISIBLE);
                createIndicators(cleanedImageUrls.size());
                imageSliderPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
                    @Override
                    public void onPageSelected(int position) {
                        super.onPageSelected(position);
                        updateIndicatorState(position);
                    }
                });
                updateIndicatorState(0);
                return;
            }
        }
        imageSliderPager.setVisibility(View.GONE);
        indicatorContainer.setVisibility(View.GONE);
    }

    private void createIndicators(int count) {
        indicatorContainer.removeAllViews();
        indicatorContainer.setVisibility(View.VISIBLE);
        indicatorContainer.setWeightSum(count);
        int activeColor = ContextCompat.getColor(this, R.color.purple_500);
        int inactiveColor = Color.parseColor("#E0E0E0");
        for (int i = 0; i < count; i++) {
            View indicator = new View(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1.0f);
            if (i > 0) {
                params.setMarginStart(8);
            }
            indicator.setLayoutParams(params);
            indicator.setBackgroundColor(i == 0 ? activeColor : inactiveColor);
            indicatorContainer.addView(indicator);
        }
    }

    private void updateIndicatorState(int position) {
        int childCount = indicatorContainer.getChildCount();
        int activeColor = ContextCompat.getColor(this, R.color.purple_500);
        int inactiveColor = Color.parseColor("#E0E0E0");
        for (int i = 0; i < childCount; i++) {
            View indicator = indicatorContainer.getChildAt(i);
            indicator.setBackgroundColor(i == position ? activeColor : inactiveColor);
        }
    }

//处理视频逻辑
    private void handleVideo(NewsItem newsItem) {
        String videoUrl = newsItem.getVideo();
        if (videoUrl != null && !videoUrl.trim().isEmpty()) {
            videoView.setVisibility(View.VISIBLE);
            videoView.setVideoURI(Uri.parse(videoUrl));
            MediaController mediaController = new MediaController(this);
            mediaController.setAnchorView(videoView);
            videoView.setMediaController(mediaController);
            videoView.setOnPreparedListener(mp -> videoView.start());
            videoView.setOnErrorListener((mp, what, extra) -> {
                Toast.makeText(this, "视频播放失败", Toast.LENGTH_SHORT).show();
                return true;
            });
        } else {
            videoView.setVisibility(View.GONE);
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}