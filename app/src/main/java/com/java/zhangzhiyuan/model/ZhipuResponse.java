package com.java.zhangzhiyuan.model;
//从AI那里收到的“回信”
import java.util.List;

public class ZhipuResponse {
    //这是一个数组，包含了AI生成的多个候选答案。通常我们只取第一个。
    public List<Choice> choices;
    //choices数组中最重要的就是message字段
    public static class Choice {
        public Message message;
    }

    public static class Message {
        public String content;
    }
}