package com.java.zhangzhiyuan.model;

import android.util.Log;

public class NewsItem {
    // 定义一个TAG，方便在Logcat中过滤日志
    private static final String TAG = "NewsItem";
    // Fields remain the same
    private String newsID;
    private String title;
    private String publisher;
    private String publishTime;
    private String image; // This is the raw string from the server, e.g., "[url1,url2]"
    private String content;
    private String video;

    // --- Getters for other fields remain the same ---
    public String getNewsID() { return newsID; }
    public String getTitle() { return title; }
    public String getPublisher() { return publisher; }
    public String getPublishTime() { return publishTime; }
    public String getContent() { return content; }
    public String getVideo() { return video; }


    // ================== The Improved getImage() Method ==================
    /**
     * This method processes the raw 'image' string from the server.
     * It handles three cases:
     * 1. No URL (e.g., "[]" or null) -> returns null.
     * 2. A single URL (e.g., "[http://.../1.jpg]") -> returns "http://.../1.jpg".
     * 3. Multiple URLs (e.g., "[http://.../1.jpg,http://.../2.jpg]") -> returns only the first URL.
     * @return A single, clean image URL string, or null if no valid URL exists.
     */
    public String getImage() {
        Log.d(TAG, "未解析的url: " + image + "'");
        // 1. Check if the raw string is null or too short to contain a URL
        if (image == null || image.length() <= 2) {
            return null;
        }

        // 2. Remove the outer brackets "[]"
        String contentInsideBrackets = image.substring(1, image.length() - 1);

        // If content is now empty, there was no URL
        if (contentInsideBrackets.isEmpty()){
            return null;
        }

        // 3. Check if the content contains multiple URLs separated by a comma
        if (contentInsideBrackets.contains(",")) {
            // Split the string by the comma and return the first part.
            // .trim() removes any accidental leading/trailing spaces.
            String[] urls = contentInsideBrackets.split(",\\s*");
            for (String url : urls) {
                // 检查当前这段url是否不为null，并且去掉空格后也不是空字符串
                if (url != null && !url.trim().isEmpty()) {
                    // 找到了第一个有效的URL，立刻返回它！
                    return url.trim();
                }
            }
        }
        return null;
    }
    // ====================================================================
}