package com.java.zhangzhiyuan.model;

import android.util.Log;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

public class NewsItem implements Serializable {
    private static final String TAG = "NewsItemParser";
    private static final List<String> BLOCKED_DOMAINS = Arrays.asList("n.sinaimg.cn", "imgpai.thepaper.cn", "p1.ifengimg.com", "finance.people.com.cn","ll.anhuinews.com");

    private String newsID;
    private String title;
    private String publisher;
    private String publishTime;
    private String image;
    private String content;
    private String video;
    private String url;

    // Getters
    public String getNewsID() { return newsID; }
    public String getTitle() { return title; }
    public String getPublisher() { return publisher; }
    public String getPublishTime() { return publishTime; }
    public String getContent() { return content; }
    public String getRawImageUrls() { return this.image; }
    public String getUrl() {return url;}


    public String getImage() {
        String finalUrl = null;
        if (image != null && image.length() > 2) {
            String contentInsideBrackets = image.substring(1, image.length() - 1).trim();
            if (!contentInsideBrackets.isEmpty()) {
                String[] urls = contentInsideBrackets.split(",");
                for (String url : urls) {
                    String trimmedUrl = url.trim();
                    if (!trimmedUrl.isEmpty()) {
                        finalUrl = trimmedUrl;
                        break;
                    }
                }
            }
        }
        if (finalUrl != null) {
            for (String blockedDomain : BLOCKED_DOMAINS) {
                if (finalUrl.contains(blockedDomain)) {
                    return null; // 直接返回null
                }
            }
        }
        return finalUrl;
    }

    public String getVideo() {
        // 原始的getVideo逻辑保持不变
        Log.d(TAG, "获取到的原始视频URL (Raw Video URL): " + this.video);
        if (this.video == null || this.video.trim().isEmpty()) {
            return null;
        }
        return this.video.trim();
    }

    // =================================================================
    // 3. 新增的、集中的日志打印方法
    // =================================================================
    /**
     * 打印这条新闻的详细信息，方便调试。
     * @param callerTag 调用方传入的TAG，用于在Logcat中区分日志来源。
     */
    public void logDetails(String callerTag) {
        Log.d(callerTag, "----------------- 新闻日志 (由NewsItem生成) Start -----------------");
        Log.d(callerTag, "标题: " + getTitle());
        Log.d(callerTag, "未解析的图片URL: " + getRawImageUrls());
        Log.d(callerTag, "解析后的图片URL: " + getImage());
        Log.d(callerTag, "未解析的视频URL: " + this.video); // 直接访问原始字段
        Log.d(callerTag, "解析后的视频URL: " + getVideo());
        Log.d(callerTag, "正文: " + getContent());
        Log.d(callerTag, "----------------- 新闻日志 End -------------------");
    }
}