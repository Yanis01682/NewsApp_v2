package com.java.zhangzhiyuan.model;

public class NewsItem {
    // 字段名与JSON中的key完全对应
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

    public String getImage() {
        // API返回的image字段是 "[url]" 或 "[]" 的形式，我们需要解析出真正的URL
        if (image != null && image.length() > 2) {
            return image.substring(1, image.length() - 1);
        }
        return null; // 如果没有图片URL，返回null
    }
}