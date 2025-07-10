package com.java.zhangzhiyuan.model;

import android.util.Log;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

public class NewsItem implements Serializable {
    private static final String TAG = "NewsItemParser";
    private static final List<String> BLOCKED_DOMAINS = Arrays.asList("n.sinaimg.cn", "imgpai.thepaper.cn", "p1.ifengimg.com", "finance.people.com.cn","ll.anhuinews.com","static.cnbetacdn.com","pic.enorth.com.cn","cssn.cn","static.statickksmg.com","kjt.fujian.gov.cn","pic.anhuinews.com");

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


    // 替换 NewsItem.java 中的 getImage() 方法

    public String getImage() {
        // 1. 检查原始数据是否为空
        if (this.image == null || this.image.trim().isEmpty()) {
            return null;
        }

        // 2. 去掉首尾的双引号（如果有的话）
        String processedImage = this.image.trim();
        if (processedImage.startsWith("\"") && processedImage.endsWith("\"")) {
            processedImage = processedImage.substring(1, processedImage.length() - 1).trim();
        }

        // 3. 去除所有的方括号 [] 和空格
        processedImage = processedImage.replace("[", "").replace("]", "").trim();

        // 4. 如果处理后为空，直接返回null
        if (processedImage.isEmpty()) {
            return null;
        }

        // 5. 按逗号分割URL
        String[] urls = processedImage.split(",");

        // 6. 遍历URL，找第一个有效且没有被屏蔽的URL
        for (String url : urls) {
            String trimmedUrl = url.trim();
            if (!trimmedUrl.isEmpty()) {
                boolean isBlocked = false;
                // 检查是否为屏蔽的域名
                for (String blockedDomain : BLOCKED_DOMAINS) {
                    if (trimmedUrl.contains(blockedDomain)) {
                        isBlocked = true;
                        break; // 发现是屏蔽域名，停止检查
                    }
                }
                // 返回第一个未屏蔽的URL
                if (!isBlocked) {
                    return trimmedUrl;
                }
            }
        }

        // 如果没有找到有效的图片URL，则返回null
        return null;
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