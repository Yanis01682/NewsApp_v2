package com.java.zhangzhiyuan.model.glm;

import java.util.List;

public class GlmResponse {
    private List<Choice> choices;

    public String getSummary() {
        if (choices != null && !choices.isEmpty()) {
            Choice firstChoice = choices.get(0);
            if (firstChoice != null && firstChoice.getMessage() != null) {
                return firstChoice.getMessage().getContent();
            }
        }
        return "摘要生成失败或无内容";
    }
}