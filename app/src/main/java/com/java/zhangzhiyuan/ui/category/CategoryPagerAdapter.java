package com.java.zhangzhiyuan.ui.category;
//页面切换
// 当ViewPager2需要显示一个新页面时，它会调用这个方法
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

    /**
     * --- 核心修正：重写getItemId和containsItem来保证Fragment的正确刷新 ---
     *
     * 返回列表中指定位置的项的唯一标识符。
     * 我们使用 Category 对象的 hashCode 作为其唯一 ID。
     * 这要求 Category 类必须正确实现 hashCode() 方法。
     *
     */
    @Override
    public long getItemId(int position) {
        return categories.get(position).hashCode();
    }

    /**
     * 检查适配器的数据集中是否包含具有给定 ID 的项。
     * 当数据集发生变化时，ViewPager2 会调用此方法来确定某个 Fragment 是否仍然有效。
     */
    @Override
    public boolean containsItem(long itemId) {
        //检查具有该ID的项是否仍然存在于数据集中
        for (Category category : categories) {
            if (category.hashCode() == itemId) {
                return true;
            }
        }
        return false;
    }
}