package com.java.zhangzhiyuan.ui.category;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.java.zhangzhiyuan.model.Category;

import java.util.List;

public class CategoryPagerAdapter extends FragmentStateAdapter {

    private final List<Category> categories;

    public CategoryPagerAdapter(@NonNull FragmentManager fragmentManager, @NonNull Lifecycle lifecycle, List<Category> categories) {
        super(fragmentManager, lifecycle);
        this.categories = categories;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        // 为每个位置创建一个新的Fragment实例
        return CategoryNewsListFragment.newInstance(categories.get(position).getValue());
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }
}