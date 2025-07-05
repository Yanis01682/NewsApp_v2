package com.java.zhangzhiyuan.model;

import java.util.List;

public class ZhipuRequest {
    public String model;
    public List<ChatMessage> messages;

    public ZhipuRequest(String model, List<ChatMessage> messages) {
        this.model = model;
        this.messages = messages;
    }

    public static class ChatMessage {
        public String role;
        public String content;

        public ChatMessage(String role, String content) {
            this.role = role;
            this.content = content;
        }
    }
}