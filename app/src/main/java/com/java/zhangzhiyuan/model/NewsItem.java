package com.java.zhangzhiyuan.model;

import android.util.Log;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

public class NewsItem implements Serializable {
    private static final String TAG = "NewsItemParser";
    private static final List<String> BLOCKED_DOMAINS = Arrays.asList("n.sinaimg.cn", "imgpai.thepaper.cn");

    private String newsID;
    private String title;
    private String publisher;
    private String publishTime;
    private String image;
    private String content;
    private String video;

    // Getters
    public String getNewsID() { return newsID; }
    public String getTitle() { return title; }
    public String getPublisher() { return publisher; }
    public String getPublishTime() { return publishTime; }
    public String getContent() { return content; }
    public String getRawImageUrls() { return this.image; }

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
                    finalUrl = null;
                    break;
                }
            }
        }
        return finalUrl;
    }

    // --- 【最终版本】直接返回原始URL，并添加日志 ---
    public String getVideo() {
        Log.d(TAG, "获取到的原始视频URL (Raw Video URL): " + this.video);
        if (this.video == null || this.video.trim().isEmpty()) {
            return null;
        }
        return this.video.trim();
    }
}