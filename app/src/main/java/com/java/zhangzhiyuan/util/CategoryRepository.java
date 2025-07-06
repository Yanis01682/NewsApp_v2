package com.java.zhangzhiyuan.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.java.zhangzhiyuan.model.Category;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CategoryRepository {

    private static final String PREFS_NAME = "category_prefs";
    private static final String KEY_MY_CATEGORIES = "my_categories";
    private static final List<Category> ALL_CATEGORIES = Arrays.asList(
            new Category("娱乐", "娱乐"),
            new Category("军事", "军事"),
            new Category("教育", "教育"),
            new Category("文化", "文化"),
            new Category("健康", "健康"),
            new Category("财经", "财经"),
            new Category("体育", "体育"),
            new Category("汽车", "汽车"),
            new Category("科技", "科技"),
            new Category("社会", "社会")
    );

    private final SharedPreferences sharedPreferences;
    private final Gson gson = new Gson();

    public CategoryRepository(Context context) {
        sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public List<Category> getMyCategories() {
        String json = sharedPreferences.getString(KEY_MY_CATEGORIES, null);
        if (json == null) {
            // 如果是第一次，返回默认的前8个分类
            return new ArrayList<>(ALL_CATEGORIES.subList(0, 8));
        }
        Type type = new TypeToken<ArrayList<Category>>() {}.getType();
        return gson.fromJson(json, type);
    }

    public void saveMyCategories(List<Category> myCategories) {
        String json = gson.toJson(myCategories);
        sharedPreferences.edit().putString(KEY_MY_CATEGORIES, json).apply();
    }

    public List<Category> getMoreCategories() {
        List<Category> myCategories = getMyCategories();
        List<Category> moreCategories = new ArrayList<>(ALL_CATEGORIES);
        moreCategories.removeAll(myCategories);
        return moreCategories;
    }
}