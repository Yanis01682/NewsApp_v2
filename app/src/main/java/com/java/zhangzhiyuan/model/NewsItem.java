package com.java.zhangzhiyuan.model;

public class NewsItem {
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
            String[] urls = contentInsideBrackets.split(",");
            //将所有url打包成string[]数组
            return urls[0].trim();
        } else {
            // 4. If no comma, it's a single URL. Return it.
            return contentInsideBrackets;
        }
    }
    // ====================================================================
}