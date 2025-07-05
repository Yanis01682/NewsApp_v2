package com.java.zhangzhiyuan.model;

import java.util.List;

public class ZhipuResponse {
    public List<Choice> choices;

    public static class Choice {
        public Message message;
    }

    public static class Message {
        public String content;
    }
}