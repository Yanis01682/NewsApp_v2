package com.java.zhangzhiyuan.adapter;
//这个适配器是为MainActivity中的主ViewPager2服务的，
// 它的作用是根据位置提供对应的“主功能”Fragment。
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.java.zhangzhiyuan.ui.category.CategoryFragment;
import com.java.zhangzhiyuan.ui.home.HomeFragment;
import com.java.zhangzhiyuan.ui.my.MyFragment;

public class MainViewPagerAdapter extends FragmentStateAdapter {

    public MainViewPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        // 根据位置返回对应的Fragment
        switch (position) {
            case 1:
                return new CategoryFragment();
            case 2:
                return new MyFragment();
            default: // 默认位置0
                return new HomeFragment();
        }
    }

    @Override
    public int getItemCount() {
        // 我们总共有3个页面
        return 3;
    }
}