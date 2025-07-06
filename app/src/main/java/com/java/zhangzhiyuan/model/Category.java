package com.java.zhangzhiyuan.model;

import java.io.Serializable;
import java.util.Objects;

public class Category implements Serializable {
    private String name;
    // 可以添加一个英文名或ID，用于API请求
    private String value;

    public Category(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    // 重写equals和hashCode，方便在列表中进行比较
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Category category = (Category) o;
        return Objects.equals(name, category.name) && Objects.equals(value, category.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, value);
    }
}