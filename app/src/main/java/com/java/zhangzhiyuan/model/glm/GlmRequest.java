package com.java.zhangzhiyuan.model.glm;

import java.util.List;

public class GlmRequest {
    private String model;
    private List<Message> messages;
    private boolean stream = false;

    public GlmRequest(String model, List<Message> messages) {
        this.model = model;
        this.messages = messages;
    }
}