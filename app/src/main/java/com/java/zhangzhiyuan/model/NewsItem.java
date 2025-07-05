package com.java.zhangzhiyuan.model;

import android.util.Log;

import java.io.Serializable; // 1. 确保导入了这个包
import java.util.Arrays;
import java.util.List;

// 主页的新闻条目
public class NewsItem implements Serializable {
    private static final String TAG = "NewsItemParser";
    // 定义一个要屏蔽的图片域名列表
    private static final List<String> BLOCKED_DOMAINS = Arrays.asList(
            "n.sinaimg.cn",         // 新浪的二维码域名
            "imgpai.thepaper.cn"    // 新增：澎湃新闻的问题图片域名
    );

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
    public String getVideo() { return video; }

    public String getRawImageUrls() {
        return this.image;
    }
//获取有效的url链接
    public String getImage() {
        String finalUrl = null;
        String blockedByDomain = null; // 用于记录是被哪个域名屏蔽的

        // 步骤 1 & 2: 解析出原始URL
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

        // 步骤 3: 【核心过滤逻辑】检查URL是否在我们的黑名单中
        if (finalUrl != null) {
            for (String blockedDomain : BLOCKED_DOMAINS) {
                if (finalUrl.contains(blockedDomain)) {
                    blockedByDomain = blockedDomain; // 记录原因
                    finalUrl = null; // 强制设为null
                    break; // 找到一个匹配就跳出循环
                }
            }
        }

        // 步骤 4: 打印包含所有信息的最终日志
        Log.d(TAG, "新闻标题: \"" + this.title + "\"");
        Log.d(TAG, "  ├─ 原始Image字段: " + this.image);
        if(blockedByDomain != null) {
            Log.w(TAG, "  ├─ URL被屏蔽: 因为它包含了黑名单域名 -> " + blockedByDomain);
        }
        Log.d(TAG, "  └─ 最终解析URL: " + finalUrl);

        return finalUrl;
    }
}