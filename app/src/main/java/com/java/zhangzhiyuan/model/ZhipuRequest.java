package com.java.zhangzhiyuan.model;
//在发起请求前，我们需要按照智谱API的要求，准备好一个包含模型名称和对话内容的请求体
//当我们创建一个ZhipuRequest对象，并让Gson将它转换成JSON时，会得到类似下面这样的字符串：
//
//        {
//        "model": "glm-4",
//        "messages": [
//        {
//        "role": "user",
//        "content": "请为以下新闻内容生成一段摘要..."
//        }
//        ]
//        }
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